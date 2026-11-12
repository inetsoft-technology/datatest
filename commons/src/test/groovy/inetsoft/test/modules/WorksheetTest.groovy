package inetsoft.test.modules

import inetsoft.graph.data.DataSetFilter
import inetsoft.graph.geo.GeoDataSet
import inetsoft.graph.geo.MappedDataSet
import inetsoft.report.TableLens
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.RuntimeWorksheet
import inetsoft.report.composition.execution.AssetQuerySandbox
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.report.composition.graph.BrushDataSet
import inetsoft.report.composition.graph.VGraphPair
import inetsoft.report.filter.SortFilter
import inetsoft.sree.SreeEnv
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.asset.AggregateInfo
import inetsoft.uql.asset.Assembly
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.TableAssemblyInfo
import inetsoft.uql.viewsheet.ChartVSAssembly
import inetsoft.uql.viewsheet.TableDataVSAssembly
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.composer.model.ws.ImportCSVDialogModel

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.RuntimeWorksheetResource
import inetsoft.test.core.TUtil
import inetsoft.test.core.DatatestSpringRuntimeInitializer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Verification is strict:
 * <ul>
 *   <li>an export failure fails the test; when one table fails, the remaining tables are still
 *   exported and all failures are reported together;</li>
 *   <li>compareData requires every baseline file of the case (and of the given sub folders) to
 *   have been exported by this run;</li>
 *   <li>the export folder of a case is emptied before its first export, so a stale file of an
 *   earlier run cannot hide a missing export.</li>
 * </ul>
 * With -Dupdate.baseline=true the exported files are copied into the module's
 * src/test/resources/expectData instead of being compared.
 */
class WorksheetTest {
   static ConfigurationContext context
   private static Object initializedApplicationContext
   private static def pendingEnvProperties
   // case export folders already emptied in this JVM, so several exports of one case keep their
   // files. A case that runs twice in one JVM is only emptied before its first run.
   private static final Set<String> cleanedExportDirs =
      Collections.synchronizedSet(new HashSet<String>())

   WorksheetTest(String caseName) {
      this.caseName = caseName
   }

   /**
    * init sree.home
    * @param casePackageName
    * @return
    */
   static initHome(String suiteName) {
      initHome(suiteName, null)
   }
   /**
    * ininit sree.home
    * @param casePackageName
    * @param properties : eg  def properties = ["permission.andCondition":"true"]
    * @return
    */
   static initHome(String suiteName, def properties) {
      System.err.print("=========ws.sree.home=====" + System.getProperty("ws.sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1 ? null : arrs[1].replace('.', '/')
      pendingEnvProperties = properties
   }

   /**
    * use to clear useless properties after case finished.
    * @param properties
    * @return
    */
   def clearEnv(def properties) {
      ensureRuntimeInitialized()
      properties.each { key, value ->
         SreeEnv.setProperty(key, null)
      }
      SreeEnv.save()
   }

   def initWS(String asset_id, SRPrincipal principal) {
      ensureRuntimeInitialized()
      ensureAdmin()
      principal = principal ?: admin
      ThreadContext.setContextPrincipal(principal)
      admin.setIgnoreLogin(true)
      worksheetResource = new RuntimeWorksheetResource(actionEventsUtil.openWorksheetEvent(asset_id))
      worksheetResource.initRuntimeWS(principal)
   }

   /**
    *  execute a wortsheet
    * @param params the parameters when open worksheet
    * @param bk use to store diff mode
    */
   def executeWS(String asset_id, Map<String, Object> params, String bk) {
      executeWS(asset_id, params, null, null, bk)
   }

   /**
    * Fail the test with a verification problem of this case.
    */
   protected void violation(String message, Throwable cause = null) {
      throw new AssertionError(message + ' [' + suiteName + '/' + caseName + ']', cause)
   }

   /**
    * Fail the test for the objects that could not be exported; all failures are reported.
    */
   private void failExport(String message, String asset_id, Map<String, Throwable> failures) {
      Exception ex = new Exception(failures.collect { k, v -> k + ': ' + v }.join('; '))
      failures.values().each { ex.addSuppressed(it) }
      violation(message + ' ' + asset_id, ex)
   }

   /**
    * execute worksheet, then save data to txt file
    * @param params worksheet variables
    * @param checkSQL check sql in show plan
    * @param mode view mode
    * @param pricipal the user info which open worksheet
    * @return
    */
   def executeWS(String asset_id, Map<String, Object> params, def mode, SRPrincipal principal, String bk) {
      initWS(asset_id, principal)
      principal = principal ?: admin
      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(principal)
      assetQuerySandbox = runtimeWorksheet.getAssetQuerySandbox()
      Assembly[] assemblies = runtimeWorksheet.getWorksheet().getAssemblies()

      params.each {
         assetQuerySandbox.getVariableTable().put(it.key, it.value)
      }

      // a failing table must not stop the export of the others
      Map<String, Throwable> failures = new LinkedHashMap<>()

      for(Assembly assembly : assemblies) {
         if(assembly.getAssemblyType() != Worksheet.TABLE_ASSET) {
            continue
         }

         TableAssembly tableAssembly = (TableAssembly) assembly

         if(!tableAssembly.isVisibleTable()) {
            continue
         }

         try {
            exportTable(asset_id, tableAssembly, params, mode, bk)
         }
         catch(Exception ex) {
            failures.put(tableAssembly.getName(), ex)
         }
      }

      if(!failures.isEmpty()) {
         failExport('==========execute worksheet failed:===========', asset_id, failures)
      }
   }

   private void exportTable(String asset_id, TableAssembly tableAssembly, Map<String, Object> params,
                            def mode, String bk)
   {
      String tableName = tableAssembly.getName()
      setLiveData(tableAssembly)
      def type = (mode == null ? AssetQuerySandbox.LIVE_MODE : mode)
      TableLens lens = assetQuerySandbox.getTableLens(tableName, type)

      // sort lens to void row sort issue.
      SortFilter sortlens = new SortFilter(lens)

      String fileName
      //check use diff variables to test inRange operator.
      if(caseName.contains('test in range')) {
         tableAssembly.getAllVariables().each {
            fileName = createExportFileByCase(asset_id, tableName + '_' + params.get(it.getName()), bk)
         }
      }
      else {
         fileName = createExportFileByCase(asset_id, tableName, bk)
      }

      exportData(sortlens, fileName, true)
   }

   private setLiveData(TableAssembly table) {
      table.setLiveData(true)
      table.setRuntime(table.isRuntimeSelected())
      table.setEditMode(false)
      TableAssemblyInfo info = table.getTableInfo()
      AggregateInfo group = table.getAggregateInfo()
      info.setAggregate(group != null && !group.isEmpty())
   }

   def exportData(def data, String filename, Boolean isFormat) {
      if(data == null || data == '') {
         data = ['null']
      }

      exportUtil.exportWSObject(filename, data instanceof TableLens ?
              exportUtil.wrapTable(data, isFormat) : data)
   }

   def importCSVToEMTable(String asset_id, String file, def fileModel, String suffix) {
      importCSVToEMTable(asset_id, file, fileModel, 500, suffix)
   }

   /**
    * import csv or excel to ws table
    * @param file
    * @return
    */
   def importCSVToEMTable(String asset_id, String file, def fileModel, def sleepSecond, String suffix) {
      initWS(asset_id, admin)
      File excelFile = new File(this.class.getResource('/files').getPath() + '/' + file)
      FileInputStream fileInputStream = new FileInputStream(excelFile)
      MultipartFile multipartFile = new MockMultipartFile(excelFile.getName(), excelFile.getName(), "text/plain", fileInputStream)
      def result = JsonOutput.toJson(fileModel)
      def parseResult = new JsonSlurper().parseText(result)
      ImportCSVDialogModel model = ImportCSVDialogModel.builder()
              .tableName(parseResult.tableName)
              .encodingSelected(parseResult.encodingSelected)
              .sheetsList(parseResult.sheetsList as String[])
              .sheetSelected(parseResult.sheetSelected)
              .delimiter(parseResult.delimiter as char)
              .delimiterTab(parseResult.delimiterTab)
              .detectType(parseResult.detectType)
              .fileType(parseResult.fileType)
              .firstRowCB(parseResult.firstRowCB)
              .headerCols(parseResult.headerCols)
              .headerNames(parseResult.headerNames)
              .ignoreTypeColumns(parseResult.ignoreTypeColumns as List<Integer>)
              .newTableName(parseResult.newTableName)
              .removeQuotesCB(parseResult.removeQuotesCB)
              .unpivotCB(parseResult.unpivotCB)
              .build()
      HashMap<String, Object> res = worksheetResource.processCSVUpload(model, multipartFile, admin)
      def validator = res?.get('validator')

      if(validator == null) {
         violation('Upload preview returned no validator (result keys: ' + res?.keySet() + ') ' +
                   asset_id + ' ' + file)
      }

      def failedInfo = validator.message
      if(failedInfo != null) {
         violation('Upload of ' + file + ' failed: ' + failedInfo)
      }
      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(admin)
      Thread.sleep(sleepSecond)
      Map<String, Throwable> failures = new LinkedHashMap<>()

      for(Assembly assembly : runtimeWorksheet.getWorksheet().getAssemblies()) {
         if(assembly.getAssemblyType() != Worksheet.TABLE_ASSET) {
            continue
         }

         try {
            TableLens lens = runtimeWorksheet.getAssetQuerySandbox().getTableLens(assembly.getName(), AssetQuerySandbox.LIVE_MODE)
            String fileName = createExportFileByCase(asset_id, assembly.getName(), suffix)
            exportData(lens, fileName, false)
         }
         catch(Exception ex) {
            failures.put(assembly.getName(), ex)
         }
      }

      if(!failures.isEmpty()) {
         failExport('==========import ' + file + ' failed:===========', asset_id, failures)
      }
   }

   /**
    * export vs data view data.
    * @param asset_id
    * @param params
    * @return
    */
   def exportVSComponentData(String asset_id, Map<String, String[]> params) {
      ensureRuntimeInitialized()
      ensureAdmin()
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      admin.setIgnoreLogin(true)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id))
      viewsheetResource.initRuntimeVS(admin)
      ThreadContext.setContextPrincipal(admin)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(admin)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      Map<String, Throwable> failures = new LinkedHashMap<>()

      for(Assembly assembly : assemblies) {
         String assemblyName = assembly.getName()

         try {
            if(assembly instanceof ChartVSAssembly) {
               final VGraphPair pair = sandbox.getVGraphPair(assemblyName, true, null, true, 1)
               data = pair.getData()
               if(data instanceof BrushDataSet || data instanceof GeoDataSet || data instanceof MappedDataSet) {
                  data = ((DataSetFilter) data).getDataSet()
               }
            }
            else if(assembly instanceof TableDataVSAssembly) {
               data = sandbox.getVSTableLens(assemblyName, false)
            }
            File outFile = createExportFileByCase(asset_id, assemblyName, null)
            exportUtil.exportVSObject(outFile.toString(), data)
         }
         catch(Exception e) {
            failures.put(assemblyName, e)
         }
      }

      if(!failures.isEmpty()) {
         failExport('==========export viewsheet data failed:===========', asset_id, failures)
      }
   }

   /**
    * create export file by case name
    * @param caseName
    * @param suffix
    * @return
    */
   def createExportFileByCase(String asset_id, String assemblyName, String bk) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String objName = ''
      if(asset_id.startsWith('1^128^')) {
         objName = 'VS' + File.separator + assemblyName + '.txt'
      }
      else if(asset_id.startsWith('1^2^')) {
         objName = (bk != null ? bk + '_' + assemblyName + '.txt' : assemblyName + '.txt')
      }
      else {
         new Exception("-------the Asset ID not right, please check-----").printStackTrace()
      }

      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator + caseName

      // remove the files of an earlier run of this case, otherwise a stale export hides a file
      // that this run failed to export. Done once per case and JVM.
      File caseDir = new File(fileName)

      if(cleanedExportDirs.add(caseDir.absolutePath) && caseDir.exists()) {
         caseDir.deleteDir()
      }

      File tempFile = new File(fileName + File.separator + objName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * folderName: the VS or Report folder, can be 'VS' or 'Report'
    * @param folderName
    */
   void compareData(String[] folderName) {
      // '' is the case folder itself
      List<String> folders = [''] + (folderName != null ? folderName.toList() : [])

      if(!checkBaselineExported(folders)) {
         return
      }

      compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName, 'TXT')
      folderName?.each {
         compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName + '/' + it, 'TXT')
      }
   }

   /**
    * @return true if the files should be compared, false if the baseline was updated instead
    */
   private boolean checkBaselineExported(List<String> folders) {
      if(Boolean.getBoolean('update.baseline')) {
         folders.each { writeBaseline(it) }
         // nothing is compared in this mode, make that visible in the log
         System.err.println("update.baseline=true: baseline of " + suiteName + '/' + caseName +
            " updated, NOT compared")
         return false
      }

      if(!exportFolder('').exists()) {
         violation('Nothing was exported by this run, expected ' + baselineFolder('').list()?.length +
                   ' baseline files')
      }

      def missing = []

      folders.each { folder ->
         listNames(baselineFolder(folder)).each {
            if(!new File(exportFolder(folder), it).exists()) {
               missing.add((folder ? folder + '/' : '') + it)
            }
         }
      }

      if(missing) {
         violation('Baseline files not exported by this run:\n' + missing.join('\n') + '\n')
      }

      return true
   }

   private void writeBaseline(String folder) {
      File target = new File(sourceBaselineFolder(), suiteName + File.separator + caseName +
         (folder ? File.separator + folder : ''))
      target.mkdirs()

      listNames(exportFolder(folder)).each {
         Files.copy(new File(exportFolder(folder), it).toPath(), new File(target, it).toPath(),
                    StandardCopyOption.REPLACE_EXISTING)
      }
   }

   private static List<String> listNames(File dir) {
      File[] files = dir.listFiles()

      if(files == null) {
         return []
      }

      return files.findAll { it.isFile() && (it.name.endsWith('.txt') || it.name.endsWith('.csv')) }
         .collect { it.name }
   }

   private File classesFolder() {
      return new File(this.class.getResource('/expectData').getPath()).getParentFile()
   }

   private File exportFolder(String folder) {
      return new File(classesFolder(), 'exportData' + File.separator + suiteName +
         File.separator + caseName + (folder ? File.separator + folder : ''))
   }

   private File baselineFolder(String folder) {
      return new File(classesFolder(), 'expectData' + File.separator + suiteName +
         File.separator + caseName + (folder ? File.separator + folder : ''))
   }

   /**
    * module/target/test-classes -> module/src/test/resources/expectData
    */
   private File sourceBaselineFolder() {
      return new File(classesFolder().parentFile.parentFile,
         'src' + File.separator + 'test' + File.separator + 'resources' + File.separator +
            'expectData')
   }

   private static String suiteName, caseName
   private static RuntimeWorksheetResource worksheetResource
   private static AssetQuerySandbox assetQuerySandbox
   private static RuntimeViewsheetResource viewsheetResource

   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   SRPrincipal admin

   private void ensureAdmin() {
      if(admin == null) {
         admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], [] as String[])
      }
   }

   private static synchronized ensureRuntimeInitialized() {
      def currentContext = ConfigurationContext.getContext().getApplicationContext()

      if(currentContext != null && initializedApplicationContext == currentContext &&
              pendingEnvProperties == null) {
         return
      }

      DatatestSpringRuntimeInitializer.ensureInitialized(System.getProperty('ws.sree.home',
              System.getProperty('sree.home', '.')))
      DataSpace.getDataSpace()
      context = ConfigurationContext.getContext()
      currentContext = context.getApplicationContext()

      if(pendingEnvProperties != null) {
         pendingEnvProperties.each { key, value ->
            SreeEnv.setProperty(key, value)
         }
         SreeEnv.save()
         pendingEnvProperties = null
      }

      initializedApplicationContext = currentContext
   }
}

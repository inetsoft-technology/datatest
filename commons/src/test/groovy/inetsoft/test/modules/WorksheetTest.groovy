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
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.RuntimeWorksheetResource
import inetsoft.test.core.TUtil

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class WorksheetTest {
   static ConfigurationContext context

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
    * @param properties: eg  def properties = ["permission.andCondition":"true"]
    * @return
    */
   static initHome(String suiteName, def properties) {
      System.err.print("=========ws.sree.home=====" + System.getProperty("ws.sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1? null : arrs[1].replace('.', '/')

      context = ConfigurationContext.getContext()
      context.setHome(System.getProperty("ws.sree.home"))

      if(properties != null) {
         properties.each{key, value ->
            SreeEnv.setProperty(key, value)
         }
         SreeEnv.save()
      }
   }

   /**
    * use to clear useless properties after case finished.
    * @param properties
    * @return
    */
   def clearEnv(def properties) {
      properties.each { key, value ->
         SreeEnv.setProperty(key, null)
      }
      SreeEnv.save()
   }

   def initWS(String asset_id, SRPrincipal principal) {
      DataSpace.getDataSpace()
      controllers = new ControllersResource()
      controllers.initControllers()
      controllers.initApplicationContext(context)
      ThreadContext.setContextPrincipal(principal)
      admin.setIgnoreLogin(true)
      worksheetResource = new RuntimeWorksheetResource(actionEventsUtil.openWorksheetEvent(asset_id), controllers)
      principal = principal ?: admin
      worksheetResource.initRuntimeWS(principal)
   }

   /**
    *  execute a wortsheet
    * @param params the parameters when open worksheet
    * @param bk use to store diff mode
    */
   def executeWS(String asset_id, Map<String, Object> params, String bk) {
      try{
         executeWS(asset_id, params, null, null, bk)
      } catch (Exception ex) {
         ex.printStackTrace()
      } finally {
         controllers.destroy()
      }
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
      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(principal)
      assetQuerySandbox = runtimeWorksheet.getAssetQuerySandbox()
      Assembly[] assemblies = runtimeWorksheet.getWorksheet().getAssemblies()

      params.each {
         assetQuerySandbox.getVariableTable().put(it.key, it.value)
      }
      assemblies.each {
         if(it.getAssemblyType() == Worksheet.TABLE_ASSET) {
            TableAssembly tableAssembly = (TableAssembly)it
            String tableName = tableAssembly.getName()
            if (!tableAssembly.isVisibleTable()) {
               return
            }

            setLiveData(tableAssembly)
            def type = (mode == null? AssetQuerySandbox.LIVE_MODE: mode)
            TableLens lens = assetQuerySandbox.getTableLens(tableName, type)

            // sort lens to void row sort issue.
            SortFilter sortlens = new SortFilter(lens)

            String fileName
            //check use diff variables to test inRange operator.
            if(caseName.contains('test in range')) {
               tableAssembly.getAllVariables().each {
                  fileName = createExportFileByCase(asset_id, tableName + '_' + params.get(it.getName()) , bk)
               }
            }
            else {
               fileName = createExportFileByCase(asset_id, tableName, bk)
            }

            exportData(sortlens, fileName, true)
         }
      }
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
      File file = new File(filename)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      } else if(file.exists()){
         file.delete()
      }

      if(data == null || data == '') {
         data = ['null']
      }

      if(data instanceof TableLens) {
         TableLens table =  exportUtil.wrapTable(data, isFormat)
         StringBuffer buffer = new StringBuffer()
         int row = 0
         while (table.moreRows(row)) {
            for(int col = 0; col < table.getColCount(); col++) {
               buffer.append(table.getObject(row, col))
               if(table.getColCount() != (col+1)) {
                  buffer.append(', ')
               }
            }
            buffer.append('\n')
            row++
         }
         file.withPrintWriter { printWriter ->
            printWriter.println("The table size(row x col) is:(" + row + " x " + table
                    .getColCount() + ")")
            printWriter.print(buffer.toString())
         }
      }
      else if(data instanceof StringBuffer) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data.toString())
         }
      }
      else if(data != null) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data)
         }
      }
   }

   def importCSVToEMTable(String asset_id, String file, def fileModel, String suffix) {
      try {
         importCSVToEMTable(asset_id, file, fileModel, 500, suffix)
      } catch(Exception ex) {
         ex.printStackTrace()
      } finally {
         controllers.destroy()
      }
   }

   /**
    * import csv or excel to ws table
    * @param file
    * @return
    */
   def importCSVToEMTable(String asset_id, String file, def fileModel, def sleepSecond, String suffix) {
      initWS(asset_id, admin)
      File  excelFile= new File(this.class.getResource('/files').getPath() + '/' + file)
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
      def failedInfo = res.get('validator').message
      if (failedInfo != null) {
         assert false : failedInfo
      }
      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(admin)
      Thread.sleep(sleepSecond)
      runtimeWorksheet.getWorksheet().getAssemblies().each {
         if(it.getAssemblyType() == Worksheet.TABLE_ASSET) {
            TableLens lens = runtimeWorksheet.getAssetQuerySandbox().getTableLens(it.getName(), AssetQuerySandbox.LIVE_MODE)
            String fileName = createExportFileByCase(asset_id, it.getName(), suffix)
            exportData(lens, fileName, false)
         }
      }
   }

   /**
    * export vs data view data.
    * @param asset_id
    * @param params
    * @return
    */
   def exportVSComponentData(String asset_id, Map<String, String[]> params) {
      controllers = new ControllersResource()
      controllers.initControllers()
      controllers.initApplicationContext(context)
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      admin.setIgnoreLogin(true)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent (params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(admin)
      ThreadContext.setContextPrincipal(admin)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(admin)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      String assemblyName = ''
      File outFile
      try {
         assemblies.each {
            assemblyName = it.getName()

            if(it instanceof ChartVSAssembly) {
               final VGraphPair pair = sandbox.getVGraphPair(assemblyName, true, null, true,1)
               data = pair.getData()
               if(data instanceof BrushDataSet || data instanceof GeoDataSet || data instanceof MappedDataSet) {
                  data = ((DataSetFilter)data).getDataSet()
               }
            }
            else if(it instanceof TableDataVSAssembly){
               data = sandbox.getVSTableLens(assemblyName, false)
            }
            outFile = createExportFileByCase(asset_id, assemblyName, null)
            exportUtil.exportVSObject(outFile.toString(), data)
         }
      }catch(Exception e) {
         e.printStackTrace()
      }finally {
         controllers.destroy()
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
      else if (asset_id.startsWith('1^2^')) {
         objName = (bk != null?  bk + '_' + assemblyName + '.txt' : assemblyName + '.txt')
      }
      else {
         new Exception("-------the Asset ID not right, please check-----").printStackTrace()
      }

      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator  + caseName
      File tempFile = new File(fileName + File.separator + objName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()){
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * folderName: the VS or Report folder, can be 'VS' or 'Report'
    * @param folderName
    */
   void compareData(String[] folderName) {
      compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName, 'TXT')
      if (folderName != null) {
         folderName.each {
            compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName + '/' + it, 'TXT')
         }
      }
   }

   private static String suiteName, caseName
   private static ControllersResource controllers
   private static RuntimeWorksheetResource worksheetResource
   private static AssetQuerySandbox assetQuerySandbox
   private static RuntimeViewsheetResource viewsheetResource

   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], [] as String[])}

package inetsoft.test.modules

import inetsoft.graph.data.DataSetFilter
import inetsoft.graph.geo.GeoDataSet
import inetsoft.graph.geo.MappedDataSet
import inetsoft.report.TableLens
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.RuntimeWorksheet
import inetsoft.report.composition.execution.AssetQuerySandbox
import inetsoft.report.composition.execution.DataMap
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.report.composition.graph.BrushDataSet
import inetsoft.report.composition.graph.VGraphPair
import inetsoft.report.filter.SortFilter
import inetsoft.sree.SreeEnv
import inetsoft.sree.internal.SUtil
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.asset.Assembly
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.AggregateInfo
import inetsoft.uql.asset.internal.TableAssemblyInfo
import inetsoft.uql.viewsheet.ChartVSAssembly
import inetsoft.uql.viewsheet.CurrentSelectionVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.GroupContainerVSAssembly
import inetsoft.uql.viewsheet.ShapeVSAssembly
import inetsoft.uql.viewsheet.TabVSAssembly
import inetsoft.uql.viewsheet.TableDataVSAssembly
import inetsoft.uql.viewsheet.VSAssembly
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.composer.ws.event.OpenWorksheetEvent
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.RuntimeWorksheetResource
import inetsoft.test.core.TUtil

import java.awt.image.BufferedImage

/**
 * to execute viewsheet/worksheet/report method
 */
class GlobalTest {
   GlobalTest(String caseName) {
      this.caseName = caseName
   }

   /**
    * init home
    */
   static initHome(String suiteName) {
      initHome(suiteName, null)
   }

   /**
    * init home
    */
   static initHome(String suiteName, def properties) {
      def arrs = suiteName.split('.cases')
      this.suiteName = (arrs.length == 1 ? null : arrs[1].replace('.', '/'))
      context = ConfigurationContext.getContext()
      context.setHome(System.getProperty("sree.home"))

      if(properties != null) {
         properties.each { key, value ->
            SreeEnv.setProperty(key, value)
         }
         SreeEnv.save()
      }
   }

   /**
    * execute multitanet test
    */
   def executeTest(String asset_id, Map<String, Object> params) {
      executeTest(asset_id, null, params)
   }

   /**
    * execute multitanet test
    * @param asset_id : vs entry, ws entry, report path
    * @param bks : bk for vs, report and ws is null
    * @param params
    * @return
    */
   def executeTest(String asset_id, String[] bks, Map<String, Object> params) {
      if(asset_id.startsWith('1^128^')) {
         executeVS(asset_id, bks, params, true)
      }
      else if(asset_id.startsWith('1^2^')) {
         executeWS(asset_id, params)
      }
      else {
         new Exception('----input asset id not right-----').printStackTrace()
      }
   }

   /**
    * export vs as PNG,HTML,print layout
    */
   def executeVS(String asset_id, String[] bks, Map<String, String[]> params, boolean match) {
      executeVS(asset_id, bks, params, ['PNG'], match)
   }

   def executeVS(String asset_id, String[] bks, Map<String, String[]> params, def types, boolean match) {
      DataSpace.getDataSpace()
      controllers = new ControllersResource()
      controllers.initControllers()
      ThreadContext.setContextPrincipal(admin)

      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      SUtil.setAdditionalDatasource(admin)
      viewsheetResource.initRuntimeVS(admin)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(admin)
      rvs.gotoBookmark('(Home)', admin.getUser().getUserIdentity(), admin)
      Optional<ViewsheetSandbox> sandboxOpt = rvs.getViewsheetSandbox()
      if(sandboxOpt.isPresent()) {
         sandboxOpt.get().resetAll(new ChangedAssemblyList())
      }

      def outFile, out
      types.each {
         if(it == 'PNG') {
            outFile = createVSExportFile(asset_id, '.png')
            out = new FileOutputStream(outFile)
            try {
               viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, match,
                       false, false, false, false,
                       bks, false, false, null, new ExportResponse(out), admin)
            }
            finally {
               out.close()
            }
         }
         else if(it == 'PDF') {
            outFile = createVSExportFile(asset_id, '.pdf')
            out = new FileOutputStream(outFile)
            try {
               viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true,
                       false, false, true, false,
                       bks, false, false, null, new ExportResponse(out), admin)
               Thread.sleep(2000)
               tutil.convertPDFToPNG(outFile.toString())
            }
            finally {
               out.close()
            }
         }
         else if(it == 'HTML') {
            outFile = createVSExportFile(asset_id, '.html')
            out = new FileOutputStream(outFile)
            try {
               viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_HTML, true,
                       false, false, false, false,
                       bks, false, false, null, new ExportResponse(out), admin)
            }
            finally {
               out.close()
            }
         }
      }
   }

   private createVSExportFile(String asset_id, String suffix) {
      String vsName = (asset_id.indexOf('/') > 0 ? asset_id.split('/').last() : asset_id.minus('1^128^__NULL__^'))
      String fileName = 'VS' + File.separator + vsName + suffix
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()

      File tempFile = new File(resourcePath + '/exportData' + suiteName + File.separator + caseName +
              File.separator + fileName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
   }

   /**
    *
    * @param params the parameters when open worksheet
    * @param asset_id : ws entry id
    */
   def executeWS(String asset_id, Map<String, String[]> params) {
      DataSpace.getDataSpace()
      controllers = new ControllersResource()
      controllers.initControllers()
      controllers.initApplicationContext(context)
      ThreadContext.setContextPrincipal(admin)
      OpenWorksheetEvent openWorksheetEvent = actionEventsUtil.openWorksheetEvent(asset_id)
      worksheetResource = new RuntimeWorksheetResource(openWorksheetEvent, controllers)
      SUtil.setAdditionalDatasource(admin)
      worksheetResource.initRuntimeWS(admin)

      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(admin)
      AssetQuerySandbox assetQuerySandbox = runtimeWorksheet.getAssetQuerySandbox()
      Assembly[] assemblies = runtimeWorksheet.getWorksheet().getAssemblies()

      params.each {
         assetQuerySandbox.getVariableTable().put(it.key, it.value)
      }
      try {
         assemblies.each {
            if(it.getAssemblyType() == Worksheet.TABLE_ASSET) {
               TableAssembly tableAssembly = (TableAssembly) it
               String tableName = tableAssembly.getName()
               if(!tableAssembly.isVisibleTable()) {
                  return
               }
               setLiveData(tableAssembly)

               TableLens lens = assetQuerySandbox.getTableLens(tableName, AssetQuerySandbox.LIVE_MODE)
               // sort lens to void row sort issue.
               SortFilter sortlens = new SortFilter(lens)
               String fileName = createFileByCase(asset_id, tableName)
               exportUtil.exportWSObject(fileName, sortlens)
            }
         }
      }
      catch(Exception e) {
         e.printStackTrace()
      }
      finally {
         controllers.destroy()
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

   /**
    * execute vs, then save chart as png, table as txt
    * @param asset_id
    * @param bks
    * @param params
    * @return
    */
   def executeVSWithElement(String asset_id, String[] bks, Map<String, String[]> params) {
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      bks.each {
         executeVSWithElement0(asset_id, it, params)
      }
   }

   /**
    * execute vs, then save chart as png, table as txt
    */
   def executeVSWithElement0(String asset_id, String bk, Map<String, String[]> params) {
      DataSpace.getDataSpace()
      controllers.initControllers()
      viewsheetResource =
              new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(admin)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(admin)
      rvs.gotoBookmark(bk, admin.getUser().getUserIdentity(), admin)
      Optional<ViewsheetSandbox> sandboxOpt = rvs.getViewsheetSandbox()
      if(sandboxOpt.isPresent()) {
         sandboxOpt.get().resetAll(new ChangedAssemblyList())
      }

      ViewsheetSandbox sandbox = sandboxOpt.get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      String assemblyName
      File outFile
      try {
         assemblies.each {
            assemblyName = it.getName()
            if((it instanceof VSAssembly && !it.getVSAssemblyInfo().isVisible(true)) ||
                    it instanceof GroupContainerVSAssembly || it instanceof TabVSAssembly ||
                    it instanceof CurrentSelectionVSAssembly || it instanceof ShapeVSAssembly) {
               return true
            }
            data = sandbox.getData(assemblyName, true, DataMap.NORMAL)
            if(it instanceof ChartVSAssembly) {
               final VGraphPair pair = sandbox.getVGraphPair(assemblyName, true, null, true, 1)
               data = pair.getData()
               if(data instanceof BrushDataSet || data instanceof GeoDataSet || data instanceof MappedDataSet) {
                  data = ((DataSetFilter) data).getDataSet()
               }
               BufferedImage image = pair.getImage(true, 72)
               outFile = createVSElementFile(assemblyName, bk, '.png')
               exportUtil.exportVSObject(outFile.toString(), image)

               outFile = createVSElementFile(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), data)
            }
            if(it instanceof TableDataVSAssembly) {
               data = new SortFilter(sandbox.getVSTableLens(assemblyName, false))
               outFile = createVSElementFile(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), data)
            }
         }
      }
      catch(Exception e) {
         e.printStackTrace()
      }
   }

   /**
    * create export file for VS element
    */
   def createVSElementFile(String assemblyName, String bk, String suffix) {
      String fileName = 'VS' + File.separator + assemblyName + (bk == '(Home)' ? '' : '_' + bk) + suffix
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      File tempFile = new File(resourcePath + '/exportData' + suiteName + File.separator + caseName +
              File.separator + fileName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * create out put file for ws with txt, report with svg
    * @param asset_id
    * @param tableName , only for ws
    * @return
    */
   def createFileByCase(String asset_id, tableName) {
      String fileName
      if(asset_id.startsWith '1^2^') {
         fileName = 'WS' + File.separator + tableName + '.txt'
      }
      else {
         new Exception('---the asset id name not right--: ').printStackTrace()
      }

      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      File tempFile = new File(resourcePath + '/exportData' + suiteName + File.separator + caseName +
              File.separator + fileName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * compare all text file
    */
   void compareData() {
      compareData(null)
   }

   /**
    * compare text file
    * @param fileNames txt file names
    */
   void compareData(String[] fls) {
      ['VS', 'WS'].each {
         compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName + '/' + it, 'TXT')
      }
   }

   /**
    * compare all image file
    */
   void compareImage() {
      compareImage(null)
   }

   /**
    * compare the image files
    * @param fls image names
    */
   void compareImage(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName + '/VS', 'PNG')
   }

   /**
    * compare image with allowed diff pixel percent
    */
   void compareImageWithAllowedDiffPixel(def allowedPixelPercent) {
      compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName + '/VS', 'PNG', allowedPixelPercent)
   }

   /**
    * compare html file for vs exported as html
    */
   void compareHTML() {
      compareImage(null)
   }

   /**
    * compare html file for vs exported as html
    */
   void compareHTML(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName + '/VS', 'HTML')
   }

   static String caseName, suiteName
   static ConfigurationContext context
   RuntimeViewsheetResource viewsheetResource
   RuntimeWorksheetResource worksheetResource
   ControllersResource controllers = new ControllersResource()
   ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   TUtil tutil = new TUtil()
   SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])

}

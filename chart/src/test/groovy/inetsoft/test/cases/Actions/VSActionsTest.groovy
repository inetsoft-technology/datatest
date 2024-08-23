package inetsoft.test.cases.Actions

import inetsoft.report.internal.table.FormatTableLens2
import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent
import inetsoft.web.viewsheet.event.chart.VSChartShowDetailsEvent
import inetsoft.web.viewsheet.service.ExportResponse

class VSActionsTest {
   VSActionsTest(String asset_id, String caseName) {
      this.asset_id = asset_id
      this.caseName = caseName
   }

   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1? null : arrs[1].replace('.', '/')
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   /**
    * Init runtime VS
    * @param params
    * @return
    */
   def initVS(Map<String, String[]> params) {
      DataSpace.getDataSpace() //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      ThreadContext.setContextPrincipal(principal)
      ControllersResource controllers = new ControllersResource()
      controllers.initControllers()
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * show detail on chart, then save as data to txt
    */
   def testShowDetailOnChart(Map<String, String[]> params, def objs) {
      initVS(params)
      objs.eachWithIndex { it, idx ->
         VSChartShowDetailsEvent event = new VSChartShowDetailsEvent()
         event.setChartName(it.chartName)
         event.setWorksheetId(null)
         event.setColumn(it.colIdx)
         event.setRangeSelection(it.isRange)
         event.setSelected(it.selected)

         FormatTableLens2 formatTableLens2 = viewsheetResource.showDetailOnChart(event, it.chartName,  principal)

         def outFile = createExportFileByCase(it.chartName, idx ,'_SHOWDETAIL.txt')
         exportUtil.exportVSObject(outFile.toString(), formatTableLens2)
      }
   }

   /**
    * do brush on chart, then export vs as png
    */
   def testBrushOnChart(Map<String, String[]> params, def objs) {
      initVS(params)
      objs.eachWithIndex { it, idx ->
         VSChartBrushEvent event = new VSChartBrushEvent()
         event.setChartName(it.chartName)
         event.setRangeSelection(it.isRange)
         event.setSelected(it.selected)

         viewsheetResource.brushOnChart(event, principal)

         File pngFile = createExportFileByCase(it.chartName, idx,'_BRUSH.png')
         OutputStream out = new FileOutputStream(pngFile)
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                 false, false, false, false,
                 ['Home'] as String[], false, false, null, new ExportResponse(out), principal)
      }
   }

   /**
    * create export file by case name
    * @param caseName
    * @param suffix
    * @return
    */
   def createExportFileByCase(String assemblyName, def idx, String suffix) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator  + caseName
      File tempFile = new File(fileName + File.separator + assemblyName + '_' + idx + suffix)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()) {
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
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'TXT')
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
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'PNG')
   }

   private static String asset_id, suiteName, caseName
   RuntimeViewsheetResource viewsheetResource

   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   TUtil tUtil = new TUtil()

   SRPrincipal principal = tUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])
}

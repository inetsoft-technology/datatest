package inetsoft.test.cases.Actions

import inetsoft.report.internal.table.FormatTableLens2
import inetsoft.test.core.ExportUtil
import inetsoft.test.modules.ViewsheetTest
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent
import inetsoft.web.viewsheet.event.chart.VSChartShowDetailsEvent
import inetsoft.web.viewsheet.service.ExportResponse

class VSActionsTest extends ViewsheetTest {
   VSActionsTest(String asset_id, String caseName) {
      super(asset_id, caseName)
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

         FormatTableLens2 formatTableLens2 = viewsheetResource.showDetailOnChart(event, it.chartName, principal)

         def outFile = createExportFileByCase(it.chartName, idx, '_SHOWDETAIL.txt')
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

         File pngFile = createExportFileByCase(it.chartName, idx, '_BRUSH.png')
         OutputStream out = new FileOutputStream(pngFile)
         try {
            viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                    false, true, false, false,
                    null, false, false, null, new ExportResponse(out), principal)
         }
         finally {
            out.close()
         }
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
      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator + caseName
      File tempFile = new File(fileName + File.separator + assemblyName + '_' + idx + suffix)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
   }

   ExportUtil exportUtil = new ExportUtil()
}

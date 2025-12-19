package inetsoft.test.modules

import inetsoft.report.io.csv.CSVConfig
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile

class VSExportTest {
   VSExportTest(String asset_id, String caseName) {
      this.asset_id = asset_id
      this.caseName = caseName
   }

   /**
    * init sree home
    * @return
    */
   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1? null : arrs[1].replace('.', '/')
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   /**
    * Test export viewsheet to HTML format
    * @param bks bookmark names array, null will use default '(Home)'
    * @param params parameters map for viewsheet execution
    */
   def testExportAsHtml(String[] bks, Map<String, String[]> params) {
      executeVS(params)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }

      File outFile = createFileByCase(caseName, '.html')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_HTML, true,
                 false, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      } finally {
         out.close()
      }
   }

   /**
    * Test Export VS to PNG
    * @param bks
    * @param match
    * @param expandSelection
    * @return
    */
   def testExportAsPNG(String[] bks, Map<String, String[]> params, Boolean match, Boolean expandSelection) {
      executeVS(params)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }

      File outFile = createFileByCase(caseName, '.png')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, match,
                 expandSelection, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      } finally {
         out.close()
      }
   }

   /**
    * export VS as CSV
    * @param bks
    * @param params
    * @param csvConfig
    * @return
    */
   def testExportAsCSV(String[] bks, Map<String, String[]> params, CSVConfig csvConfig) {
      executeVS(params)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }

      File outFile = createFileByCase(caseName, '.zip')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_CSV, false, false, false,
                 false, false, bks, false, false, csvConfig,
                 new ExportResponse(out), principal)
      } finally {
         out.close()
      }

      unzipFile(outFile)
   }

   /**
    * Test Export VS to PDF with print layout
    * @param bks
    * @param match
    * @param expandSelection
    * @return
    */
   def testExportWithPrintLayout(String[] bks, Map<String, String[]> params) {
      executeVS(params)
      if(bks == null) {
         bks = ['Home'] as String[]
      }

      File outFile = createFileByCase(caseName, '.pdf')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true,
                 false, false, true, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      } finally {
         out.close()
      }

      Thread.sleep(PDF_EXPORT_WAIT_MS)
      tUtil.convertPDFToPNG(outFile.toString())
   }

   /**
    * unzip zip file to current export file.
    * @param outFile
    * @return
    */
   def unzipFile(File outFile) {
      String fileFolder = outFile.getParent().toString() + File.separator
      //clear all csv file
      new File(fileFolder).listFiles().each {
         if (it.path.endsWith('.csv')) {
            it.delete()
         }
      }
      def zipFile = new ZipFile(outFile)

      zipFile.entries().each { it ->
         def path = Paths.get(fileFolder + it.name)
         Files.copy(zipFile.getInputStream(it), path)
      }
   }

   /**
    * Execute the VS
    * @param params
    * @return
    */
   def executeVS(Map<String, String[]> params) {
      DataSpace.getDataSpace() //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      controllers.initControllers()
      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent (params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * create export file by case name
    * @param caseName
    * @param suffix
    * @return
    */
   def createFileByCase(String caseName, String suffix) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String fileName = resourcePath + '/exportData' + suiteName
      File tempFile = new File(fileName + File.separator  + caseName  + File.separator + caseName + suffix)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()){
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * compare all text file
    */
   void compareCSV() {
      compareCSV(null)
   }

   /**
    * compare text file
    * @param fileNames txt file names
    */
   void compareCSV(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'CSV')
   }

   /**
    * compare all image file
    */
   void comparePNG() {
      comparePNG(null)
   }

   /**
    * compare the image files
    * @param fls image names
    */
   void comparePNG(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'PNG')
   }

   /**
    * compare HTML file
    */
   void compareHTML() {
      compareHTML(null)
   }

   /**
    * compare the HTML files
    * @param fls HTML names
    */
   void compareHTML(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'HTML')
   }

   private static final int PDF_EXPORT_WAIT_MS = 2000

   static String asset_id, suiteName, caseName
   RuntimeViewsheetResource viewsheetResource
   SRPrincipal principal = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])
   ControllersResource controllers = new ControllersResource()
   ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   CompareUtil compareUtil = new CompareUtil()
   TUtil tUtil = new TUtil()
}

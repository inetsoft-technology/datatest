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
    * Initialize sree home directory
    * @param suiteName the suite name to initialize
    */
   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1 ? null : arrs[1].replace('.', '/')
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   /**
    * Test export viewsheet to HTML format
    * @param bks bookmark names array, null will use default '(Home)'
    * @param params parameters map for viewsheet execution
    */
   def testExportAsHtml(String[] bks, Map<String, String[]> params) {
      executeVS(params)
      bks = normalizeBookmarks(bks)

      File outFile = createFileByCase(caseName, '.html')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_HTML, true,
                 false, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }
      finally {
         out.close()
      }
   }

   /**
    * Test export viewsheet to PNG format
    * @param bks bookmark names array, null will use default '(Home)'
    * @param params parameters map for viewsheet execution
    * @param match whether to match the export
    * @param expandSelection whether to expand selection
    */
   def testExportAsPNG(String[] bks, Map<String, String[]> params, Boolean match, Boolean expandSelection) {
      executeVS(params)
      bks = normalizeBookmarks(bks)

      File outFile = createFileByCase(caseName, '.png')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, match,
                 expandSelection, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }
      finally {
         out.close()
      }
   }

   /**
    * Test export viewsheet as CSV format
    * @param bks bookmark names array, null will use default '(Home)'
    * @param params parameters map for viewsheet execution
    * @param csvConfig CSV configuration object
    */
   def testExportAsCSV(String[] bks, Map<String, String[]> params, CSVConfig csvConfig) {
      executeVS(params)
      bks = normalizeBookmarks(bks)

      File outFile = createFileByCase(caseName, '.zip')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_CSV, false, false, false,
                 false, false, bks, false, false, csvConfig,
                 new ExportResponse(out), principal)
      }
      finally {
         out.close()
      }

      unzipFile(outFile)
   }

   /**
    * Test export viewsheet to PDF with print layout
    * @param bks bookmark names array, null will use default '(Home)'
    * @param params parameters map for viewsheet execution
    */
   def testExportWithPrintLayout(String[] bks, Map<String, String[]> params) {
      executeVS(params)
      bks = normalizeBookmarks(bks)

      File outFile = createFileByCase(caseName, '.pdf')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true,
                 false, false, true, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }
      finally {
         out.close()
      }

      Thread.sleep(PDF_EXPORT_WAIT_MS)
      tUtil.convertPDFToPNG(outFile.toString())
   }

   /**
    * Unzip zip file to current export file directory
    * @param outFile the zip file to unzip
    * @throws IOException if file operations fail
    */
   def unzipFile(File outFile) {
      if(outFile == null || !outFile.exists()) {
         throw new IllegalArgumentException("Output file is null or does not exist: ${outFile}")
      }

      try {
         String fileFolder = outFile.getParent().toString() + File.separator
         // Clear all csv files
         File folder = new File(fileFolder)
         if(folder.exists() && folder.isDirectory()) {
            folder.listFiles()?.each {
               if(it.path.endsWith('.csv')) {
                  it.delete()
               }
            }
         }

         new ZipFile(outFile).withCloseable { ZipFile zipFile ->
            zipFile.entries().each { entry ->
               if(!entry.isDirectory()) {
                  def path = Paths.get(fileFolder + entry.name)
                  Files.createDirectories(path.parent)
                  Files.copy(zipFile.getInputStream(entry), path)
               }
            }
         }
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to unzip file: ${outFile}", e)
      }
   }

   /**
    * Execute the viewsheet with given parameters
    * @param params parameters map for viewsheet execution, can be null
    */
   def executeVS(Map<String, String[]> params) {
      try {
         DataSpace.getDataSpace() //after upgrade storage, need get first to get dataspace, then to get indexstorage.
         controllers.initControllers()
         ThreadContext.setContextPrincipal(principal)
         viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
         viewsheetResource.initRuntimeVS(principal)
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to execute viewsheet", e)
      }
   }

   /**
    * Create export file by case name
    * @param caseName the case name
    * @param suffix the file suffix (e.g., '.xlsx', '.png')
    * @return the created file object
    * @throws IOException if file creation fails
    */
   def createFileByCase(String caseName, String suffix) {
      try {
         String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
         String fileName = resourcePath + '/exportData' + suiteName
         File tempFile = new File(fileName + File.separator + caseName + File.separator + caseName + suffix)

         if(!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs()
         }
         else if(tempFile.exists()) {
            tempFile.delete()
         }
         return tempFile
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to create file by case: ${caseName}${suffix}", e)
      }
   }

   /**
    * Normalize bookmark array, return default if null
    * @param bks bookmark names array
    * @return normalized bookmark array
    */
   private String[] normalizeBookmarks(String[] bks) {
      return bks == null ? (['(Home)'] as String[]) : bks
   }

   /**
    * Compare all CSV text files
    */
   void compareCSV() {
      compareCSV(null)
   }

   /**
    * Compare CSV text files
    * @param fileNames array of CSV file names to compare, null to compare all
    */
   void compareCSV(String[] fileNames) {
      compareUtil.CompareFileByFeature(fileNames, suiteName + '/' + caseName, 'CSV')
   }

   /**
    * Compare all PNG image files
    */
   void comparePNG() {
      comparePNG(null)
   }

   /**
    * Compare PNG image files
    * @param fileNames array of PNG file names to compare, null to compare all
    */
   void comparePNG(String[] fileNames) {
      compareUtil.CompareFileByFeature(fileNames, suiteName + '/' + caseName, 'PNG')
   }

   /**
    * Compare all HTML files
    */
   void compareHTML() {
      compareHTML(null)
   }

   /**
    * Compare HTML files
    * @param fileNames array of HTML file names to compare, null to compare all
    */
   void compareHTML(String[] fileNames) {
      compareUtil.CompareFileByFeature(fileNames, suiteName + '/' + caseName, 'HTML')
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

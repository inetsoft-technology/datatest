package inetsoft.test.modules

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * ViewsheetTest for the date comparison specs. Differences from ViewsheetTest:
 * <ul>
 *   <li>an export failure fails the test instead of only printing the stack trace;</li>
 *   <li>when no file names are given, every baseline file under expectData must have been
 *   exported by this run (ViewsheetTest only compares the files that were exported);</li>
 *   <li>with -Dupdate.baseline=true the exported files are copied into the module's
 *   src/test/resources/expectData instead of being compared.</li>
 * </ul>
 */
class DcViewsheetTest extends ViewsheetTest {
   DcViewsheetTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   @Override
   protected void onExportFailure(String message, Exception ex) {
      throw new RuntimeException(message + " " + asset_id + " [" + caseName + "]", ex)
   }

   /**
    * ViewsheetTest.exportAsPDF defaults to the bookmark 'Home', which no longer exists
    * (the home bookmark is '(Home)'), so the exported PDF has no pages and no <name>-N.png
    * is rendered from it.
    */
   @Override
   def exportAsPDF(Map<String, String[]> params, String[] bks) {
      super.exportAsPDF(params, bks == null ? ['(Home)'] as String[] : bks)
   }

   @Override
   void compareData(String[] fls) {
      if(prepareCompare(fls, '.txt')) {
         super.compareData(fls)
      }
   }

   @Override
   void compareImage(String[] fls) {
      if(prepareCompare(fls, '.png')) {
         super.compareImage(fls)
      }
   }

   /**
    * @return true if the files should be compared, false if the baseline was updated instead
    */
   private boolean prepareCompare(String[] fls, String suffix) {
      if(Boolean.getBoolean('update.baseline')) {
         updateBaseline(fls, suffix)
         return false
      }

      if(fls == null) {
         def missing = baselineNames(suffix).findAll { !new File(exportDir, it).exists() }

         if(missing) {
            throw new AssertionError("\nBaseline files not exported by this run (" + suiteName +
               '/' + caseName + "):\n" + missing.join("\n") + "\n")
         }
      }

      return true
   }

   private void updateBaseline(String[] fls, String suffix) {
      List<String> names = fls != null ? fls.collect { it + suffix } : exportedNames(suffix)
      File target = new File(sourceBaselineRoot, suiteName + File.separator + caseName)
      target.mkdirs()

      // png rendering differs between platforms: only write a missing png baseline unless
      // -Dupdate.baseline.png=true
      boolean overwrite = suffix != '.png' || Boolean.getBoolean('update.baseline.png')

      names.each {
         File src = new File(exportDir, it)
         File dest = new File(target, it)
         if(!src.exists()) {
            throw new AssertionError("Cannot update baseline, file was not exported: " + src)
         }

         if(overwrite || !dest.exists()) {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
         }
      }
   }

   private List<String> baselineNames(String suffix) {
      return namesIn(new File(testClassesDir, 'expectData' + File.separator + suiteName +
         File.separator + caseName), suffix)
   }

   private List<String> exportedNames(String suffix) {
      return namesIn(exportDir, suffix)
   }

   private static List<String> namesIn(File dir, String suffix) {
      File[] files = dir.listFiles()

      if(files == null) {
         return []
      }

      return files.findAll {
         it.isFile() && it.name.endsWith(suffix) && !it.name.endsWith('_diff.png')
      }.collect { it.name }
   }

   private File getTestClassesDir() {
      return new File(this.class.getResource('/expectData').getPath()).getParentFile()
   }

   private File getExportDir() {
      return new File(testClassesDir, 'exportData' + File.separator + suiteName +
         File.separator + caseName)
   }

   /**
    * module/target/test-classes -> module/src/test/resources/expectData
    */
   private File getSourceBaselineRoot() {
      return new File(testClassesDir.parentFile.parentFile,
         'src' + File.separator + 'test' + File.separator + 'resources' + File.separator +
            'expectData')
   }
}

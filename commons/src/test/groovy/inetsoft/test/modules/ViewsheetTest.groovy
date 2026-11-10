package inetsoft.test.modules

import inetsoft.graph.data.AbstractDataSet
import inetsoft.graph.data.DataSet
import inetsoft.graph.data.DataSetFilter
import inetsoft.graph.data.FullProjectedDataSet
import inetsoft.graph.data.PairsDataSet
import inetsoft.graph.data.SortedDataSet
import inetsoft.graph.geo.GeoDataSet
import inetsoft.graph.geo.MappedDataSet
import inetsoft.report.composition.graph.BrushDataSet
import inetsoft.report.composition.graph.IntervalDataSet
import inetsoft.report.composition.graph.VGraphPair
import inetsoft.report.composition.graph.VSDataSet
import inetsoft.sree.security.SRPrincipal
import inetsoft.sree.internal.SUtil
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.uql.asset.Assembly
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.viewsheet.ChartVSAssembly
import inetsoft.uql.viewsheet.CurrentSelectionVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.GroupContainerVSAssembly
import inetsoft.uql.viewsheet.ShapeVSAssembly
import inetsoft.uql.viewsheet.TabVSAssembly
import inetsoft.uql.viewsheet.TableDataVSAssembly
import inetsoft.uql.viewsheet.VSAssembly
import inetsoft.uql.viewsheet.graph.ChartAggregateRef
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.TUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.DatatestSpringRuntimeInitializer


import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Verification is strict:
 * <ul>
 *   <li>an export failure fails the test; when one assembly fails, the remaining assemblies are
 *   still exported and all failures are reported together;</li>
 *   <li>when compareData/compareImage get no file names, every baseline file under expectData
 *   must have been exported by this run.</li>
 * </ul>
 * With -Dupdate.baseline=true the exported files are copied into the module's
 * src/test/resources/expectData instead of being compared.
 */
class ViewsheetTest {
   //static ConfigurationContext context
   private static Object initializedApplicationContext
   // export folders already cleaned in this JVM, so several instances of one case keep their files.
   // A case that runs twice in one JVM is only cleaned before its first run.
   private static final Set<String> cleanedExportDirs = Collections.synchronizedSet(new HashSet<String>())

   ViewsheetTest(String asset_id, String caseName) {
      ensureRuntimeInitialized()
      this.asset_id = asset_id
      this.caseName = caseName
      cleanExportDir()
   }

   /**
    * Fail the test with a verification problem of this case.
    */
   protected void violation(String message, Throwable cause = null) {
      throw new AssertionError(message + ' ' + asset_id + ' [' + suiteName + '/' + caseName + ']',
         cause)
   }

   /**
    * Remove the files of a previous run of this case, otherwise a stale export hides a file that
    * this run failed to export. Done once per case and JVM.
    */
   private void cleanExportDir() {
      // without expectData on the classpath or a suite (initHome not called) there is no export
      // folder of this case to clean; exporting reports the problem later
      if(suiteName == null || caseName == null || this.class.getResource('/expectData') == null) {
         return
      }

      File dir = exportFolder()

      if(cleanedExportDirs.add(dir.absolutePath) && dir.exists()) {
         dir.listFiles()?.each { if(it.isFile()) it.delete() }
      }
   }

   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home") + "\n")
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1 ? null : arrs[1].replace('.', '/')
   }

   private static synchronized ensureRuntimeInitialized() {
      ConfigurationContext context = ConfigurationContext.getContext()
      def currentContext = DatatestSpringRuntimeInitializer.ensureInitialized(
         System.getProperty('sree.home', '.'))

      DataSpace.getDataSpace()
      initializedApplicationContext = currentContext
   }

   /**
    * Init runtime VS
    * @return
    */
   def initVS() {
      initVS(null)
   }

   /**
    * Init runtime VS
    * @param params
    * @return
    */
   def initVS(Map<String, String[]> params) {
      initVS(params, true)
   }

   /**
    * Init runtime VS
    * @param params
    * @param isViewer
    * @return
    */
   def initVS(Map<String, String[]> params, Boolean isViewer) {
      ensurePrincipal()
      ThreadContext.setContextPrincipal(principal)
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id, isViewer))
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * execute vs
    * @param params
    * @param bks eg: ['Home', 'brush1', 'brush2', 'exclude1', 'zoom'] as String[]
    * @return
    */
   def executeVS(Map<String, String[]> params, String[] bks) {
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      bks.each {
         executeVS0(params, it)
      }
   }

   /**
    * execute vs
    * @param params
    * @param bk
    * @return
    */
   def executeVS0(Map<String, String[]> params, String bk) {
      initVS(params)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      rvs.gotoBookmark(bk, principal.getUser().getUserIdentity(), principal)
      Optional<ViewsheetSandbox> sandboxOpt = rvs.getViewsheetSandbox()

      if(!sandboxOpt.isPresent()) {
         throw new IllegalStateException("No viewsheet sandbox for " + asset_id + ", bookmark " + bk)
      }

      ViewsheetSandbox sandbox = sandboxOpt.get()
      sandbox.resetAll(new ChangedAssemblyList())
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()
      Map<String, Exception> failures = new LinkedHashMap<>()

      assemblies.each {
         String assemblyName = it.getName()
         //Ignore, and unexport assembly data when assembly is shape,group,selectionContainer, tab
         if((it instanceof VSAssembly && !it.getVSAssemblyInfo().isVisible(true)) ||
                 it instanceof GroupContainerVSAssembly || it instanceof TabVSAssembly ||
                 it instanceof CurrentSelectionVSAssembly || it instanceof ShapeVSAssembly) {
            return
         }

         // export each assembly on its own, a failure must not skip the remaining assemblies
         try {
            def data
            File outFile
            if(it instanceof ChartVSAssembly) {
               final VGraphPair pair = sandbox.getVGraphPair(assemblyName, true, null, true, 1)
               data = getVSChartDataSet(pair)
               //GDebug.printDataSet(data, 0)

               BufferedImage image = pair.getImage(true, 72)
               outFile = createExportFileByCase(assemblyName, bk, '.png')
               exportUtil.exportVSObject(outFile.toString(), image)

               outFile = createExportFileByCase(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), sortVSDataSet(data))
            }
            else if(it instanceof TableDataVSAssembly) {
               data = sandbox.getVSTableLens(assemblyName, false)
               outFile = createExportFileByCase(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), data)
            }
         }
         catch(Exception ex) {
            failures.put(assemblyName, ex)
         }
      }

      if(!failures.isEmpty()) {
         Exception ex = new Exception("assemblies failed (bookmark " + bk + "): " +
            failures.collect { k, v -> k + ": " + v }.join("; "))
         failures.values().each { ex.addSuppressed(it) }
         onExportFailure("==========execute viewsheet failed:===========", ex)
      }
   }

   /**
    * Called when an export step throws: fails the test.
    */
   protected void onExportFailure(String message, Exception ex) {
      violation(message, ex)
   }

   /**
    * getVSChart dataset. code from VSChartShowDataController.java, see applyChanges() function
    */
   protected DataSet getVSChartDataSet(VGraphPair vgraphpair) {
      DataSet data = vgraphpair.getData()
      DataSet filter = data
      while(true) {
         if(filter instanceof VSDataSet) {
            break
         }
         else if(filter instanceof PairsDataSet) {
            // advance filter too, otherwise this branch is taken forever (same as VSChartShowDataService)
            data = filter = ((PairsDataSet) filter).getDataSet()
         }
         else if(filter instanceof DataSetFilter) {
            boolean useBase = filter instanceof BrushDataSet ||
                    filter instanceof GeoDataSet ||
                    filter instanceof MappedDataSet
            filter = ((DataSetFilter) filter).getDataSet()

            if(useBase) {
               data = filter
            }
         }
         else {
            break
         }
      }
      // trend/comparison as discrete measure
      boolean discreteCalc = data instanceof VSDataSet &&
              Arrays.stream(((VSDataSet) data).getDataRefs())
                      .filter(ref -> ref instanceof ChartAggregateRef)
                      .anyMatch(ref -> ((ChartAggregateRef) ref).isDiscrete() &&
                              ((ChartAggregateRef) ref).getCalculator() != null);
      if(((AbstractDataSet) data).getRowsProjectedForward() > 0 || !discreteCalc ||
              // need to wrap sub-dataset inside IntervalDataSet. (57229)
              data instanceof IntervalDataSet) {
         data = ((AbstractDataSet) data).getFullProjectedDataSet()
      }

      return data
   }

   /**
    * sort dataset, make sure it show same with show data.
    * see line 255 of VSChartShowDataController.java
    */
   protected DataSet sortVSDataSet(DataSet vsDataSet) {
      DataSet sorted
      if(vsDataSet instanceof FullProjectedDataSet) {
         sorted = new FullProjectedDataSet(getSortedSubdataSets((FullProjectedDataSet) vsDataSet))
      }
      else {
         // apply ValueOrder sorting to the data set
         SortedDataSet sorted0 = new SortedDataSet(vsDataSet)
         for(int i = 0; i < vsDataSet.getColCount(); i++) {
            sorted0.addSortColumn(vsDataSet.getHeader(i), false)
         }
         sorted = sorted0
      }

      return sorted
   }

   protected List<AbstractDataSet> getSortedSubdataSets(FullProjectedDataSet dset) {
      List<AbstractDataSet> subDataSets = new ArrayList<>()

      for(AbstractDataSet subSet : dset.getSubDataSets()) {
         // apply ValueOrder sorting to each sub data set
         SortedDataSet sortedSubSet = new SortedDataSet(subSet)

         for(int i = 0; i < subSet.getColCount(); i++) {
            String header = subSet.getHeader(i)
            sortedSubSet.addSortColumn(header, false)
         }

         subDataSets.add(sortedSubSet)
      }

      return subDataSets
   }


   /**
    * export vs as PNG, you can hide other assembly by set 'HideOnExport as true'
    * @param params
    * @param bks
    * @return
    */
   def exportAsPNG(Map<String, String[]> params, String[] bks) {
      exportAsPNG(bks, params, true, false)
   }

   /**
    * export vs as PNG
    * @param bks
    * @param params
    * @param match
    * @param expandSelection
    * @return
    */
   def exportAsPNG(String[] bks, Map<String, String[]> params, Boolean match, Boolean expandSelection) {
      initVS(params)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      File outFile = createExportFileByCase(null, null, '.png')
      OutputStream out = new FileOutputStream(outFile)

      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, match,
                 expandSelection, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }
      catch(Exception ex) {
         onExportFailure("==========export PNG failed:===========", ex)
      }
      finally {
         out.close()
      }
   }

   /**
    * Test Export VS to PDF
    * if have printlayout, export with printlayout.
    * @param bks
    * @param match
    * @param expandSelection
    * @return
    */
   def exportAsPDF(Map<String, String[]> params, String[] bks) {
      initVS(params)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      File outFile = createExportFileByCase(null, null, '.pdf')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true,
                 false, false, true, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }
      catch(Exception ex) {
         onExportFailure("==========export PDF failed:===========", ex)
      }
      finally {
         out.close()
      }

      Thread.sleep(500)
      tUtil.convertPDFToPNG(outFile.toString())
   }


   /**
    * create export file by case name
    * @param caseName
    * @param suffix
    * @return
    */
   def createExportFileByCase(String assemblyName, String bk, String suffix) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String vsPath = asset_id.substring(asset_id.lastIndexOf('^') + 1)
      String vsName = vsPath.substring(vsPath.lastIndexOf('/') + 1)
      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator + caseName
      File tempFile = new File(fileName + File.separator +
              ((!['Home', '(Home)', null].contains(bk)) ? (bk + '_') : '') +
              (assemblyName != null ? assemblyName : vsName) +
              suffix)

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
      if(checkBaselineExported(fls, '.txt')) {
         compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'TXT')
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
      if(checkBaselineExported(fls, '.png')) {
         compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'PNG')
      }
   }

   /**
    * compare image with allowed diff pixel percent
    */
   void compareImageWithAllowedDiff() {
      compareImageWithAllowedDiffPixel(0.01)
   }

   void compareImageWithAllowedDiffPixel(def allowedPixelPercent) {
      if(checkBaselineExported(null, '.png')) {
         compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName, 'PNG', allowedPixelPercent)
      }
   }

   /**
    * @return true if the files should be compared, false if the baseline was updated instead
    */
   protected boolean checkBaselineExported(String[] fls, String suffix) {
      if(Boolean.getBoolean('update.baseline')) {
         writeBaseline(fls, suffix)
         // nothing is compared in this mode, make that visible in the log
         System.err.println("update.baseline=true: " + suffix + " baseline of " + suiteName + '/' +
            caseName + " updated, NOT compared")
         return false
      }

      if(fls == null) {
         def missing = listNames(baselineFolder(), suffix).findAll { !new File(exportFolder(), it).exists() }

         if(missing) {
            violation("Baseline files not exported by this run:\n" + missing.join("\n") + "\n")
         }
      }

      return true
   }

   private void writeBaseline(String[] fls, String suffix) {
      List<String> names = fls != null ? fls.collect { it + suffix } : listNames(exportFolder(), suffix)
      File target = new File(sourceBaselineFolder(), suiteName + File.separator + caseName)
      target.mkdirs()

      // png rendering differs between platforms: only write a missing png baseline unless
      // -Dupdate.baseline.png=true
      boolean overwrite = suffix != '.png' || Boolean.getBoolean('update.baseline.png')

      names.each {
         File src = new File(exportFolder(), it)
         File dest = new File(target, it)

         if(!src.exists()) {
            throw new AssertionError("Cannot update baseline, file was not exported: " + src)
         }

         if(overwrite || !dest.exists()) {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
         }
      }
   }

   private static List<String> listNames(File dir, String suffix) {
      File[] files = dir.listFiles()

      if(files == null) {
         return []
      }

      return files.findAll {
         it.isFile() && it.name.endsWith(suffix) && !it.name.endsWith('_diff.png')
      }.collect { it.name }
   }

   protected File classesFolder() {
      return new File(this.class.getResource('/expectData').getPath()).getParentFile()
   }

   protected File exportFolder() {
      return new File(classesFolder(), 'exportData' + File.separator + suiteName +
         File.separator + caseName)
   }

   protected File baselineFolder() {
      return new File(classesFolder(), 'expectData' + File.separator + suiteName +
         File.separator + caseName)
   }

   /**
    * module/target/test-classes -> module/src/test/resources/expectData
    */
   protected File sourceBaselineFolder() {
      return new File(classesFolder().parentFile.parentFile,
         'src' + File.separator + 'test' + File.separator + 'resources' + File.separator +
            'expectData')
   }

   /**
    * SRPrincipal triggers XSessionService via Spring; runtime initialization must run
    * before principal construction so the ApplicationContext is available.
    */
   protected void ensurePrincipal() {
      if(principal == null) {
         principal = tUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
                 new String[0])
      }
   }

   protected static String asset_id, suiteName, caseName
   RuntimeViewsheetResource viewsheetResource

   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   TUtil tUtil = new TUtil()

   SRPrincipal principal
}

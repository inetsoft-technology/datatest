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
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.uql.asset.Assembly
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
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.CompareUtil


import java.awt.image.BufferedImage

class ViewsheetTest {
   //static ConfigurationContext context
   static ControllersResource controllers

   ViewsheetTest(String asset_id, String caseName) {
      this.asset_id = asset_id
      this.caseName = caseName
   }

   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      //println '----suitename--------' + suiteName
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1 ? null : arrs[1].replace('.', '/')

      /*context = ConfigurationContext.getContext()
      context.setHome(System.getProperty("sree.home"))*/

      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }
   /**
    * Init runtime VS
    * @param params
    * @return
    */
   def initVS(Map<String, String[]> params) {
      DataSpace.getDataSpace()  //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      controllers = new ControllersResource()
      controllers.initControllers()

      ThreadContext.setContextPrincipal(principal)
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * execute vs
    * @param params
    * @param bks eg: ['Home', 'brush1', 'brush2', 'exclude1', 'zoom'] as String[]
    * @return
    */
   def executeVS(Map<String, String[]> params, String[] bks) {
      if (bks == null) {
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
      rvs.getViewsheetSandbox().resetAll(new ChangedAssemblyList())

      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      String assemblyName = ''
      File outFile
      try {
         assemblies.each {
            assemblyName = it.getName()
            //Ignore, and unexport assembly data when assembly is shape,group,selectionContainer, tab
            if ((it instanceof VSAssembly && !it.getVSAssemblyInfo().isVisible(true)) ||
                    it instanceof GroupContainerVSAssembly || it instanceof TabVSAssembly ||
                    it instanceof CurrentSelectionVSAssembly || it instanceof ShapeVSAssembly) {
               return true
            }
            if (it instanceof ChartVSAssembly) {
               final VGraphPair pair = sandbox.getVGraphPair(assemblyName, true, null, true, 1)
               data = getVSChartDataSet(pair)
               //GDebug.printDataSet(data, 0)

               BufferedImage image = pair.getImage(true, 72)
               outFile = createExportFileByCase(assemblyName, bk, '.png')
               exportUtil.exportVSObject(outFile.toString(), image)

               outFile = createExportFileByCase(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), sortVSDataSet(data))
            } else if (it instanceof TableDataVSAssembly) {
               data = sandbox.getVSTableLens(assemblyName, false)
               outFile = createExportFileByCase(assemblyName, bk, '.txt')
               exportUtil.exportVSObject(outFile.toString(), data)
            }
         }
      } catch (Exception ex) {
         new Exception("==========execute viewsheet failed:===========", ex).printStackTrace();
      }
   }

   /**
    * getVSChart dataset. code from VSChartShowDataController.java, see applyChanges() function
    */
   private DataSet getVSChartDataSet(VGraphPair vgraphpair) {
      DataSet data = vgraphpair.getData()
      DataSet filter = data
      while (true) {
         if (filter instanceof VSDataSet) {
            break
         } else if (filter instanceof PairsDataSet) {
            data = ((PairsDataSet) filter).getDataSet()
         } else if (filter instanceof DataSetFilter) {
            boolean useBase = filter instanceof BrushDataSet ||
                    filter instanceof GeoDataSet ||
                    filter instanceof MappedDataSet
            filter = ((DataSetFilter) filter).getDataSet()

            if (useBase) {
               data = filter
            }
         } else {
            break
         }
      }
      // trend/comparison as discrete measure
      boolean discreteCalc = data instanceof VSDataSet &&
              Arrays.stream(((VSDataSet) data).getDataRefs())
                      .filter(ref -> ref instanceof ChartAggregateRef)
                      .anyMatch(ref -> ((ChartAggregateRef) ref).isDiscrete() &&
                              ((ChartAggregateRef) ref).getCalculator() != null);
      if (((AbstractDataSet) data).getRowsProjectedForward() > 0 || !discreteCalc ||
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
   private DataSet sortVSDataSet(DataSet vsDataSet) {
      DataSet sorted
      if (vsDataSet instanceof FullProjectedDataSet) {
         sorted = new FullProjectedDataSet(getSortedSubdataSets((FullProjectedDataSet) vsDataSet))
      } else {
         // apply ValueOrder sorting to the data set
         SortedDataSet sorted0 = new SortedDataSet(vsDataSet)
         for (int i = 0; i < vsDataSet.getColCount(); i++) {
            sorted0.addSortColumn(vsDataSet.getHeader(i), false)
         }
         sorted = sorted0
      }

      return sorted
   }

   private List<AbstractDataSet> getSortedSubdataSets(FullProjectedDataSet dset) {
      List<AbstractDataSet> subDataSets = new ArrayList<>()

      for (AbstractDataSet subSet : dset.getSubDataSets()) {
         // apply ValueOrder sorting to each sub data set
         SortedDataSet sortedSubSet = new SortedDataSet(subSet)

         for (int i = 0; i < subSet.getColCount(); i++) {
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
      if (bks == null) {
         bks = ['(Home)'] as String[]
      }
      File outFile = createExportFileByCase(null, null, '.png')
      OutputStream out = new FileOutputStream(outFile)
      try{
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, match,
                 expandSelection, false, false, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }catch (Exception ex) {
         new Exception("==========export PNG failed:===========", ex).printStackTrace();
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
      if (bks == null) {
         bks = ['Home'] as String[]
      }
      File outFile = createExportFileByCase(null, null, '.pdf')
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true,
                 false, false, true, false,
                 bks, false, false, null, new ExportResponse(out), principal)
      }catch (Exception ex) {
         new Exception("==========export PDF failed:===========", ex).printStackTrace();
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

      if (!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if (tempFile.exists()) {
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

   /**
    * compare image with allowed diff pixel percent
    */
   void compareImageWithAllowedDiff() {
      compareImageWithAllowedDiffPixel(0.01)
   }

   void compareImageWithAllowedDiffPixel(def allowedPixelPercent) {
      compareUtil.CompareFileByFeature(null, suiteName + '/' + caseName, 'PNG', allowedPixelPercent)
   }

   private static String asset_id, suiteName, caseName
   RuntimeViewsheetResource viewsheetResource

   ExportUtil exportUtil = new ExportUtil()
   CompareUtil compareUtil = new CompareUtil()
   TUtil tUtil = new TUtil()

   SRPrincipal principal = tUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           new String[0])
}

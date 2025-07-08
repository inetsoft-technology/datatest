package inetsoft.test.modules

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.sree.security.IdentityID
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.CrosstabVSAssembly
import inetsoft.uql.viewsheet.EmbeddedTableVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.uql.viewsheet.VSBookmarkInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil

class VSCalcTest {
   VSCalcTest(String asset_id, String caseName) {
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
   def initVS(Map<String, String[]> params, Boolean isViewer) {
      DataSpace.getDataSpace()  //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      ControllersResource controllers = new ControllersResource()
      controllers.initControllers()
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id, isViewer), controllers)
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * convert table as freehand, then export as PNG
    * @param params
    * @return
    */
   def checkConvert(Map<String, String[]> params) {
      initVS(params, false)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()
      String assemblyName

      try {
         assemblies.each {
            assemblyName = it.getName()

            if ((it instanceof EmbeddedTableVSAssembly || it instanceof TableVSAssembly
                    || it instanceof CrosstabVSAssembly)
                    && it.getVSAssemblyInfo().isVisible(true)) {
               viewsheetResource.convertToFreehand(principal, assemblyName)
            }
         }
      }catch(Exception e) {
         e.printStackTrace()
      }

      /*on design time, didn't init ibookmark, so the ibookmark of rvs is null,
      in order to void NPE on line 867 of RuntimeViewsheet.java, use addBookmark methord to init ibookmark.*/
      rvs.addBookmark("(Home)", VSBookmarkInfo.ALLSHARE,
              new IdentityID("admin", "host-org"), false)

      File pngFile = createExportFileByCase(null, null,'_CALC.png')
      OutputStream out = new FileOutputStream(pngFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
              false, false, false, false,
              ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
   }

   /**
    * export vs as PNG
    * @param bks
    * @param params
    * @param match
    * @param expandSelection
    * @return
    */
   def exportAsPNG(Map<String, String[]> params, String[] bks) {
      initVS(params, true)
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      File outFile = createExportFileByCase(null, null,'.png')
      OutputStream out = new FileOutputStream(outFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
              false, false, false, false,
              bks, false, false, null, new ExportResponse(out), principal)
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
      String fileName = resourcePath + '/exportData' + File.separator + suiteName + File.separator  + caseName
      File tempFile = new File(fileName + File.separator +
              ((!['Home', '(Home)', null].contains(bk))? (bk + '_') : '' ) +
              (assemblyName != null? assemblyName : vsName) +
              suffix)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()) {
         tempFile.delete()
      }
      return tempFile
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

   CompareUtil compareUtil = new CompareUtil()
   TUtil tUtil = new TUtil()

   SRPrincipal principal = tUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])
}

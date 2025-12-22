package inetsoft.test.modules

import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.sree.internal.SUtil
import inetsoft.sree.portal.PortalThemesManager
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.util.Tool
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil

class CSSTest {
   CSSTest(String caseName) {
      this.caseName = caseName
   }

   static initHome(String suiteName) {
      def arrs = suiteName.split('.cases')
      this.suiteName = (arrs.length == 1 ? null : arrs[1].replace('.', '/'))
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   /**
    * prepare css before execute test
    */
   def InitCSS(def css) {
      InputStream inputStream = new ByteArrayInputStream(css.getBytes())

      DataSpace.getDataSpace().withOutputStream("portal", 'format.css', out -> Tool.copyTo(inputStream, out))
      PortalThemesManager.getManager().setCSSFile("portal/format.css")

      Thread.sleep(500)
   }

   /**
    * execute vscss case
    * @param asset_id
    * @param css
    * @return
    */
   def VSCSSTest(String asset_id) {
      VSCSSTest(asset_id, admin, null)
   }
   /**
    * execute vs css test
    * @param asset_id
    * @param principal
    * @param params
    */
   def VSCSSTest(String asset_id, SRPrincipal principal, Map<String, String[]> params) {
      controllers.initControllers()
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      SUtil.setAdditionalDatasource(principal)
      ThreadContext.setContextPrincipal(principal)  //use to set additional db permission
      viewsheetResource.initRuntimeVS(principal)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      rvs.gotoBookmark('(Home)', principal.getUser().getUserIdentity())
      rvs.getViewsheetSandbox().resetAll(new ChangedAssemblyList())

      File outFile = createExportFileByCase(asset_id)
      OutputStream out = new FileOutputStream(outFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                 false, false, false, false,
                 ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
      }
      finally {
         out.close()
      }
   }

   /**
    * create export file by case name
    * @param caseName
    * @param suffix
    * @return
    */
   def createExportFileByCase(String asset_id) {
      String fName
      if(asset_id.startsWith('1^128^')) {
         fName = asset_id.indexOf('/') > 0 ?
                 asset_id.split('/').last() + '.png' : asset_id.minus('1^128^__NULL__^') + '.png'
      }
      else {
         new Exception("the asset id not right: ").printStackTrace()
      }

      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      File tempFile = new File(resourcePath + '/exportData' + suiteName + File.separator + caseName +
              File.separator + fName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      }
      else if(tempFile.exists()) {
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
    * compare the file
    * @param fls
    */
   void compareImage(String[] fls) {
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName, 'PNG')
   }

   static String suiteName, caseName
   // for vs
   private RuntimeViewsheetResource viewsheetResource
   private ControllersResource controllers = new ControllersResource()

   SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], [] as String[])

   private CompareUtil compareUtil = new CompareUtil()
}

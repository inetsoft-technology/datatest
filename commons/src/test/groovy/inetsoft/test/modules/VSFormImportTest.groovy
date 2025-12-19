package inetsoft.test.modules

import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource

import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class VSFormImportTest extends ViewsheetTest {

   VSFormImportTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   /**
    * Init runtime VS
    * @param params
    * @return
    */
   def initVS() {
      DataSpace.getDataSpace() //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      controllers = new ControllersResource()
      controllers.initControllers()
      controllers.initApplicationContext(ConfigurationContext.getContext())
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(null, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * import excel to current vs
    * @param file file name with suffix, eg: vs.xlsx
    * @return
    */
   def importXLSToVS(String file) {
      try {
         initVS()
         File excelFile = new File(this.class.getResource('/excelFiles').getPath() + '/' + file)
         FileInputStream fileInputStream = new FileInputStream(excelFile)
         MultipartFile multipartFile = new MockMultipartFile(excelFile.getName(), excelFile.getName(), 'text/plain', fileInputStream)
         viewsheetResource.processImportXLS(principal, multipartFile)

         File pngFile = createExportFileByCase(null, null, '_Import.png')
         OutputStream out = new FileOutputStream(pngFile)
         try {
            viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, false,
                    false, true, false, false,
                    ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
         } finally {
            out.close()
         }
      } catch (Exception ex) {
         ex.printStackTrace()
      } finally {
         controllers.destroy()
      }
   }

   private static ControllersResource controllers
}

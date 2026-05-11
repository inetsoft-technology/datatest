package inetsoft.test.modules

import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.MessageTestUtils
import inetsoft.test.core.RuntimeViewsheetResource

import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class VSFormImportTest extends ViewsheetTest {

   VSFormImportTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   /**
    * Init runtime VS
    */
   def initVS() {
      ensurePrincipal()
      ThreadContext.setContextPrincipal(principal)
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(null, asset_id))
      viewsheetResource.initRuntimeVS(principal)
   }

   /**
    * import excel to current vs
    * @param file file name with suffix, eg: vs.xlsx
    */
   def importXLSToVS(String file) {
      try {
         initVS()
         File excelFile = new File(this.class.getResource('/excelFiles').getPath() + '/' + file)
         byte[] bytes
         new FileInputStream(excelFile).withCloseable { fis -> bytes = fis.bytes }
         MultipartFile multipartFile = new MockMultipartFile(excelFile.getName(), excelFile.getName(),
                 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', bytes)
         viewsheetResource.processImportXLS(principal, multipartFile)

         File pngFile = createExportFileByCase(null, null, '_Import.png')
         OutputStream out = new FileOutputStream(pngFile)
         try {
            // Isolate MessageContext + refresh + export to this spec only — avoids changing
            // behavior for all other RuntimeViewsheetResource.exportVS callers.
            String rid = viewsheetResource.getRuntimeId()
            MessageTestUtils.withMockMessageContext(principal, rid, {
               viewsheetResource.refreshViewsheet(principal)
               try {
                  // match=true: same layout path as other VS PNG tests; current=true: post-import runtime.
                  viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                          false, true, false, false,
                          ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
               }
               catch(Exception exportEx) {
                  throw new RuntimeException('Export after import failed', exportEx)
               }
            } as Runnable)
         }
         finally {
            out.close()
         }
      }
      catch(Exception ex) {
         ex.printStackTrace()
      }
   }
}

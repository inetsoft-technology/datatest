package inetsoft.test.modules

import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef
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

   def initVS() {
      ensurePrincipal()
      ThreadContext.setContextPrincipal(principal)
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(null, asset_id))
      viewsheetResource.initRuntimeVS(principal)
   }

   def importXLSToVS(String file) {
      try {
         initVS()
         String rel = 'excelFiles/' + file
         byte[] excelBytes = readClasspathResourceBytes(rel)

         MultipartFile multipartFile = new MockMultipartFile(file, file,
                 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', excelBytes)
         viewsheetResource.processImportXLS(principal, multipartFile)

         File pngFile = createExportFileByCase(null, null, '_Import.png')
         OutputStream out = new FileOutputStream(pngFile)
         try {
            String rid = ConfigurationContext.getContext().getSpringBean(RuntimeViewsheetRef.class).getRuntimeId()
            MessageTestUtils.withMockMessageContext(principal, rid, {
               viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, false,
                       false, true, false, false,
                       ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
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

   /**
    * Load bytes from classpath ({@code excelFiles/…}); tries this class's loader then context class loader.
    */
   private static byte[] readClasspathResourceBytes(String classpathRelative) {
      try (InputStream stream = VSFormImportTest.class.classLoader.getResourceAsStream(classpathRelative)
            ?: Thread.currentThread().contextClassLoader.getResourceAsStream(classpathRelative)) {
         if(stream == null) {
            throw new IllegalStateException('Missing classpath resource: ' + classpathRelative)
         }
         return stream.bytes
      }
   }
}

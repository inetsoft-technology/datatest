package inetsoft.test.modules

import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.web.viewsheet.service.ExportResponse

import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class VSFormImportTest extends ViewsheetTest {
   VSFormImportTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   /**
    * import excel to current vs
    * @param file file name with suffix, eg: vs.xlsx
    * @return
    */
   def importXLSToVS(String file) {
      initVS()
      File excelFile = new File(this.class.getResource('/excelFiles').getPath() + '/' + file)
      FileInputStream fileInputStream = new FileInputStream(excelFile)
      MultipartFile multipartFile = new MockMultipartFile(excelFile.getName(), excelFile.getName(), 'text/plain', fileInputStream)
      viewsheetResource.processImportXLS(principal, multipartFile)

      File pngFile = createExportFileByCase(null, null, '_Import.png')
      OutputStream out = new FileOutputStream(pngFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, false, false, true,
                 false, false, ['(Home)'] as String[], false,
                 false, null, new ExportResponse(out), principal)
      } finally {
         out.close()
      }
   }
}

package inetsoft.test.modules

import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class VSFormImportTest {
   VSFormImportTest(String asset_id, String caseName) {
      this.asset_id = asset_id
      this.caseName = caseName
   }

   static initHome(String suiteName) {
      def arrs = suiteName.split('.cases')
      this.suiteName = arrs.length == 1? null : arrs[1].replace('.', '/')
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   /**
    * Init runtime VS
    * @param params
    * @return
    */
   def initVS() {
      DataSpace.getDataSpace() //after upgrade storage, need get first to get dataspace, then to get indexstorage.
      ControllersResource controllers = new ControllersResource()
      controllers.initControllers()
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
      initVS()
      File  excelFile= new File(this.class.getResource('/excelFiles').getPath() + '/' + file)
      FileInputStream fileInputStream = new FileInputStream(excelFile)
      MultipartFile multipartFile = new MockMultipartFile(excelFile.getName(), excelFile.getName(), 'text/plain', fileInputStream)
      viewsheetResource.processImportXLS(principal, multipartFile)

      File pngFile = createExportFileByCase(null, null,'_Import.png')
      OutputStream out = new FileOutputStream(pngFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, false,
              false, true, false, false,
              ['Home'] as String[], false, false, null, new ExportResponse(out), principal)
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

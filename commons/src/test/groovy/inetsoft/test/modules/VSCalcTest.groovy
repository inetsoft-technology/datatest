package inetsoft.test.modules

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.sree.security.IdentityID
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.CrosstabVSAssembly
import inetsoft.uql.viewsheet.EmbeddedTableVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.uql.viewsheet.VSBookmarkInfo
import inetsoft.web.viewsheet.service.ExportResponse

class VSCalcTest extends ViewsheetTest {
   VSCalcTest(String asset_id, String caseName) {
      super(asset_id, caseName)
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
      } catch (Exception e) {
         e.printStackTrace()
      }

      /*on design time, didn't init ibookmark, so the ibookmark of rvs is null,
      in order to void NPE on line 867 of RuntimeViewsheet.java, use addBookmark methord to init ibookmark.*/
      rvs.addBookmark("(Home)", VSBookmarkInfo.ALLSHARE,
              new IdentityID("admin", "host-org"), false)

      File pngFile = createExportFileByCase(null, null, '_CALC.png')
      OutputStream out = new FileOutputStream(pngFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
              false, false, false, false,
              ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
   }
}

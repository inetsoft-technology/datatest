package inetsoft.test.modules

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.CalcTableVSAssembly
import inetsoft.uql.viewsheet.CrosstabVSAssembly
import inetsoft.uql.viewsheet.EmbeddedTableVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.web.viewsheet.service.ExportResponse

class VSCalcTest extends ViewsheetTest {
   VSCalcTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   def checkConvert(Map<String, String[]> params) {
      checkConvert0(params)
   }

   /**
    * convert table as freehand, then export as PNG
    * @param params
    * @return
    */
   def checkConvert0(Map<String, String[]> params) {
      initVS(params, false)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()
      String assemblyName
      List<String> convertedAssemblies = []
      File pngFile = createExportFileByCase(null, null, '_CALC.png')

      try {
         assemblies.each {
            assemblyName = it.getName()

            if((it instanceof EmbeddedTableVSAssembly || it instanceof TableVSAssembly
                    || it instanceof CrosstabVSAssembly)
                    && it.getVSAssemblyInfo().isVisible(true)) {
               viewsheetResource.convertToFreehand(principal, assemblyName)
               convertedAssemblies.add(assemblyName)
            }
         }
      }
      catch(Exception e) {
         throw new RuntimeException("convert to freehand failed: " + assemblyName, e)
      }

      rvs = viewsheetResource.getRuntimeViewsheet(principal)

      if(convertedAssemblies.isEmpty()) {
         throw new RuntimeException("no visible table or crosstab assembly was converted")
      }

      convertedAssemblies.each {
         Assembly assembly = rvs.getViewsheet().getAssembly(it)

         if(!(assembly instanceof CalcTableVSAssembly)) {
            throw new RuntimeException("assembly was not converted to freehand: " + it)
         }
      }

      OutputStream out = new FileOutputStream(pngFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                 false, true, false, false,
                 [] as String[], false, false, null, new ExportResponse(out), principal)
      }
      catch(Exception ex) {
         throw new RuntimeException("export converted viewsheet failed", ex)
      }
      finally {
         out.close()
      }

      if(pngFile.length() == 0L) {
         throw new RuntimeException("converted viewsheet export is empty: " + pngFile)
      }
   }
}

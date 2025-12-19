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
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService
import inetsoft.web.viewsheet.controller.OpenViewsheetController
import inetsoft.web.viewsheet.controller.OpenViewsheetService
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil
import org.mockito.MockedStatic
import static org.mockito.Mockito.*

class VSCalcTest extends ViewsheetTest {
   VSCalcTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   def checkConvert(Map<String, String[]> params) {
      try (MockedStatic<ConfigurationContext> staticConfigurationContext = mockStatic(ConfigurationContext.class)) {
         ConfigurationContext spyContext = spy(context)

         OpenViewsheetController openViewsheetController = controllers.getOpenViewsheetController()
         OpenViewsheetService openViewsheetService = controllers.getOpenViewsheetService()
         ComposerVSTableService composerVSTableService = controllers.getComposerVSTableService()

         doReturn(openViewsheetController)
                 .when(spyContext)
                 .getSpringBean(OpenViewsheetController.class)

         doReturn(openViewsheetService)
                 .when(spyContext)
                 .getSpringBean(OpenViewsheetService.class)

         doReturn(composerVSTableService)
                 .when(spyContext)
                 .getSpringBean(ComposerVSTableService.class)

         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext)

         checkConvert0(params)
      } catch (Exception e) {
         System.err.println("======convert========exceptions============");
         e.printStackTrace();
      }

   }

   /**
    * convert table as freehand, then export as PNG
    * @param params
    * @return
    */
   def checkConvert0(Map<String, String[]> params) {
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
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                 false, false, false, false,
                 ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
      } catch (Exception ex) {
         ex.printStackTrace()
      }
   }
}

package inetsoft.test.modules

import inetsoft.analytic.composition.ViewsheetService
import inetsoft.sree.ClientInfo
import inetsoft.sree.internal.SUtil
import inetsoft.sree.security.IdentityID
import inetsoft.sree.security.SRPrincipal
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.DataMap
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.TableDataVSAssembly
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.util.Tool
import inetsoft.web.viewsheet.event.OpenViewsheetEvent
import inetsoft.web.portal.data.DatabaseDatasourcesController
import inetsoft.web.viewsheet.controller.OpenViewsheetController
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef

import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.MessageTestUtils

class VPMTest {
   VPMTest(String asset_id) {
      this.asset_id = asset_id
   }

   static initHome(ViewsheetService viewsheetService,
                   OpenViewsheetController openViewsheetController,
                   RuntimeViewsheetRef runtimeViewsheetRef,
                   DatabaseDatasourcesController databaseDatasourcesController) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home") + "\n")
      DataSpace.getDataSpace()
      this.viewsheetService = viewsheetService
      this.openViewsheetController = openViewsheetController
      this.runtimeViewsheetRef = runtimeViewsheetRef
      this.databaseDatasourcesController = databaseDatasourcesController
   }

   def executeVS(SRPrincipal user, Map<String, String[]> params) {
      user.setIgnoreLogin(true)
      ThreadContext.setContextPrincipal(user)
      OpenViewsheetEvent openViewsheetEvent = actionEventsUtil.createOpenViewsheetEvent(params, asset_id)
      SUtil.setAdditionalDatasource(user)
      String runtimeId = openRuntimeViewsheet(openViewsheetEvent, user)

      RuntimeViewsheet rvs = viewsheetService.getViewsheet(runtimeId, user)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      String assemblyName = ''
      String userName = user.getClientUserID().getName()
      System.err.print("=========userName=====" + userName + "\n")
      try {
         assemblies.each {
            assemblyName = it.getName()
            data = sandbox.getData(assemblyName, true, DataMap.NORMAL)
            if(it instanceof TableDataVSAssembly) {
               data = sandbox.getVSTableLens(assemblyName, false) ?: data
            }
            exportData(data, getExportFilePath(userName, assemblyName))

         }
      }
      catch(Exception e) {
         e.printStackTrace()
      }
   }

   /**
    * refresh medatadata
    * @param datasource : 'Examples/Orders'
    */
   def refreshMetadata(String datasource) {
      databaseDatasourcesController.refreshMetadata(datasource)
   }

   private static String openRuntimeViewsheet(OpenViewsheetEvent openViewsheetEvent, SRPrincipal user) {
      MessageTestUtils.withMockMessageContext(user, null, openViewsheetEvent, (ctx, event) -> {
         openViewsheetController.openViewsheet(event, ctx.getUser(), ctx.getCommandDispatcher(),
                 'http://localhost:8080/sree')
         runtimeViewsheetRef.getRuntimeId()
      })
   }

   def exportData(def data, String filename) {
      exportUtil.exportVSObject(filename, data, true)
   }

   void compareData(String[] fileNames) {
      String suiteName = '/' + asset_id.substring(asset_id.lastIndexOf('^') + 1)
      compareUtil.CompareFileByFeature(fileNames, suiteName, 'TXT')
   }

   private String getExportFolderPath() {
      String path = new File(this.getClass().getResource("/expectData").getPath()).getParent()
      return path + File.separator + 'exportData' + File.separator +
              asset_id.substring(asset_id.lastIndexOf('^') + 1)
   }

   private String getExportFilePath(String userName, String assemblyname) {
      return getExportFolderPath() + File.separator + userName + '_' + assemblyname + '.txt'
   }

   static SRPrincipal createPrincipal(String userName, String[] roles, String[] groups) {
      IdentityID identityUser = new IdentityID(userName, 'host-org')
      ClientInfo user = new ClientInfo(identityUser, Tool.getIP())
      IdentityID[] identityRoles = new IdentityID[0]
      roles.each { role ->
         IdentityID newRole = role != 'Administrator' ? new IdentityID(role, 'host-org') : new IdentityID('Administrator', null)
         newRole.setName(role)
         identityRoles += newRole
      }
      return new SRPrincipal(user, identityRoles, groups, 'host-org', Tool.getSecureRandom().nextLong())
   }

   private static String asset_id
   private static ViewsheetService viewsheetService
   private static OpenViewsheetController openViewsheetController
   private static RuntimeViewsheetRef runtimeViewsheetRef
   private static DatabaseDatasourcesController databaseDatasourcesController
   private static ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   private static ExportUtil exportUtil = new ExportUtil()
   private static CompareUtil compareUtil = new CompareUtil()
}

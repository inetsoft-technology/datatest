package inetsoft.test.modules

import inetsoft.report.TableLens
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

import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.ExportUtil

class VPMTest {
   VPMTest(String asset_id) {
      this.asset_id = asset_id
   }

   static initHome() {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
      DataSpace.getDataSpace()
      controllers.initControllers()
   }

   def executeVS(SRPrincipal user, Map<String, String[]> params) {
      user.setIgnoreLogin(true)
      ThreadContext.setContextPrincipal(user)
      OpenViewsheetEvent openViewsheetEvent = actionEventsUtil.createOpenViewsheetEvent(params, asset_id)
      viewsheetResource = new RuntimeViewsheetResource(openViewsheetEvent, controllers)
      SUtil.setAdditionalDatasource(user)
      viewsheetResource.initRuntimeVS(user)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(user)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()

      def data = null
      String assemblyName = ''
      String userName = user.getClientUserID().getName()
      try{
         assemblies.each {
            assemblyName = it.getName()
            data = sandbox.getData(assemblyName,true, DataMap.NORMAL)
            if(it instanceof TableDataVSAssembly) {
               data = sandbox.getVSTableLens(assemblyName, false)
            }
            exportData(data, getExportFilePath(userName, assemblyName))

         }
      }catch(Exception e) {
         e.printStackTrace()
      }
   }

   /**
    * refresh medatadata
    * @param datasource: 'Examples/Orders'
    */
   def refreshMetadata(String datasource) {
      controllers.getDatabaseDatasourcesController().refreshMetadata(datasource)
   }

   def exportData(def data, String filename) {
      File file = new File(filename)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      } else if(file.exists()){
         file.delete()
      }

      if(data == null || data == '') {
         data = ['null']
      }

      if(data instanceof TableLens) {
         TableLens table =  exportUtil.wrapTable(data, true)
         StringBuffer buffer = new StringBuffer()
         int row = 0
         while (table.moreRows(row)) {
            for(int col = 0; col < table.getColCount(); col++) {
               buffer.append(table.getObject(row, col))
               if(table.getColCount() != (col+1)) {
                  buffer.append(', ')
               }
            }
            buffer.append('\n')
            row++
         }
         file.withPrintWriter { printWriter ->
            printWriter.println("The data size(row x col) is:(" + row + " x " + table
                    .getColCount() + ")")
            printWriter.print(buffer.toString())
         }
      }
   }

   void compareData(String[] fileNames) {
      String resPath
      def resArray = []

      if(fileNames != null) {
         fileNames.each {
            resPath = getExportFolderPath() + File.separator + it + '.txt'
            resArray.add(new CompareUtil().FileCompare(resPath))
         }
      }
      else {
         File folder = new File(getExportFolderPath())
         folder.listFiles().each {
            if(it.getAbsolutePath().endsWith('.txt')) {
               resArray.add(new CompareUtil().FileCompare(it.getAbsolutePath()))
            }
         }
      }

      String failedInfo
      resArray.each {
         if (it.getAt('false') != null) {
            failedInfo += "\n" + it.getAt('false') + "\n"
         }
      }

      if (failedInfo != null) {
         assert false : failedInfo
      }
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
         IdentityID newRole = role != 'Administrator' ? new IdentityID(role, 'host-org'): new IdentityID('Administrator', null)
         newRole.setName(role)
         identityRoles += newRole
      }
      return new SRPrincipal(user, identityRoles, groups, 'host-org', Tool.getSecureRandom().nextLong())
   }

   private static String asset_id
   private static ControllersResource controllers = new ControllersResource()
   private static RuntimeViewsheetResource viewsheetResource
   private static ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   private static ExportUtil exportUtil = new ExportUtil()
}

package inetsoft.test.modules

import inetsoft.report.TableLens
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.RuntimeWorksheet
import inetsoft.report.composition.execution.AssetQuerySandbox
import inetsoft.report.filter.SortFilter
import inetsoft.sree.internal.SUtil
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.asset.AggregateInfo
import inetsoft.uql.asset.Assembly
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.TableAssemblyInfo
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.composer.ws.event.OpenWorksheetEvent
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.ExportUtil
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.RuntimeWorksheetResource
import inetsoft.test.core.TUtil

class AdditionalConnectionTest {
   AdditionalConnectionTest(String caseName) {
      this.caseName = caseName
   }

   static initHome(String suiteName) {
      //System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      def arrs = suiteName.split('.cases')
      this.suiteName = (arrs.length == 1? null : arrs[1].replace('.', '/'))
      context = ConfigurationContext.getContext()
      context.setHome(System.getProperty("sree.home"))
   }

   /**
    * execute multitanet test
    * @param asset_id: vs entry, ws entry, report path
    * @param principals
    * @param params
    * @return
    */
   def executeTest(String asset_id, def principals, Map<String, String[]> params) {
      SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
              [] as String[])
      DataSpace.getDataSpace()
      controllers = new ControllersResource()
      controllers.initControllers()
      controllers.initApplicationContext(context)

      try {
         if (principals == null) {
            principals = [admin]
         }
         principals.each {
            SUtil.setAdditionalDatasource(it)
            ThreadContext.setContextPrincipal(it)  //use to set additional db permission

            if (asset_id.startsWith('1^128^')) {
               executeVS(asset_id, it, params)
            }
            else if (asset_id.startsWith'1^2^') {
               executeWS(asset_id, it, params)
            }
            else {
               new Exception("====Input right asset_id========" + asset_id).printStackTrace()
            }
         }
      } finally {
         controllers.destroy()
      }
}

   /**
    * execute vs with user and parameter
    * @param asset_id
    * @param principal
    * @param paramscreateFileByCase
    */
   def executeVS(String asset_id, SRPrincipal principal, Map<String, String[]> params) {
      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent (params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      rvs.gotoBookmark('(Home)', principal.getUser().getUserIdentity(), principal)
      rvs.getViewsheetSandbox().resetAll(new ChangedAssemblyList())

      File outFile = createFileByCase(caseName, asset_id, principal, null)
      OutputStream out = new FileOutputStream(outFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
              false, false, false, false,
              ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
   }

   /**
    * execute ws with principal
    * @param asset_id: ws entry, such as 1^2^__NULL__^printLayout/Sws1_1
    * @param principal
    * @param params
    * @return
    */
   def executeWS(String asset_id, SRPrincipal principal, Map<String, Object[]> params) {
      OpenWorksheetEvent openWorksheetEvent = actionEventsUtil.openWorksheetEvent(asset_id)
      worksheetResource = new RuntimeWorksheetResource(openWorksheetEvent, controllers)
      worksheetResource.initRuntimeWS(principal)

      RuntimeWorksheet runtimeWorksheet = worksheetResource.getRuntimeWorksheet(principal)
      assetQuerySandbox = runtimeWorksheet.getAssetQuerySandbox()
      Assembly[] assemblies = runtimeWorksheet.getWorksheet().getAssemblies()

      params.each {
         assetQuerySandbox.getVariableTable().put(it.key, it.value)
      }
      try{
         assemblies.each {
            if(it.getAssemblyType() == Worksheet.TABLE_ASSET) {
               TableAssembly tableAssembly = (TableAssembly)it
               String tableName = tableAssembly.getName()
               if (!tableAssembly.isVisibleTable()) {
                  return
               }
               setLiveData(tableAssembly)

               TableLens lens = assetQuerySandbox.getTableLens(tableName, AssetQuerySandbox.LIVE_MODE)
               // sort lens to void row sort issue.
               SortFilter sortlens = new SortFilter(lens)
               File outFile = createFileByCase(caseName, asset_id, principal, tableName)
               exportUtil.exportWSObject(outFile.absolutePath, sortlens)
            }
         }
      }catch (Exception e) {
         e.printStackTrace()
      }
   }

   private setLiveData(TableAssembly table) {
      table.setLiveData(true)
      table.setRuntime(table.isRuntimeSelected())
      table.setEditMode(false)
      TableAssemblyInfo info = table.getTableInfo()
      AggregateInfo group = table.getAggregateInfo()
      info.setAggregate(group != null && !group.isEmpty())
   }

   /**
    * create out put file
    * @param caseName
    * @param asset_id
    * @param principal
    * @param tableName, only for ws
    * @return
    */
   def createFileByCase(String caseName, String asset_id, SRPrincipal principal, String tableName) {
      String fileName = ''
      String objName = ''

      if (asset_id.startsWith('1^128^')) {
         objName = (asset_id.indexOf('/') > 0 ?
                 asset_id.split('/').last() : asset_id.minus('1^128^__NULL__^'))
         fileName = 'VS' + File.separator + objName + '__' + principal.getIdentityID().getName() + '.png'
      }
      else if (asset_id.startsWith'1^2^') {
         objName = (asset_id.indexOf('/') > 0 ?
                 asset_id.split('/').last() : asset_id.minus('1^2^__NULL__^'))
         fileName = 'WS' + File.separator + objName + '__' + tableName + '__' + principal.getIdentityID().getName() + '.txt'
      }
      else {
         new Exception("====Input right asset_id when export file======" + asset_id).printStackTrace()
      }

      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      File tempFile = new File(resourcePath + '/exportData' + suiteName + File.separator + caseName +
              File.separator + fileName)

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()){
         tempFile.delete()
      }
      return tempFile
   }

   /**
    * compare all text file
    */
   void compareData() {
      compareData(null)
   }

   /**
    * compare text file
    * @param fileNames txt file names
    */
   void compareData(String[] fls) {
      ['VS','WS'].each {
         compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName + '/' + it, 'TXT')
      }
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
      compareUtil.CompareFileByFeature(fls, suiteName + '/' + caseName + '/VS', 'PNG')
   }

   static String suiteName, caseName
   static ConfigurationContext context
   // for vs, ws
   private RuntimeViewsheetResource viewsheetResource
   private ControllersResource controllers
   private AssetQuerySandbox assetQuerySandbox
   private RuntimeWorksheetResource worksheetResource

   private CompareUtil compareUtil = new CompareUtil()
   private ExportUtil exportUtil = new ExportUtil()
   private ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
}

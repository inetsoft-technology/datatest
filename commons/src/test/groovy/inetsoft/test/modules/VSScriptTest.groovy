package inetsoft.test.modules

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.VSAssembly
import inetsoft.uql.viewsheet.VSBookmark
import inetsoft.uql.viewsheet.VSBookmarkInfo
import inetsoft.uql.viewsheet.Viewsheet
import inetsoft.uql.viewsheet.internal.VSAssemblyInfo
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.service.ExportResponse

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.CompareUtil
import inetsoft.test.core.ControllersResource
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class VSScriptTest {
   VSScriptTest(String asset_id, String caseName) {
      this.asset_id = asset_id
      this.caseName = caseName
   }

   /**
    * init sree home
    * @return
    */
   static initHome(String suiteName) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home"))
      def arrs = suiteName.split('.vsscript')
      this.suiteName = arrs.size() == 1? null : arrs[1].replace('.', '/')
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
   }

   def printVS(def bks, def assemblies) {
      printVS(null, bks, null, null, assemblies)
   }

   /*def printVS(String scriptName, def testData) {
      printVS(null, null, scriptName, testData, null)
   }*/

   def printVS(String scriptName, def testData, def assemblyNames) {
      printVS(null, null, scriptName, testData, assemblyNames)
   }

   /**
    *
    * @param suffix
    * @param params
    * @param bks default is '(Home)'
    * @param type
    * @return
    */
   def printVS(Map<String, String[]> params, String[] bks, String scriptName, def testData, def assemblyNames) {
      SRPrincipal principal = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      DataSpace.getDataSpace()
      controllers.initControllers()
      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id), controllers)
      viewsheetResource.initRuntimeVS(principal)
      RuntimeViewsheet runtimeViewsheet = viewsheetResource.getRuntimeViewsheet(principal)
      Viewsheet viewsheet = runtimeViewsheet.getViewsheet()

      //set script
      testData.each {
         def  result = JsonOutput.toJson(it)
         JsonSlurper jsonSlurper = new JsonSlurper()
         def handler = jsonSlurper.parseText(result).HANDLER
         def command = jsonSlurper.parseText(result).COMMAND

         if('ONINIT'.equals(handler)) {
            viewsheet.getViewsheetInfo().setOnInit(command)
         }
         else if ('ONREFRESH'.equals(handler)) {
            viewsheet.getViewsheetInfo().setOnLoad(command)
         }
         else {
            VSAssembly vsAssembly = (VSAssembly)viewsheet.getAssembly(handler)
            vsAssembly.getVSAssemblyInfo().setScript(command)
         }
         viewsheetResource.refreshViewsheet(principal)
      }

      //hide other assembly
      if (assemblyNames != null) {
         viewsheet.getAssemblies().each { it ->
            if (!(it.getName() in assemblyNames)) {
               VSAssemblyInfo assemblyInfo = (VSAssemblyInfo)it.getInfo()
               //assemblyInfo.setVisible('hide')
               assemblyInfo.setScript("visible='hide'")
            }
         }
      }
      //for calctable, if set script, the script didn't save to home bookmark, so add it
      runtimeViewsheet.addBookmark(VSBookmark.HOME_BOOKMARK, VSBookmarkInfo.ALLSHARE, principal.getUser().getUserIdentity(), false)

      scriptName = scriptName?: 'VS'
      bks = bks?:['(Home)'] as String[]
      File outFile = createFile(scriptName)
      OutputStream out = new FileOutputStream(outFile)
      viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
              false, false, false, false,
              bks, false, false,null, new ExportResponse(out), principal)
   }

   /**
    * to create the export file
    * @param asset_id
    * @param type
    * @param idx
    * @param map
    * @return
    */
   def createFile(String scriptName) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String fileName = resourcePath + '/exportData' + suiteName
      File tempFile = new File(fileName + File.separator  + caseName  + '_' + scriptName + '.png')

      if(!tempFile.getParentFile().exists()) {
         tempFile.getParentFile().mkdirs()
      } else if(tempFile.exists()){
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
      compareUtil.CompareFileByFeature(fls, suiteName, 'PNG')
   }

   static String asset_id, suiteName, caseName
   RuntimeViewsheetResource viewsheetResource
   ControllersResource controllers = new ControllersResource()
   ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
   CompareUtil compareUtil = new CompareUtil()
}

package inetsoft.test.mv

import inetsoft.mv.data.MVQuery
import inetsoft.mv.MVManager
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.TableLens
import inetsoft.report.composition.execution.AssetTableLens
import inetsoft.report.composition.execution.DataMap
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.report.composition.execution.MVInfo
import inetsoft.report.filter.SortFilter
import inetsoft.sree.SreeEnv
import inetsoft.sree.security.IdentityID
import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.CompareUtil
import inetsoft.uql.XConstants
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.AssociatedSelectionVSAssembly
import inetsoft.uql.viewsheet.CalendarVSAssembly
import inetsoft.uql.viewsheet.CompositeSelectionValue
import inetsoft.uql.viewsheet.ListData
import inetsoft.uql.viewsheet.SelectionList
import inetsoft.uql.viewsheet.SelectionVSAssembly
import inetsoft.uql.viewsheet.SelectionValue
import inetsoft.uql.viewsheet.TimeSliderVSAssembly
import inetsoft.uql.viewsheet.Viewsheet
import inetsoft.util.ConfigurationContext
import inetsoft.util.Tool
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.event.OpenViewsheetEvent
import inetsoft.report.composition.graph.VSDataSet
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.ControllersResource

import java.security.Principal
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.text.DecimalFormat

class MVTest {
   MVTest(String asset_id) {
      this.asset_id = asset_id
   }

   def static initHome() {
      ConfigurationContext.getContext().setHome(System.getProperty("sree.home"))
      ControllersResource controllers = new ControllersResource()
      controllers.initControllers()
   }

   def static initHome(def pro) {
      initHome()
      if(pro != null) {
         pro.each {key, value ->
            SreeEnv.setProperty(key, value)
         }
      }
      SreeEnv.save()
   }

   List getMVDefInfo(List<TableLens> datas) {
      if(datas == null || datas.size() == 0) {
         return null
      }

      def allMVInfos = []
      int count = 0
      for(n in 0..< datas.size()) {
         if(datas[n] != null) {
            def mvInfos = []
            List<MVInfo> list = MVQuery.getMVInfos(datas[n])

            if(list != null && list.size() > 0) {
               for(i in 0..< list.size()) {
                  mvInfos[i] = list[i].getMVTable() + "|" + (list[i].isSub() ? "SubMV" :
                        "TopMV")
               }
            }else {
               mvInfos = null
               System.err.println("=========can not hit mv=========")
            }
            allMVInfos[count] = mvInfos
            count++
         }
      }

      return allMVInfos.flatten()
   }

   static def getMVDefInfo() {
      def mvinfos = []
      MVManager.getManager().list(true).each {
         mvinfos.add(it.getMVTable() + "|" + (it.isSub() ? "SubMV" : "TopMV"))
      }
      return mvinfos
   }

   /**
    * execute vs with admin
    * @param params
    * @param bks
    * @param incremental
    * @param exportSelection
    * @return
    */
   def executeVS(Map<String, String[]> params, String[] bks, boolean incremental, boolean exportSelection) {
      executeVS(params, bks, incremental, exportSelection, null)
   }

   /**
    * execute vs with user
    * @param params
    * @param bks
    * @param incremental
    * @param exportSelection
    * @param principal
    * @return
    */
   def executeVS(Map<String, String[]> params, String[] bks, boolean incremental, boolean exportSelection, SRPrincipal principal) {
      if(bks == null) {
         bks = ['(Home)'] as String[]
      }
      bks.each {
         executeTest(params, it, incremental, exportSelection, principal)
      }
   }

   def executeTest(Map<String, String[]> params, String bk, boolean incremental, boolean exportSelection, SRPrincipal principal) {
      if(principal == null) {
         principal = createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
      controllers = new ControllersResource()
      controllers.initControllers()
      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(createOpenViewsheetEvent(params)
            , controllers)
      viewsheetResource.initRuntimeVS(principal)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      if(bk != null) {
         rvs.gotoBookmark(bk, principal.getUser().getUserIdentity())
         rvs.getViewsheetSandbox().resetAll(new ChangedAssemblyList())
      }
      Viewsheet vs = rvs.getViewsheet()
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox()
      Assembly[] assemblies = vs.getAssemblies()
      def datas = []
      def assemblyNames = []
      def tableLens = []
      int n = 0

      assemblies.each {
         if(it instanceof SelectionVSAssembly) {
            if(exportSelection) {
               assemblyNames.add(it.getName())
               if(it instanceof AssociatedSelectionVSAssembly) {
                  datas.add(((AssociatedSelectionVSAssembly) it).getSelectionList())
               }else if(it instanceof TimeSliderVSAssembly) {
                  datas.add((TimeSliderVSAssembly) it)
               }else if(it instanceof CalendarVSAssembly) {
                  datas.add((CalendarVSAssembly) it)
               }
            }
         }else {
            try{
               sandbox.shrink()
               String assemblyName = it.getName()
               def data = sandbox.getData(assemblyName, true, DataMap.NORMAL)
               //for chart
               if(data instanceof VSDataSet) {
                  data = ((VSDataSet) data).getTable()
               }
               datas.add(data)
               assemblyNames.add(assemblyName)

               if(data instanceof TableLens) {
                  tableLens[n] = data
               }
            }catch(Exception e) {
               e.printStackTrace()
            }
            n++
         }
      }

      for(i in 0..< datas.size()) {
         exportData(datas[i], getExportFilePath(assemblyNames[i] as String,
                 principal, bk, incremental))
      }

      return tableLens
   }

   def exportData(def data, String fileName) {
      File file = new File(fileName)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      }else if(file.exists()){
         file.delete()
      }

      def printWriter = file.newPrintWriter()

      if(data == null || data == '') {
         data = ['NULL']
      }

      if(data instanceof Object[] && !(data instanceof  TableLens[])) {
         Object[] vals = (Object[]) data
         printWriter.println("The length of the array is: [" + vals.length + "]")

         vals.each {
            printWriter.println(format(it))
         }
      }else if(data instanceof TableLens[]) {
         TableLens[] tables = (TableLens[]) data
         tables.each {
            export(it, printWriter)
         }
      }else if(data instanceof TableLens) {
         export((TableLens)data, printWriter)
      }else if(data instanceof SelectionList) {
         export((SelectionList)data, printWriter)
      }else if(data instanceof TimeSliderVSAssembly) {
         export((TimeSliderVSAssembly)data, printWriter)
      }else if(data instanceof CalendarVSAssembly) {
         export((CalendarVSAssembly)data, printWriter)
      }else if(data instanceof ListData) {
         export((ListData)data, printWriter)
      }else if(data instanceof StringBuffer) {
         printWriter.println(data.toString())
      }else if(data != null) {
         printWriter.println(format(data))
      }

      printWriter.flush()
      printWriter.close()
   }

   def export(TableLens table, def printWriter) {
      table = new SortFilter(table)
      table = wrapTable(table)
      StringBuffer buffer = new StringBuffer()
      int row = 0
      while(table.moreRows(row)) {
         for(int col = 0; col < table.getColCount(); col++){
            buffer.append(table.getObject(row, col))
         }
         buffer.append("\n")
         row++
      }

      printWriter.println("The table size(row x col) is: ("+ row + " x " + table
            .getColCount() + ")")
      printWriter.println(buffer.toString())
      printWriter.flush()
   }

   def export(SelectionList list, def printWriter) {
      list = (SelectionList)list.clone()
      list.sort(XConstants.SORT_ASC)
      export(list, printWriter, null)
   }

   def export(SelectionList list, def printWriter, String parent) {
      SelectionValue[] values = list.getAllSelectionValues()
      values.each {
         if(parent != null) {
            printWriter.write(parent)
         }
         printWriter.print(format(it.getLabel()))
         printWriter.print("   SelectState:" + format(it.getState()))
         if (it.getMeasureLabel() != null) {
            printWriter.print("   Measure:" + format(it.getMeasureLabel()))
         }
         printWriter.println()

         if(it instanceof CompositeSelectionValue) {
            export(((CompositeSelectionValue)it).getSelectionList(), printWriter, (parent ==
                  null ? "" : parent) + format(it.getLabel()))
         }
      }
      printWriter.flush()
   }

   def export(TimeSliderVSAssembly assembly, def printWriter) {
      SelectionList slist = (SelectionList)assembly.getSelectionList().clone()
      SelectionValue[] values = slist.getSelectionValues()

      printWriter.println("The min value is: [" + values[0].getLabel() + "]")
      printWriter.println("The max value is: [" + values[values.length - 1].getLabel() + "]")
      printWriter.println("Selected: [" + format(assembly.getSelectedMin()) + "] -> [" + format(assembly.getSelectedMax()) + "]")
      printWriter.flush()
   }

   def export(CalendarVSAssembly assembly, def printWriter) {
      String[] ranges = assembly.getRange()
      printWriter.print("The range is: ")
      ranges.each{
         printWriter.print(format(it))
      }
      printWriter.println()

      String[] dates = assembly.getDates()
      printWriter.print("The dates are: ")
      printWriter.print(dates.length == 0 ? "[No Selected]" : "")
      dates.each{
         printWriter.print(format(it))
      }
      printWriter.println()
      printWriter.flush()
   }

   def export(ListData data, def printWriter) {
      Object[] vals = (Object[])data.getValues()
      Object[] labels = (Object[])data.getLabels()
      Arrays.sort(vals, new ComparatorImpl())
      Arrays.sort(labels, new ComparatorImpl())

      printWriter.println("The value is: [" + vals.length + "]")
      vals.each {
         printWriter.println(format(it))
      }

      printWriter.println("The label is: [" + labels.length + "]")
      labels.each {
         printWriter.println(format(it))
      }
   }

   void compareData(boolean incremental) {
      File folder = new File(getExportFolderPath())
      File expFolder = new File(getExportFolderPath().replaceAll('exportedData', 'expectData'))
      File[] files = folder.listFiles(incremental ? new IncrementalMVFilter() : new MVFilter())
      File[] expFiles = expFolder.listFiles(incremental ? new IncrementalMVFilter() : new MVFilter())

      //if some component is not exported, fail case
      if (files.length != expFiles.length) {
         assert false : asset_id + " The file number is different in exp and export"
      }

      def resArray = []

      files.each {
         String resPath = it.getAbsolutePath()
           resArray.add(new CompareUtil().FileCompare(resPath))
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

   private TableLens wrapTable(TableLens lens) {
      return new AssetTableLens() {
         int getRowCount() {
            return lens.getRowCount()
         }

         int getHeaderRowCount() {
            return lens.getHeaderRowCount()
         }

         int getColCount() {
            return lens.getColCount()
         }

         boolean moreRows(int row) {
            return lens.moreRows(row)
         }

         Object getObject(int r, int c) {
            return format(lens.getObject(r, c))
         }
      }
   }

   String getExportFilePath(String assemblyName, SRPrincipal principal, String bookmark, boolean incremental) {
      String path = getExportFolderPath()
      path += File.separator + principal.getIdentityID().getName() + "_" + assemblyName +
              (bookmark == '(Home)'? '':  '_'+ bookmark) + (incremental ? MV_INCREMENTAL : MV_EXT) + ".txt"

      return path
   }

   private String getExportFolderPath() {
      String path = new File(this.getClass().getResource("/expectData").getPath()
      ).getParent()
      return path + File.separator + "exportedData" + File.separator + asset_id
            .substring(asset_id.lastIndexOf("^") + 1)
   }

   private String format(def val) {
      if(val == null || val == '') {
         val = ['NULL']
      }else if(val instanceof Date) {
         val = dateformat.format(val)
      }else if(val instanceof Float || val instanceof Double) {
         if(val == Float.NaN || val == Double.NaN){
            val = 'NaN'
         }else{
            val = numformat.format((val as Number).doubleValue())
         }
      }

      return "[" + val + "]"
   }

   static SRPrincipal createPrincipal(String userName, String[] roles, String[] groups) {
      IdentityID identityUser = new IdentityID(userName, 'host-org')
      IdentityID[] identityRoles = new IdentityID[0]

      roles.each { role ->
         IdentityID newRole = role != 'Administrator' ? new IdentityID(role, 'host-org'): new IdentityID()
         newRole.setName(role)
         identityRoles += newRole
      }

      return  new SRPrincipal(identityUser, identityRoles, groups, "host-org", Tool.getSecureRandom().nextLong())
   }

   OpenViewsheetEvent createOpenViewsheetEvent(Map<String, String[]> parameters) {
      OpenViewsheetEvent event = new OpenViewsheetEvent()
      event.setEntryId(asset_id)
      event.setViewer(true)

      if(parameters != null) {
         event.setParameters(parameters)
      }
      return event
   }

   /*
   * use to clean all properties setting for some special testing
   */
   static def clearEnv() {
      if(SreeEnv.getProperty('mv.outer.moveUp') != null) {
         SreeEnv.setProperty('mv.outer.moveUp', null)
      }
      SreeEnv.save()
   }

   private class MVFilter implements FileFilter {
      @Override
      boolean accept(File file) {
         String fileName = file.getName()
         if(fileName.indexOf(MV_EXT) > 0) {
            return true
         }
         return false
      }
   }

   private class IncrementalMVFilter implements  FileFilter {
      @Override
      boolean accept(File file) {
         String fileName = file.getName()
         if(fileName.indexOf(MV_INCREMENTAL) > 0) {
            return true
         }
         return false
      }
   }

   private class ComparatorImpl implements Comparator {
      @Override
      int compare(Object o1, Object o2) {
         if(o1 == null) {
            return -1
         }

         if(o2 == null) {
            return 1
         }

         if(o1 instanceof Comparable) {
            return ((Comparable)o1).compareTo(o2)
         }

         if(o2 instanceof Comparable) {
            return -1*((Comparable)o2).compareTo(o1)
         }

         return 0
      }
   }

   private ControllersResource controllers
   private RuntimeViewsheetResource viewsheetResource
   private String asset_id
   private final static NumberFormat numformat = new DecimalFormat("#0.####")
   private final static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd " +
         "HH:mm:ss")
   private final String MV_INCREMENTAL = "__MVIncremental"
   private final String MV_EXT = "__MV"
}

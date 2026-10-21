package inetsoft.test.mv

import inetsoft.mv.data.MVQuery
import inetsoft.mv.MVDef
import inetsoft.mv.MVManager
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.TableFilter
import inetsoft.report.TableLens
import inetsoft.report.composition.execution.AssetTableLens
import inetsoft.report.composition.execution.DataMap
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.report.composition.execution.MVInfo
import inetsoft.report.filter.SortFilter
import inetsoft.report.internal.table.XTableLens
import inetsoft.sree.SreeEnv
import inetsoft.sree.security.IdentityID
import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.CompareUtil
import inetsoft.uql.XConstants
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.AssociatedSelectionVSAssembly
import inetsoft.uql.viewsheet.BindableVSAssembly
import inetsoft.uql.viewsheet.CalendarVSAssembly
import inetsoft.uql.viewsheet.CompositeSelectionValue
import inetsoft.uql.viewsheet.ListData
import inetsoft.uql.viewsheet.SelectionList
import inetsoft.uql.viewsheet.SelectionVSAssembly
import inetsoft.uql.viewsheet.SelectionValue
import inetsoft.uql.viewsheet.TimeSliderVSAssembly
import inetsoft.uql.viewsheet.Viewsheet
import inetsoft.util.ConfigurationContext
import inetsoft.util.DataSpace
import inetsoft.util.Tool
import inetsoft.util.ThreadContext
import inetsoft.web.viewsheet.event.OpenViewsheetEvent
import inetsoft.report.composition.graph.VSDataSet
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.DatatestSpringRuntimeInitializer

import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.text.DecimalFormat

class MVTest {
   /** Derived in {@link #initHome(String)} (ViewsheetTest pattern). */
   static String suiteName

   MVTest(String asset_id) {
      ensureRuntimeInitialized()
      this.asset_id = asset_id
      materializedViewResource = new MaterializedViewResource(this.asset_id)
   }

   /**
    * Lightweight suite hook (same pattern as {@link inetsoft.test.modules.ViewsheetTest#initHome(String)}):
    * logs {@code sree.home} and derives optional {@link #suiteName} from the spec class name.
    * Does not bootstrap Spring — Spock {@code @ContextConfiguration} owns the single context and MapDB;
    * {@link #ensureRuntimeInitialized()} runs from {@link #createPrincipal} or {@code new MVTest}.
    */
   static initHome(String suiteNameArg) {
      System.err.print("=========sree.home=====" + System.getProperty("sree.home") + "\n")
      def arrs = suiteNameArg.split('.cases')
      suiteName = arrs.length == 1 ? null : arrs[1].replace('.', '/')
   }

   private static synchronized ensureRuntimeInitialized() {
      ConfigurationContext ctx = ConfigurationContext.getContext()
      String home = System.getProperty('sree.home', '.')
      DatatestSpringRuntimeInitializer.ensureInitialized(home)

      DataSpace.getDataSpace()
   }

   List getMVDefInfo(List<TableLens> datas) {
      if(datas == null || datas.size() == 0) {
         return null
      }

      def allMVInfos = []
      int count = 0
      for(n in 0..<datas.size()) {
         if(datas[n] != null) {
            def mvInfos = []
            List<MVInfo> list = MVQuery.getMVInfos(datas[n])

            if(list != null && list.size() > 0) {
               for(i in 0..<list.size()) {
                  mvInfos[i] = list[i].getMVTable() + "|" + (list[i].isSub() ? "SubMV" :
                          "TopMV")
               }
            }
            else {
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

      // errors left by an earlier call that was cut short by an exception must not be reported
      executionErrors.clear()

      bks.each {
         executeTest(params, it, incremental, exportSelection, principal)
      }

      // fail after all bookmarks ran, so every broken component is listed in one report
      if(!executionErrors.isEmpty()) {
         String msg = asset_id + " failed to execute " + executionErrors.size() +
                 " component(s):\n" + executionErrors.join("\n")
         executionErrors.clear()
         throw new AssertionError(msg)
      }
   }

   def executeTest(Map<String, String[]> params, String bk, boolean incremental, boolean exportSelection, SRPrincipal principal) {
      if(principal == null) {
         principal = createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }

      ThreadContext.setContextPrincipal(principal)
      viewsheetResource = new RuntimeViewsheetResource(createOpenViewsheetEvent(params))
      viewsheetResource.initRuntimeVS(principal)

      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      if(bk != null) {
         rvs.gotoBookmark(bk, principal.getUser().getUserIdentity(), principal)
         Optional<ViewsheetSandbox> sandboxOpt = rvs.getViewsheetSandbox();
         if(sandboxOpt.isPresent()) {
            sandboxOpt.get().resetAll(new ChangedAssemblyList());
         }
      }
      Viewsheet vs = rvs.getViewsheet()
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      Assembly[] assemblies = vs.getAssemblies()
      def datas = []
      def assemblyNames = []
      def tableLens = []
      int n = 0

      assemblies.each {
         if(it instanceof SelectionVSAssembly) {
            recordMVLookupHit(rvs, sandbox, (SelectionVSAssembly) it, principal)

            if(exportSelection) {
               assemblyNames.add(it.getName())
               if(it instanceof AssociatedSelectionVSAssembly) {
                  datas.add(((AssociatedSelectionVSAssembly) it).getSelectionList())
               }
               else if(it instanceof TimeSliderVSAssembly) {
                  datas.add((TimeSliderVSAssembly) it)
               }
               else if(it instanceof CalendarVSAssembly) {
                  datas.add((CalendarVSAssembly) it)
               }
            }
         }
         else {
            try {
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
               else if(data != null) {
                  // plain values (text, gauge, ...): the table behind them can not be inspected
                  hitUnobservable = true

                  if(it instanceof BindableVSAssembly) {
                     recordMVLookupHit(rvs, sandbox, (BindableVSAssembly) it, principal)
                  }
               }
            }
            catch(Exception e) {
               // assemblies without data (current selection, shapes, annotations) can not be
               // queried and are not exported, which is not a failure
               if(e.getMessage() == null || !e.getMessage().startsWith("Unsupported assembly found")) {
                  e.printStackTrace()
                  executionErrors.add("[" + bk + "] " + it.getName() + ": " + e)
               }
            }
            n++
         }
      }

      for(i in 0..<datas.size()) {
         exportData(datas[i], getExportFilePath(assemblyNames[i] as String,
                 principal, bk, incremental))
      }

      recordMVHits(tableLens)
      return tableLens
   }

   /**
    * Remember the MVs the executed tables were actually read from.
    */
   private void recordMVHits(List tableLens) {
      tableLens.each { lens ->
         if(lens != null) {
            List<MVInfo> infos = findMVInfos((TableLens) lens)

            infos?.each { info ->
               hitMVs.add(info.getMVTable() + "|" + (info.isSub() ? "SubMV" : "TopMV"))
            }
         }
      }
   }

   /**
    * Remember the MV a component resolves to. Selection lists, time sliders, calendars and
    * components that show a plain value (text, ...) are not tables, a time slider even reads the
    * range from the MV definition without running a query, so no table carries the MV
    * information. The lookup is the one the sandbox does for the component, so it tells which MV
    * the component can use, not that rows were read from it.
    */
   private void recordMVLookupHit(RuntimeViewsheet rvs, ViewsheetSandbox sandbox,
                                  BindableVSAssembly assembly, SRPrincipal principal)
   {
      try {
         String tname = assembly.getTableName()

         if(tname == null) {
            return
         }

         if(tname.startsWith(Assembly.TABLE_VS)) {
            tname = tname.substring(Assembly.TABLE_VS.length())
         }

         def rmv = MVManager.getManager().findRuntimeMV(
                 rvs.getEntry(), rvs.getViewsheet(), assembly.getName(), tname, principal,
                 sandbox, true, true)
         MVDef mvdef = rmv == null ? null : MVManager.getManager().get(rmv.getMV())

         if(mvdef != null) {
            hitMVs.add(mvdef.getMVTable() + "|" + (mvdef.isSub() ? "SubMV" : "TopMV"))
         }
      }
      catch(Exception e) {
         // findRuntimeMV(findOnly) returns null when the component has no MV, so an exception
         // is a real problem. Swallowing it would turn the check back into the weak one.
         e.printStackTrace()
         executionErrors.add("[MV lookup] " + assembly.getName() + ": " + e)
      }
   }

   /**
    * Get the MV information of a table. A snapshot (e.g. the data of a chart) hides the table
    * the MV was read for, so it is unwrapped.
    */
   private List<MVInfo> findMVInfos(TableLens lens) {
      List<MVInfo> infos = MVQuery.getMVInfos(lens)

      if(infos != null && !infos.isEmpty()) {
         return infos
      }

      def leaf = lens
      int depth = 0

      while(leaf instanceof TableFilter && depth++ < 100) {
         leaf = ((TableFilter) leaf).getTable()
      }

      // a snapshot of a table, e.g. the data of a chart
      if(leaf instanceof XTableLens) {
         def inner = ((XTableLens) leaf).getTable()

         if(inner instanceof TableLens && !inner.is(lens)) {
            return findMVInfos((TableLens) inner)
         }

         // the data was already copied into memory, which drops the MV information
         hitUnobservable = true

         if(!loggedUnobservable) {
            loggedUnobservable = true
            System.err.println("MVTest: can not tell which MV was used behind " +
                    leaf.getClass().getName() + " wrapping " + inner?.getClass()?.getName())
         }
      }

      return infos
   }

   /**
    * Get the MVs that the queries executed by this instance actually hit. Unlike
    * {@link #getMVDefInfo()}, an MV that is only defined but not used by the query is not
    * included. For selection components and components that show a plain value only the MV
    * found by the sandbox lookup is recorded, see {@link #recordMVLookupHit}. When nothing could
    * be observed because every component returned a plain value (text, ...), the defined MVs are
    * returned as before.
    */
   List getMVHitInfo() {
      if(hitMVs.isEmpty() && hitUnobservable) {
         return getMVDefInfo()
      }

      return hitMVs.toList()
   }

   def exportData(def data, String fileName) {
      File file = new File(fileName)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      }
      else if(file.exists()) {
         file.delete()
      }

      def printWriter = file.newPrintWriter()

      if(data == null || data == '') {
         data = ['NULL']
      }

      if(data instanceof Object[] && !(data instanceof TableLens[])) {
         Object[] vals = (Object[]) data
         printWriter.println("The length of the array is: [" + vals.length + "]")

         vals.each {
            printWriter.println(format(it))
         }
      }
      else if(data instanceof TableLens[]) {
         TableLens[] tables = (TableLens[]) data
         tables.each {
            export(it, printWriter)
         }
      }
      else if(data instanceof TableLens) {
         export((TableLens) data, printWriter)
      }
      else if(data instanceof SelectionList) {
         export((SelectionList) data, printWriter)
      }
      else if(data instanceof TimeSliderVSAssembly) {
         export((TimeSliderVSAssembly) data, printWriter)
      }
      else if(data instanceof CalendarVSAssembly) {
         export((CalendarVSAssembly) data, printWriter)
      }
      else if(data instanceof ListData) {
         export((ListData) data, printWriter)
      }
      else if(data instanceof StringBuffer) {
         printWriter.println(data.toString())
      }
      else if(data != null) {
         printWriter.println(format(data))
      }

      printWriter.flush()
      printWriter.close()
   }

   def createMV(boolean applyVPM, boolean ... expandGroup) {
      materializedViewResource.createMV(applyVPM, expandGroup)
   }

   def removeMV() {
      materializedViewResource.removeMV()
   }

   def createIncrementMV(int count) {
      materializedViewResource.createIncrementMV(count)
   }

   def export(TableLens table, def printWriter) {
      table = new SortFilter(table)
      table = wrapTable(table)
      StringBuffer buffer = new StringBuffer()
      int row = 0
      while(table.moreRows(row)) {
         for(int col = 0; col < table.getColCount(); col++) {
            buffer.append(table.getObject(row, col))
         }
         buffer.append("\n")
         row++
      }

      printWriter.println("The table size(row x col) is: (" + row + " x " + table
              .getColCount() + ")")
      printWriter.println(buffer.toString())
      printWriter.flush()
   }

   def export(SelectionList list, def printWriter) {
      list = (SelectionList) list.clone()
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
         if(it.getMeasureLabel() != null) {
            printWriter.print("   Measure:" + format(it.getMeasureLabel()))
         }
         printWriter.println()

         if(it instanceof CompositeSelectionValue) {
            export(((CompositeSelectionValue) it).getSelectionList(), printWriter, (parent ==
                    null ? "" : parent) + format(it.getLabel()))
         }
      }
      printWriter.flush()
   }

   def export(TimeSliderVSAssembly assembly, def printWriter) {
      SelectionList slist = (SelectionList) assembly.getSelectionList().clone()
      SelectionValue[] values = slist.getSelectionValues()

      printWriter.println("The min value is: [" + values[0].getLabel() + "]")
      printWriter.println("The max value is: [" + values[values.length - 1].getLabel() + "]")
      printWriter.println("Selected: [" + format(assembly.getSelectedMin()) + "] -> [" + format(assembly.getSelectedMax()) + "]")
      printWriter.flush()
   }

   def export(CalendarVSAssembly assembly, def printWriter) {
      String[] ranges = assembly.getRange()
      printWriter.print("The range is: ")
      ranges.each {
         printWriter.print(format(it))
      }
      printWriter.println()

      String[] dates = assembly.getDates()
      printWriter.print("The dates are: ")
      printWriter.print(dates.length == 0 ? "[No Selected]" : "")
      dates.each {
         printWriter.print(format(it))
      }
      printWriter.println()
      printWriter.flush()
   }

   def export(ListData data, def printWriter) {
      Object[] vals = (Object[]) data.getValues()
      Object[] labels = (Object[]) data.getLabels()
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

      // listFiles returns null when the directory does not exist
      if(files == null) {
         assert false: asset_id + " nothing was exported: " + folder
      }

      if(expFiles == null) {
         assert false: asset_id + " no expected data found: " + expFolder
      }

      //if some component is not exported, fail case
      if(files.length != expFiles.length) {
         assert false: asset_id + " The file number is different in exp and export"
      }

      def resArray = []

      files.each {
         String resPath = it.getAbsolutePath()
         resArray.add(new CompareUtil().FileCompare(resPath))
      }

      def failures = []
      resArray.each {
         if(it.getAt('false') != null) {
            failures.add(it.getAt('false'))
         }
      }

      if(!failures.isEmpty()) {
         assert false: failures.join("\n")
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
              (bookmark == '(Home)' ? '' : '_' + bookmark) + (incremental ? MV_INCREMENTAL : MV_EXT) + ".txt"

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
      }
      else if(val instanceof Date) {
         val = dateformat.format(val)
      }
      else if(val instanceof Float || val instanceof Double) {
         if(Double.isNaN((val as Number).doubleValue())) {
            val = 'NaN'
         }
         else {
            val = numformat.format((val as Number).doubleValue())
         }
      }

      return "[" + val + "]"
   }

   /**
    * SRPrincipal triggers XSessionService via Spring; {@link #ensureRuntimeInitialized()} must run first.
    * Call from {@code setup()} (after Spock-Spring loaded context), not {@code setupSpec()}.
    */
   static SRPrincipal createPrincipal(String userName, String[] roles, String[] groups) {
      ensureRuntimeInitialized()
      IdentityID identityUser = new IdentityID(userName, 'host-org')
      IdentityID[] identityRoles = new IdentityID[0]

      roles.each { role ->
         IdentityID newRole = role != 'Administrator' ? new IdentityID(role, 'host-org') : new IdentityID()
         newRole.setName(role)
         identityRoles += newRole
      }

      SRPrincipal principal = new SRPrincipal(identityUser, identityRoles, groups, 'host-org', Tool.getSecureRandom().nextLong())
      principal.setIgnoreLogin(true)
      // mark it as a logged-in (non-SSO) principal like SUtil.getPrincipal() does, so its roles
      // and groups are resolved through the security provider, including parent groups and
      // inherited roles, instead of only the ones passed in here
      principal.setProperty('__internal__', 'true')

      return principal
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
         return fileName.indexOf(MV_EXT) > 0 && fileName.indexOf(MV_INCREMENTAL) < 0
      }
   }

   private class IncrementalMVFilter implements FileFilter {
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
            return ((Comparable) o1).compareTo(o2)
         }

         if(o2 instanceof Comparable) {
            return -1 * ((Comparable) o2).compareTo(o1)
         }

         return 0
      }
   }

   private MaterializedViewResource materializedViewResource
   private final List<String> executionErrors = []
   private final Set<String> hitMVs = new LinkedHashSet<String>()
   private boolean hitUnobservable
   private boolean loggedUnobservable
   private RuntimeViewsheetResource viewsheetResource
   private String asset_id
   private final static NumberFormat numformat = new DecimalFormat("#0.####")
   private final static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd " +
           "HH:mm:ss")
   private final String MV_INCREMENTAL = "__MVIncremental"
   private final String MV_EXT = "__MV"
}

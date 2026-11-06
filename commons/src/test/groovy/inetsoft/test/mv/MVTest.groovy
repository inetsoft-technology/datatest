package inetsoft.test.mv

import inetsoft.mv.data.MVQuery
import inetsoft.mv.MVDef
import inetsoft.mv.MVManager
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.TableFilter
import inetsoft.report.TableLens
import inetsoft.report.XSessionManager
import inetsoft.report.composition.execution.AssetDataCache
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
   /**
    * How the MV results are checked, from the system property {@code mv.compare}:
    * {@code golden} (default) compares them with {@code expectData} only, {@code nomv} compares
    * them with the result without MV ({@link #compareWithoutMV()}) in the cases that call
    * {@link #executeVSWithoutMV}, and {@code both} does both. An invalid value fails each case
    * in {@link #MVTest(String)}.
    */
   static final String COMPARE_MODE = System.getProperty('mv.compare', 'golden')

   /** Derived in {@link #initHome(String)} (ViewsheetTest pattern). */
   static String suiteName

   MVTest(String asset_id) {
      this(asset_id, null)
   }

   /**
    * @param worksheetId the identifier of the worksheet to create the MVs for (a worksheet MV),
    *                    or null to create them for the viewsheet asset_id, see
    *                    {@link MaterializedViewResource#MaterializedViewResource(String, String)}
    */
   MVTest(String asset_id, String worksheetId) {
      if(!(COMPARE_MODE in ['golden', 'nomv', 'both'])) {
         throw new IllegalArgumentException("mv.compare must be golden, nomv or both: " + COMPARE_MODE)
      }

      ensureRuntimeInitialized()
      this.asset_id = asset_id
      this.worksheetId = worksheetId
      materializedViewResource = new MaterializedViewResource(this.asset_id, worksheetId)
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

      lastBookmarkCount = bks.length

      // fail after all bookmarks ran, so every broken component is listed in one report
      if(!executionErrors.isEmpty()) {
         String msg = asset_id + " failed to execute " + executionErrors.size() +
                 " component(s):\n" + executionErrors.join("\n")
         executionErrors.clear()
         throw new AssertionError(msg)
      }
   }

   /**
    * Execute the viewsheet without MV and export the data with the {@link #NO_MV_EXT} suffix, as
    * the reference that {@link #compareWithoutMV()} compares the MV results with. It must run
    * before {@link #createMV}: the MV does not exist yet, and a run after it could read the MV
    * result from the data cache. The caches are cleared afterwards, so the MV run does not read
    * this result either. See {@link #compareWithoutMV()} for the cases it applies to.
    * <p>
    * Does nothing when {@link #COMPARE_MODE} is {@code golden}.
    */
   def executeVSWithoutMV(Map<String, String[]> params, String[] bks, boolean incremental, boolean exportSelection) {
      executeVSWithoutMV(params, bks, incremental, exportSelection, null)
   }

   def executeVSWithoutMV(Map<String, String[]> params, String[] bks, boolean incremental,
                          boolean exportSelection, SRPrincipal principal)
   {
      if(COMPARE_MODE == 'golden') {
         return
      }

      if(mvCreated) {
         throw new IllegalStateException(asset_id + ": executeVSWithoutMV must run before createMV")
      }

      withoutMV = true

      try {
         executeVS(params, bks, incremental, exportSelection, principal)
         ranWithoutMV = true

         // findRuntimeMV also finds an MV left by an earlier case or created by another viewsheet
         if(!hitMVs.isEmpty()) {
            throw new AssertionError(asset_id + " used MV " + hitMVs + " before its MV was created")
         }
      }
      finally {
         withoutMV = false
         hitMVs.clear()
         hitUnobservable = false
         AssetDataCache.getCache().clearCache()
         XSessionManager.getSessionManager().clearCache()
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

      lastRuntime = rvs
      lastBookmark = bk
      lastPrincipal = principal
      return exportRuntime(rvs, bk, incremental, exportSelection, principal)
   }

   /**
    * Refresh the viewsheet opened by the last {@link #executeVS} call, as the Refresh button of
    * the viewer does ({@code VSRefreshService}: {@code RuntimeViewsheet.resetRuntime()}, then
    * the assemblies are reset), and export its data again. The viewsheet is not opened again,
    * and the caches are not cleared, so the export shows what a user who keeps the viewsheet
    * open sees after the refresh. The last executeVS must have run one bookmark: only the
    * viewsheet of its last bookmark is kept.
    */
   def refreshVS(boolean incremental, boolean exportSelection) {
      if(lastRuntime == null) {
         throw new IllegalStateException(asset_id + ": refreshVS needs executeVS")
      }

      // only the viewsheet of the last bookmark is kept open
      if(lastBookmarkCount > 1) {
         throw new IllegalStateException(asset_id + ": refreshVS needs executeVS with one bookmark")
      }

      executionErrors.clear()
      ThreadContext.setContextPrincipal(lastPrincipal)
      lastRuntime.resetRuntime()
      lastRuntime.getViewsheetSandbox().get().resetAll(new ChangedAssemblyList())
      exportRuntime(lastRuntime, lastBookmark, incremental, exportSelection, lastPrincipal)

      if(!executionErrors.isEmpty()) {
         String msg = asset_id + " failed to refresh " + executionErrors.size() +
                 " component(s):\n" + executionErrors.join("\n")
         executionErrors.clear()
         throw new AssertionError(msg)
      }
   }

   private def exportRuntime(RuntimeViewsheet rvs, String bk, boolean incremental,
                             boolean exportSelection, SRPrincipal principal)
   {
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
      exportedFiles.add(new File(fileName).getCanonicalPath())
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
      mvCreated = true
      createdApplyVPM = applyVPM
      createdExpandGroup = expandGroup
      materializedViewResource.createMV(applyVPM, expandGroup)
   }

   /**
    * Get the MVs hit since the last call (see {@link #getMVHitInfo()}) and forget them, so an
    * execution, e.g. the one after an incremental update, is checked on its own. Otherwise the
    * hits of all executions of the case add up.
    */
   List takeMVHits() {
      List hits = getMVHitInfo()
      hitMVs.clear()
      hitUnobservable = false
      return hits
   }

   /**
    * Get the worksheet MVs created by this instance that the executions since the last call
    * actually read, and forget the hits like {@link #takeMVHits()}. An MV counts only when it is
    * a worksheet MV ({@code MVDef.isWSMV()}) of the worksheet given to the constructor and a hit
    * names its MV table. Hits come from the MV information of the executed tables, and for
    * selections and components that show a plain value from the sandbox lookup of the viewsheet
    * (see {@link #recordMVLookupHit}), which does not find worksheet MVs.
    * <p>
    * A hit only names the MV table, which is the same for every MV of the worksheet table, so
    * the check fails when another worksheet MV of the worksheet exists, e.g. one left by an
    * earlier run in the shared storage. When the executed tables did not show which MV they
    * read, the check fails instead of falling back to the defined MVs, which would also pass
    * when the data came from the database.
    */
   List<String> takeWSMVHits() {
      if(worksheetId == null) {
         throw new IllegalStateException(asset_id + ": takeWSMVHits needs a worksheet MV")
      }

      boolean unobservable = hitUnobservable
      Set<String> hits = new LinkedHashSet<>(hitMVs)
      hitMVs.clear()
      hitUnobservable = false
      assert !unobservable: asset_id + ": the executed tables do not show which MV they read"

      MVManager manager = MVManager.getManager()
      List<String> views = materializedViewResource.getViews()
      List<String> others = manager.list(false).findAll { MVDef mvdef ->
         mvdef.isWSMV() && worksheetId == mvdef.getWsId() && !views.contains(mvdef.getName())
      }*.getName()
      assert others.isEmpty(): asset_id + ": other worksheet MVs of the worksheet exist, " +
         "a hit can not tell them apart: " + others

      return views.findAll { String name ->
         MVDef mvdef = manager.get(name)

         mvdef != null && mvdef.isWSMV() && worksheetId == mvdef.getWsId() &&
            hits.contains(mvdef.getMVTable() + "|" + (mvdef.isSub() ? "SubMV" : "TopMV"))
      }
   }

   /** The names of the MVs created by this instance. */
   List<String> materializedViewNames() {
      return materializedViewResource.getViews()
   }

   /**
    * Set the last update time of the MVs created by this instance, which is the value of
    * {@code MV.LastUpdateTime} in the MV conditions of the next incremental update. MVDef has
    * no setter, and the update runs on a background thread where {@code data.cache.ts} can not
    * be set, so the field is set and the def is stored again, as MVManager does after an update.
    * The update reads the def from MVManager. A viewsheet with selections also has association
    * MVs, all of them are changed.
    * <p>
    * The private field {@code MVDef.lastUpdateTime} is set by reflection. If it is renamed or
    * removed, {@code getDeclaredField} throws {@code NoSuchFieldException} and every case that
    * calls this method fails; change the field name here.
    */
   void setLastUpdateTime(long time) {
      List<String> views = materializedViewResource.getViews()

      if(views.isEmpty()) {
         throw new IllegalStateException(asset_id + ": setLastUpdateTime needs createMV")
      }

      def field = MVDef.getDeclaredField('lastUpdateTime')
      field.setAccessible(true)
      MVManager manager = MVManager.getManager()

      views.each { String name ->
         MVDef mvdef = manager.get(name)
         field.setLong(mvdef, time)
         manager.add(mvdef)
      }

      views.each { String name ->
         assert manager.get(name).getLastUpdateTime() == time: name + ": last update time not set"
      }
   }

   /**
    * Remove the MVs, create them again on the current data in the same way as
    * {@link #createMV}, and execute the viewsheet. Its export is the expected result of the
    * incremental update, see {@link #compareWithRebuild()}. The hits recorded before are
    * dropped, so {@link #takeMVHits()} afterwards returns the hits of the rebuilt MV.
    */
   def rebuildMV(Map<String, String[]> params, String[] bks, boolean exportSelection) {
      if(!mvCreated) {
         throw new IllegalStateException(asset_id + ": rebuildMV needs createMV")
      }

      removeMV()
      createMV(createdApplyVPM, createdExpandGroup)
      AssetDataCache.getCache().clearCache()
      XSessionManager.getSessionManager().clearCache()
      takeMVHits()
      // compareWithRebuild then knows that an __MV file was written by this run, without
      // comparing modification times, which some file systems keep only to the second
      new File(getExportFolderPath()).listFiles(new MVFilter())?.each { it.delete() }
      rebuildTime = System.currentTimeMillis()
      executeVS(params, bks, false, exportSelection)
   }

   /**
    * Compare the data exported after the incremental update ({@code __MVIncremental}) with the
    * data of the MV rebuilt on the final data by {@link #rebuildMV} ({@code __MV}). Both must
    * be the same: the update appends and deletes the rows that a new MV would have.
    * <p>
    * Only for MV conditions that do not use {@code MV.} variables in the delete condition. Such
    * a delete condition is ignored when the MV is created ({@code MVCreatorUtil.isConditionValid}),
    * so a rebuilt MV keeps the rows the update deletes, unless the case deleted them from the
    * database too. Append conditions with {@code MV.} variables are fine: they are also ignored
    * when the MV is created, and a new MV has all the rows.
    */
   void compareWithRebuild() {
      if(rebuildTime == 0) {
         assert false: asset_id + " compareWithRebuild needs rebuildMV"
      }

      File folder = new File(getExportFolderPath())
      // the files this instance exported, not the folder: an earlier case of the same viewsheet
      // leaves its files there, and modification times kept to the second can not tell them
      // apart from the files of this case
      File[] files = exported(MV_INCREMENTAL)

      if(files.length == 0) {
         assert false: asset_id + " nothing was exported after the incremental update: " + folder
      }

      def failures = []

      files.each {
         File rebuilt = new File(folder, it.getName().replace(MV_INCREMENTAL + ".txt", MV_EXT + ".txt"))

         // rebuildMV deleted the old __MV files, so an existing one was exported by the rebuild
         if(!rebuilt.exists()) {
            failures.add(it.getName() + ": not exported after the MV was rebuilt")
         }
         else {
            List<String> incrementalLines = it.readLines('UTF-8')
            List<String> rebuiltLines = rebuilt.readLines('UTF-8')

            if(incrementalLines != rebuiltLines) {
               failures.add(it.getName() + ": differs from the rebuilt MV\n" +
                       firstDifference(incrementalLines, rebuiltLines, "incremental", "rebuilt"))
            }
         }
      }

      if(!failures.isEmpty()) {
         assert false: asset_id + " incremental results differ from the rebuilt MV:\n" +
                 failures.join("\n")
      }
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

   /**
    * Compare the exported data with {@code expectData}. Skipped when {@link #COMPARE_MODE} is
    * {@code nomv} and this case compared its MV results with {@link #compareWithoutMV()};
    * incremental results and cases without a run without MV are always compared.
    */
   void compareData(boolean incremental) {
      if(COMPARE_MODE == 'nomv' && ranWithoutMV && !incremental) {
         return
      }

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

   /**
    * Compare the data exported with MV with the data exported by {@link #executeVSWithoutMV}.
    * Each exported MV file must have a file without MV with the same content.
    * <p>
    * This only shows that the MV does not change the result. A bug in the code shared by both
    * paths (aggregation, calculated fields, formatting, ...) gives the same wrong result twice
    * and is only found by {@link #compareData} ({@code golden} or {@code both}).
    * <p>
    * Only for a full MV of a table without MV delete conditions. When the MV is created, the
    * negated delete conditions are added to the table conditions, and an incremental update
    * appends and deletes rows of the MV, so in both cases the MV does not hold what the
    * database returns. Files of an incremental update ({@code __MVIncremental}) are not
    * compared.
    * <p>
    * Does nothing when {@link #COMPARE_MODE} is {@code golden}.
    */
   void compareWithoutMV() {
      if(COMPARE_MODE == 'golden') {
         return
      }

      if(!ranWithoutMV) {
         assert false: asset_id + " compareWithoutMV needs executeVSWithoutMV before createMV"
      }

      File folder = new File(getExportFolderPath())
      // the files this instance exported, see compareWithRebuild
      File[] files = exported(MV_EXT)

      if(files.length == 0) {
         assert false: asset_id + " nothing was exported with MV: " + folder
      }

      def failures = []

      files.each {
         File noMV = new File(folder, it.getName().replace(MV_EXT + ".txt", NO_MV_EXT + ".txt"))

         if(!noMV.exists()) {
            failures.add(it.getName() + ": not exported without MV")
         }
         else if(!exportedFiles.contains(noMV.getCanonicalPath())) {
            // left by an earlier case or run, this case did not call executeVSWithoutMV for it
            failures.add(it.getName() + ": not exported without MV in this run")
         }
         else {
            List<String> mvLines = it.readLines('UTF-8')
            List<String> noMVLines = noMV.readLines('UTF-8')

            if(mvLines != noMVLines) {
               failures.add(it.getName() + ": differs from the result without MV\n" +
                       firstDifference(mvLines, noMVLines))
            }
         }
      }

      if(!failures.isEmpty()) {
         assert false: asset_id + " MV results differ from the results without MV:\n" +
                 failures.join("\n")
      }
   }

   /**
    * The files with the suffix (e.g. {@link #MV_INCREMENTAL}) that this instance exported and
    * that still exist (rebuildMV deletes the {@link #MV_EXT} files).
    */
   private File[] exported(String suffix) {
      return exportedFiles.findAll { it.endsWith(suffix + ".txt") }
         .collect { new File(it) }.findAll { it.exists() } as File[]
   }

   private static String firstDifference(List<String> mvLines, List<String> noMVLines) {
      return firstDifference(mvLines, noMVLines, "with MV", "without MV")
   }

   private static String firstDifference(List<String> lines1, List<String> lines2,
                                         String name1, String name2)
   {
      int n = Math.min(lines1.size(), lines2.size())
      int width = Math.max(name1.length(), name2.length()) + 1

      for(int i = 0; i < n; i++) {
         if(lines1[i] != lines2[i]) {
            return "  line " + (i + 1) + "\n    " + (name1 + ":").padRight(width) + " " + lines1[i] +
                    "\n    " + (name2 + ":").padRight(width) + " " + lines2[i]
         }
      }

      return "  " + name1 + " " + lines1.size() + " lines, " + name2 + " " + lines2.size() + " lines"
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
              (bookmark == '(Home)' ? '' : '_' + bookmark) +
              (withoutMV ? NO_MV_EXT : incremental ? MV_INCREMENTAL : MV_EXT) + ".txt"

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
   private boolean withoutMV
   private boolean mvCreated
   private boolean ranWithoutMV
   private boolean createdApplyVPM
   private boolean[] createdExpandGroup
   private long rebuildTime
   // the paths exported by this instance, for compareWithRebuild
   private final Set<String> exportedFiles = new LinkedHashSet<>()
   private RuntimeViewsheetResource viewsheetResource
   // the viewsheet opened by the last executeTest, for refreshVS
   private RuntimeViewsheet lastRuntime
   private String lastBookmark
   private int lastBookmarkCount
   private SRPrincipal lastPrincipal
   private String asset_id
   private String worksheetId
   private final static NumberFormat numformat = new DecimalFormat("#0.####")
   private final static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd " +
           "HH:mm:ss")
   private final String MV_INCREMENTAL = "__MVIncremental"
   private final String MV_EXT = "__MV"
   private final String NO_MV_EXT = "__NoMV"
}

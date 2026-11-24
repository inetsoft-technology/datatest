package inetsoft.test.mv.cases.others

import inetsoft.mv.DateMVColumn
import inetsoft.mv.MVDef
import inetsoft.mv.MVManager
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVTest
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.ColumnRef
import inetsoft.uql.asset.ConcatenatedTableAssembly
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.erm.AttributeRef
import inetsoft.uql.viewsheet.SelectionList
import inetsoft.uql.viewsheet.SelectionListVSAssembly
import inetsoft.uql.viewsheet.SelectionValue
import inetsoft.uql.viewsheet.Viewsheet
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

/**
 * {@code mv.*} properties that change how a table is materialized: each case sets a property
 * with {@link MVTest#setProperties} ({@code @SreeProperty} has no effect in a spec), runs an
 * existing viewsheet or a run-time copy of one, and restores the property in {@code cleanup()}.
 * The checks that matter are on the export of the MV run itself (the MV hit, the row counts, the
 * materialized columns), so they also apply in the default {@code golden} mode, where
 * {@link MVTest#compareWithoutMV()} does nothing. {@code mv.preferred.block} is in
 * {@code IncrementalDataChange_Spec}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class MVProperties_Spec extends Specification {
   static final String PATH = 'submv/union_properties'
   // MVTest names the export folder after the part of the id after the last ^, so no org
   static final String VS_ID = '1^128^__NULL__^' + PATH
   static final String WS_ENTRY = '1^2^__NULL__^' + PATH + '^host-org'
   static final String VS_ENTRY = VS_ID + '^host-org'

   // the selection columns and the expected hits of the union cases
   static final List<String> NAMES = ['CATEGORY_NAME', 'REGION']
   static final List<String> IDS = ['CATEGORY_ID', 'REGION_ID']
   static final List<String> TOP = ['Query1|TopMV']
   static final List<String> SUB = ['CATEGORIES1|SubMV', 'REGIONS1|SubMV']

   @Shared def admin
   def mvtest
   Map<String, String> previous
   boolean copied

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }

   def cleanup() {
      def repository = AssetUtil.getAssetRepository(false)
      Throwable failure = null
      def steps = [{ mvtest?.removeMV() }, { MVTest.restoreProperties(previous) }]

      if(copied) {
         steps << { repository.removeSheet(AssetEntry.createAssetEntry(VS_ENTRY), admin, true) }
         steps << { repository.removeSheet(AssetEntry.createAssetEntry(WS_ENTRY), admin, true) }
      }

      // every step runs even when an earlier one fails, so nothing is left for the next case
      steps.each { Closure step ->
         try {
            step()
         }
         catch(Throwable e) {
            if(failure == null) {
               failure = e
            }
            else {
               failure.addSuppressed(e)
            }
         }
      }

      if(failure != null) {
         throw failure
      }
   }

   // The selection on the inner side of an outer join (the right table of a left join, the left
   // table of a right join) is refused by default (fault outerJoin) and the tables get SubMVs.
   // With mv.outer.moveUp=true it is moved up to the join as "condition OR column IS NULL", only
   // an approximation (SelectionUpTransformer.addOuterCondition0): an outer row whose inner
   // matches all fail the condition is deleted instead of kept with NULLs. The MV result loses
   // rows; a known limitation, the property stays as it is (#81658). Without MV, and with
   // the default false (SubMVs of the two tables), (Home) and every bookmark have 7 rows (left)
   // and 35 rows (right), as the golden files of SubMV_Others_Spec.
   @Issue('https://issues.inetsoft.com/issues/81658')
   def 'mv.outer.moveUp=true, #name: a TopMV on the join, and the bookmarks lose rows (#81658)'() {
      given:
      previous = MVTest.setProperties(['mv.outer.moveUp': 'true'])
      mvtest = new MVTest('1^128^__NULL__^submv/' + name)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, rows.keySet() as String[], false, false)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll([hit])
         !mvtest.getMVHitInfo().any { it.endsWith('|SubMV') }
         rows.collectEntries { bk, n -> [bk, rowCount('submv/' + name, 'Query1', bk)] } == rows
      }

      where:
      name                           | hit            | rows
      'left outer join-select right' | 'Query1|TopMV' | ['(Home)': 7, 'bk1': 6]
      'right outer join-select left' | 'Query2|TopMV' | ['(Home)': 35, 'bk1': 29, 'bk2': 31]
   }

   // A selection whose table is the source of another selection's table (nested selections) is
   // moved up only when mv.ignore.nestedSelection is true, the default (defaults.properties).
   // With false, the analysis keeps the selection on a SubMV of the table instead of the TopMV
   // of the default, without a warning, and the results are those of the golden files of
   // TopMV_Others_Spec.
   def 'mv.ignore.nestedSelection=false, #name: a SubMV instead of a TopMV, the same results'() {
      given:
      previous = MVTest.setProperties(['mv.ignore.nestedSelection': 'false'])
      String[] bks = ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4']
      mvtest = new MVTest('1^128^__NULL__^topmv/' + name)

      when:
      mvtest.executeVSWithoutMV(null, bks, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, bks, false, true)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll([hit])
         !mvtest.getMVHitInfo().contains(byDefault)
         mvtest.getAnalysisWarnings() == []
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

      where:
      name                | hit              | byDefault
      'selection nested'  | 'Query1_O|SubMV' | 'Query1|TopMV'
      'selection nested1' | 'Query2_O|SubMV' | 'Query2|TopMV'
   }

   // date.common.options lists the date levels that the MV materializes as real columns
   // (DateMVColumn.createAll, read when the MV is defined); the others are computed from the
   // date when they are queried. Not set: year and month; "": none. The charts group ORDER_DATETIME
   // by year and by month; whatever is materialized, the results are those of the golden files
   // of MVHit_GroupAndAggregates_Spec.
   def 'date.common.options #label: the real date columns are #real, the same results'() {
      given:
      previous = MVTest.setProperties(['date.common.options': value])
      String[] bks = ['(Home)', 'bk1']
      mvtest = new MVTest('1^128^__NULL__^hit mv/chart/chart2')

      when:
      mvtest.executeVSWithoutMV(null, bks, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, bks, false, true)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3|TopMV'])
         realDateColumns('Query3') == real
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

      where:
      label         | value                         | real
      'not set'     | null                          | ['Month(ORDER_DATETIME)', 'Year(ORDER_DATETIME)']
      'empty'       | ''                            | []
      'five levels' | 'year,quarter,month,week,day' | ['Day(ORDER_DATETIME)', 'Month(ORDER_DATETIME)', 'Quarter(ORDER_DATETIME)', 'Week(ORDER_DATETIME)', 'Year(ORDER_DATETIME)']
   }

   // A distinct union Query1 of CATEGORIES1 (CATEGORY_ID 1..7, CATEGORY_NAME) and REGIONS1
   // (REGION_ID 1..2, REGION "USA East", "USA West"), a copy of submv/union-select in both
   // childs with one selection list on each child. A selection moves up to the union only when
   // mv.union.moveUp is true, the union is distinct, and every child has a selection on the same
   // column position (SelectionUpTransformer.isMoveableToUnionTable); the union then gets one
   // TopMV and the selections of the children are joined with OR on it. That is right only when
   // the children do not share the selected values (the code says so; the property is off by
   // default, #6808) and, as found here, every child has a selected value (#82210). In 12.2 the
   // move up of a union of tables with selections was refused whatever the property, and that
   // change was reverted (#4906, #6585), so the wrong result on the ids is the documented
   // behavior. The asset itself has its selections on different positions (CATEGORY_NAME,
   // REGION_ID) and never moves up. The selection lists also hit TopMVs of CATEGORIES1 and
   // REGIONS1, so the check is on the MV of the union or the SubMVs.
   @Issue(['https://issues.inetsoft.com/issues/4906', 'https://issues.inetsoft.com/issues/6585',
           'https://issues.inetsoft.com/issues/6808', 'https://issues.inetsoft.com/issues/82210'])
   def 'union, mv.union.moveUp=#moveUp, #name: #hits'() {
      given:
      previous = MVTest.setProperties(['mv.union.moveUp': moveUp as String])
      copyUnionAssets(columns, selected, distinct)
      mvtest = new MVTest(VS_ID)
      // moved up: no SubMV of a child; not moved up: no MV of the union
      def absent = hits.contains('Query1|TopMV') ? ['CATEGORIES1|SubMV', 'REGIONS1|SubMV'] : ['Query1|TopMV']

      when:
      if(same) {
         mvtest.executeVSWithoutMV(null, null, false, false)
      }

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(hits)
         !mvtest.getMVHitInfo().any { it in absent }
         unionRows() == rows
      }

      and: 'where the MV result is right, the same as without MV'
      if(same) {
         mvtest.compareWithoutMV()
      }

      where:
      name                                         | moveUp | distinct | columns | selected                 | same  | hits | rows
      // the names of the children differ: right
      'names, both selected'                       | true   | true     | NAMES   | ['Graphics', 'USA West'] | true  | TOP  | ['2|USA West', '5|Graphics']
      'names, both selected'                       | false  | true     | NAMES   | ['Graphics', 'USA West'] | true  | SUB  | ['2|USA West', '5|Graphics']
      // a union all never moves up
      'union all, names'                           | true   | false    | NAMES   | ['Graphics', 'USA West'] | true  | SUB  | ['2|USA West', '5|Graphics']
      // ids 1 and 2 are in both children: "id in (1) or id in (2)" on the whole union also keeps
      // (1, USA East) and (2, Educational); without MV (and with false) 2 rows
      'ids, both selected'                         | true   | true     | IDS     | ['1', '2']               | false | TOP  | ['1|Games', '1|USA East', '2|Educational', '2|USA West']
      'ids, both selected'                         | false  | true     | IDS     | ['1', '2']               | true  | SUB  | ['1|Games', '2|USA West']
      // only CATEGORIES1 has a selected value: the merged condition leaves out a child without a
      // condition (getRuntimeConditions skips an empty filter), so "name in (Graphics)" on the
      // whole union also drops the rows of REGIONS1, although the names do not overlap; without
      // MV 3 rows: 1|USA East, 2|USA West, 5|Graphics. Pinned: change the expected rows when
      // #82210 is answered or fixed
      'a value selected on the names of one child' | true   | true     | NAMES   | ['Graphics', null]       | false | TOP  | ['5|Graphics']
   }

   /** The names of the real date columns of the MVs of this case on the table, sorted. */
   private List<String> realDateColumns(String table) {
      MVManager manager = MVManager.getManager()
      return mvtest.materializedViewNames().collect { manager.get(it) }
         .findAll { MVDef mv -> mv != null && mv.getMVTable() == table }
         .collectMany { MVDef mv -> mv.getColumns() }
         .findAll { it instanceof DateMVColumn && ((DateMVColumn) it).isReal() }
         .collect { it.getName() }.unique().sort()
   }

   /**
    * Copy submv/union-select in both childs to {@link #PATH}: SelectionList1 bound to the column
    * columns[0] of CATEGORIES1 and SelectionList2 to columns[1] of REGIONS1, each with the value
    * selected[i] (if not null) selected in the saved state (so (Home) has it), and the union distinct or not.
    */
   private void copyUnionAssets(List<String> columns, List<String> selected, boolean distinct) {
      def repository = AssetUtil.getAssetRepository(false)
      Viewsheet vs = (Viewsheet) repository.getSheet(
         AssetEntry.createAssetEntry('1^128^__NULL__^submv/union-select in both childs^host-org'),
         admin, false, AssetContent.ALL).clone()
      Worksheet ws = (Worksheet) repository.getSheet(vs.getBaseEntry(), admin, false, AssetContent.ALL).clone()
      ConcatenatedTableAssembly union = (ConcatenatedTableAssembly) ws.getAssembly('Query1')
      union.getOperator(0).getOperator(0).setDistinct(distinct)

      copied = true
      repository.setSheet(AssetEntry.createAssetEntry(WS_ENTRY), ws, admin, true)
      vs.setBaseEntry(AssetEntry.createAssetEntry(WS_ENTRY))

      [['SelectionList1', 'CATEGORIES1'], ['SelectionList2', 'REGIONS1']].eachWithIndex { pair, i ->
         SelectionListVSAssembly list = (SelectionListVSAssembly) vs.getAssembly(pair[0])
         assert list.getTableName() == pair[1]: pair[0] + ' is bound to ' + list.getTableName()
         ColumnRef wscol = (ColumnRef) ((TableAssembly) ws.getAssembly(pair[1]))
            .getColumnSelection(true).getAttribute(columns[i])
         assert wscol != null: pair[1] + ' has no column ' + columns[i]
         ColumnRef col = new ColumnRef(new AttributeRef(null, columns[i]))
         col.setDataType(wscol.getDataType())
         list.setDataRef(col)

         if(selected[i] != null) {
            SelectionValue value = new SelectionValue(selected[i], selected[i])
            value.setState(SelectionValue.STATE_SELECTED)
            SelectionList state = new SelectionList()
            state.setSelectionValues([value] as SelectionValue[])
            list.setStateSelectionList(state)
         }
      }

      repository.setSheet(AssetEntry.createAssetEntry(VS_ENTRY), vs, admin, true)
   }

   /** The rows "id|name" of the union table of the MV run, sorted. */
   private List<String> unionRows() {
      return exported(PATH, 'Query1', '(Home)').drop(2).findAll { it }
         .collect { it.substring(1, it.length() - 1).split(/\]\[/, -1).join('|') }.sort()
   }

   /** The number of rows of an exported table of the MV run, from its size line. */
   private int rowCount(String path, String name, String bk) {
      def size = (exported(path, name, bk)[0] =~ /\((\d+) x \d+\)/)
      assert size.find(): 'no size line in the export of ' + name + ' ' + bk
      return (size.group(1) as int) - 1
   }

   /** The lines of an exported file of the MV run. */
   private List<String> exported(String path, String name, String bk) {
      File folder = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                             'exportedData/' + path)
      File file = new File(folder, 'admin_' + name + (bk == '(Home)' ? '' : '_' + bk) + '__MV.txt')
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }
}

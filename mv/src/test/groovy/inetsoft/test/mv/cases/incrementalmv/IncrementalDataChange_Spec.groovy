package inetsoft.test.mv.cases.incrementalmv

import inetsoft.report.XSessionManager
import inetsoft.report.composition.execution.AssetDataCache
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVSourceTable
import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import inetsoft.uql.ColumnSelection
import inetsoft.uql.ConditionItem
import inetsoft.uql.ConditionList
import inetsoft.uql.XCondition
import inetsoft.uql.asset.AggregateFormula
import inetsoft.uql.asset.AggregateInfo
import inetsoft.uql.asset.AggregateRef
import inetsoft.uql.asset.AssetCondition
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.BoundTableAssembly
import inetsoft.uql.asset.ColumnRef
import inetsoft.uql.asset.DateRangeRef
import inetsoft.uql.asset.ExpressionValue
import inetsoft.uql.asset.GroupRef
import inetsoft.uql.asset.MirrorTableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.erm.AttributeRef
import inetsoft.uql.jdbc.JDBCDataSource
import inetsoft.uql.jdbc.JDBCHandler
import inetsoft.uql.schema.XSchema
import inetsoft.uql.service.DataSourceRegistry
import inetsoft.uql.viewsheet.SelectionListVSAssembly
import inetsoft.uql.viewsheet.SingleTimeInfo
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.uql.viewsheet.TimeInfo
import inetsoft.uql.viewsheet.TimeSliderVSAssembly
import inetsoft.uql.viewsheet.Viewsheet
import inetsoft.uql.viewsheet.internal.SelectionListVSAssemblyInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp

/**
 * Incremental MV updates on data that changes between the MV creation and the updates: the
 * cases create a table in the local H2 database {@code mvorder1} (data source {@code mvtest}),
 * insert and delete rows, and check the MV after each update. The append conditions take the
 * new rows ({@code > MV.MaxValue}, {@code > MV.LastUpdateTime}), as users write them, unlike
 * the other incremental cases, whose data never changes.
 * <p>
 * The worksheet and the viewsheet are copies of {@code MV_Creator/Append} assets made by the
 * case, bound to its table and with its MV conditions, and removed when it ends;
 * {@code mvStorage.zip} is not changed. The expected result is a full rebuild of the MV on the
 * final data ({@link MVTest#compareWithRebuild()}), or the expected rows when the delete
 * condition uses {@code MV.} variables, which the MV creation ignores. The last update time is
 * set by the case ({@link MVTest#setLastUpdateTime(long)}), so the results do not depend on
 * the time the case runs.
 * <p>
 * Running the cases changes {@code docker/src/test/resources/H2Data/mvorder1.mv.db}, even
 * though the table is dropped; restore it with git before committing.
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
class IncrementalDataChange_Spec extends Specification {
   static final String PATH = 'MV_Creator/Append/data_change'
   // MVTest names the export folder after the part of the id after the last ^, so no org
   static final String VS_ID = '1^128^__NULL__^' + PATH
   static final String WS_ENTRY = '1^2^__NULL__^' + PATH + '^host-org'
   static final String VS_ENTRY = VS_ID + '^host-org'
   static final String TABLE = 'MVINC_DATA_CHANGE'
   static final String VIEW = 'MVINC_DATA_CHANGE_V'
   // a copy of mvtest whose queries are run while the rows are read (H2 LAZY_QUERY_EXECUTION), so
   // a query can fail after it returned some rows
   static final String LAZY_DS = 'mvtest_lazy'
   static final String DAYS_BEFORE_UPDATE = 'new Date(MV.LastUpdateTime.getTime() - 5 * 86400000)'

   @Shared def admin
   def mvtest
   MVSourceTable source

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }

      source = new MVSourceTable('mvtest', TABLE, admin)
   }

   def cleanup() {
      def repository = AssetUtil.getAssetRepository(false)

      // every step runs even when an earlier one fails, so nothing is left for the next case
      runAll(
         { mvtest?.removeMV() },
         { source.execute("DROP VIEW IF EXISTS ${VIEW}") },
         { source.drop() },
         {
            DataSourceRegistry registry = DataSourceRegistry.getRegistry()

            if(registry.getDataSource(LAZY_DS) != null) {
               registry.removeDataSource(LAZY_DS)
            }
         },
         { repository.removeSheet(AssetEntry.createAssetEntry(VS_ENTRY), admin, true) },
         { repository.removeSheet(AssetEntry.createAssetEntry(WS_ENTRY), admin, true) })
   }

   /** Run all the steps, then throw the first failure with the later ones suppressed. */
   private static void runAll(Closure... steps) {
      Throwable failure = null

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

   def 'append condition > MV.MaxValue: the new rows are appended once'() {
      given:
      source.createLike('MVSCRIPT')
      insertIntegers(1..5)
      copyIntegerAssets()
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = integers(false)

      and: '3 new rows'
      insertIntegers(6..8)
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = integers(true)

      and: 'no new rows: nothing is appended again'
      update()
      def updated2 = mvtest.takeMVHits()

      and:
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['MVSCRIPT1|TopMV']
         updated == ['MVSCRIPT1|TopMV']
         updated2 == ['MVSCRIPT1|TopMV']
         mvtest.takeMVHits() == ['MVSCRIPT1|TopMV']
         createdRows == (1..5).toList()
         updatedRows == (1..8).toList()
         integers(true) == (1..8).toList()
         mvtest.compareWithRebuild()
      }
   }

   def 'append condition > MV.LastUpdateTime: only the rows after the last update time are appended'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03')
      copyOrdersAssets('MV.LastUpdateTime', null)
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: 'last update time 2020-01-10: 4 is older, 5 and 6 are newer'
      mvtest.setLastUpdateTime(time('2020-01-10'))
      insertOrders(4: '2020-01-08', 5: '2020-01-12', 6: '2020-01-15')
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'the last update time is now the time of the first update: nothing is appended again'
      update()
      def updated2 = mvtest.takeMVHits()

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         updated2 == ['MVorders1|TopMV']
         createdRows == [1, 2, 3]
         updatedRows == [1, 2, 3, 5, 6]
         orders(true) == [1, 2, 3, 5, 6]
      }
   }

   // the delete condition uses MV.LastUpdateTime, which the MV creation ignores, so a rebuilt MV
   // keeps the deleted rows and the expected values are checked instead
   @Issue('https://issues.inetsoft.com/issues/79747')
   def 'delete condition before a date: the old rows are deleted, and the selections show the rows left'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-04', 3: '2020-01-07', 4: '2020-01-09')
      copyOrdersAssets('MV.LastUpdateTime', DAYS_BEFORE_UPDATE) { Viewsheet vs ->
         addSelectionList(vs)
         addNumberSlider(vs)
         addDateSlider(vs)
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)
      def createdRows = orders(false)
      def createdList = listValues(false)
      def createdNumber = sliderRange('NS', false)
      def createdDate = sliderRange('TS', false)
      mvtest.takeMVHits()

      and: 'last update time 2020-01-10: the rows before 2020-01-05 (1 and 2) are deleted'
      mvtest.setLastUpdateTime(time('2020-01-10'))
      mvtest.createIncrementMV(1)
      clearCaches()
      mvtest.executeVS(null, null, true, true)
      def updated = mvtest.takeMVHits()

      then:
      verifyAll {
         createdRows == [1, 2, 3, 4]
         createdList == ['101', '102', '103', '104']
         createdNumber == ['101', '104']
         createdDate == ['2020-01-01', '2020-01-09']
         !updated.isEmpty()
         orders(true) == [3, 4]
         listValues(true) == ['103', '104']
         sliderRange('NS', true) == ['103', '104']
         // #79747: the time slider kept starting at the deleted 2020-01-01
         sliderRange('TS', true) == ['2020-01-07', '2020-01-09']
      }
   }

   def 'new rows appended and old rows deleted in one update: the same as a rebuilt MV'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-03', 3: '2020-01-07', 4: '2020-01-09')
      copyOrdersAssets('MV.LastUpdateTime', DAYS_BEFORE_UPDATE) { Viewsheet vs ->
         addSelectionList(vs)
         addNumberSlider(vs)
         addDateSlider(vs)
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      and: 'last update time 2020-01-10: 1 and 2 are deleted, 5 and 6 are appended'
      mvtest.setLastUpdateTime(time('2020-01-10'))
      insertOrders(5: '2020-01-12', 6: '2020-01-15')
      mvtest.takeMVHits()
      mvtest.createIncrementMV(1)
      clearCaches()
      mvtest.executeVS(null, null, true, true)
      def updated = mvtest.takeMVHits()

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno IN (1, 2)")
      mvtest.rebuildMV(null, null, true)

      then:
      verifyAll {
         !updated.isEmpty()
         orders(true) == [3, 4, 5, 6]
         listValues(true) == ['103', '104', '105', '106']
         sliderRange('NS', true) == ['103', '106']
         sliderRange('TS', true) == ['2020-01-07', '2020-01-15']
         mvtest.compareWithRebuild()
      }
   }

   def 'monthly summary over the detail table: the new rows are added to their months'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-05', 1, 0.1], 2: ['2020-01-08', 2, 0.2], 3: ['2020-02-03', 1, 0.3])
      copyMonthlyAssets(null)
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = rows(false)

      and: 'last update time 2020-02-10: 4 and 5 are added to February (5 with a new customer), 6 is March'
      mvtest.setLastUpdateTime(time('2020-02-10'))
      insertOrders(4: ['2020-02-15', 1, 0.4], 5: ['2020-02-20', 3, 0.5], 6: ['2020-03-01', 2, 0.6])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = rows(true)

      and:
      mvtest.rebuildMV(null, null, false)

      then: 'month, Sum(orderno), Average(discount), Distinct Count(customer_id)'
      verifyAll {
         created == ['MVorders1|SubMV']
         updated == ['MVorders1|SubMV']
         createdRows == [['2020-01-01 00:00:00', '3', '0.15', '2'],
                         ['2020-02-01 00:00:00', '3', '0.3', '1']]
         updatedRows == [['2020-01-01 00:00:00', '3', '0.15', '2'],
                         ['2020-02-01 00:00:00', '12', '0.4', '2'],
                         ['2020-03-01 00:00:00', '6', '0.6', '1']]
         mvtest.compareWithRebuild()
      }
   }

   // the delete condition uses MV.LastUpdateTime, which the MV creation ignores, so a rebuilt MV
   // keeps the deleted rows and the expected values are checked instead
   def 'monthly summary over the detail table with a rolling delete: the deleted month is gone'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-05', 1, 0.1], 2: ['2020-01-08', 2, 0.2], 3: ['2020-02-03', 1, 0.3])
      copyMonthlyAssets('new Date(MV.LastUpdateTime.getTime() - 30 * 86400000)')
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def createdRows = rows(false)
      mvtest.takeMVHits()

      and: 'last update time 2020-02-10: the January rows (before 2020-01-11) are deleted'
      mvtest.setLastUpdateTime(time('2020-02-10'))
      insertOrders(4: ['2020-02-15', 1, 0.4], 5: ['2020-02-20', 3, 0.5], 6: ['2020-03-01', 2, 0.6])
      update()
      def updated = mvtest.takeMVHits()

      then:
      verifyAll {
         updated == ['MVorders1|SubMV']
         createdRows == [['2020-01-01 00:00:00', '3', '0.15', '2'],
                         ['2020-02-01 00:00:00', '3', '0.3', '1']]
         rows(true) == [['2020-02-01 00:00:00', '12', '0.4', '2'],
                        ['2020-03-01 00:00:00', '6', '0.6', '1']]
      }
   }

   // an MV of a grouped table holds the groups, and an update appends whole groups without
   // merging them into the groups of the MV, so the update deletes the groups it appends again
   def 'table grouped by month: the update deletes the latest month and appends it again'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-05', 1, 0.1], 2: ['2020-01-08', 2, 0.2], 3: ['2020-02-03', 1, 0.3])
      copyMonthGroupedAssets()
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = rows(false)

      and: '4 and 5 are added to February, the latest month in the MV, 6 is March'
      insertOrders(4: ['2020-02-15', 1, 0.4], 5: ['2020-02-20', 3, 0.5], 6: ['2020-03-01', 2, 0.6])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = rows(true)

      and: 'no new rows: March is deleted and appended again, not duplicated'
      update()
      def updated2 = mvtest.takeMVHits()

      and:
      mvtest.rebuildMV(null, null, false)

      then: 'month, Sum(orderno), Sum(discount)'
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         updated2 == ['MVorders1|TopMV']
         createdRows == [['2020-01-01 00:00:00', '3', '0.3'],
                         ['2020-02-01 00:00:00', '3', '0.3']]
         updatedRows == [['2020-01-01 00:00:00', '3', '0.3'],
                         ['2020-02-01 00:00:00', '12', '1.2'],
                         ['2020-03-01 00:00:00', '6', '0.6']]
         rows(true) == updatedRows
         mvtest.compareWithRebuild()
      }
   }

   @Issue('https://issues.inetsoft.com/issues/77901')
   def 'the incremental query fails while its rows are read: the update fails and appends nothing'() {
      given: 'a view that fails on the row INTEGER = 7, read lazily so that 6 is returned before the failure'
      source.createLike('MVSCRIPT')
      source.execute("CREATE VIEW ${VIEW} AS SELECT \"INTEGER\" + 0 * (1 / (\"INTEGER\" - 7)) AS \"INTEGER\" " +
                     "FROM ${TABLE}")
      insertIntegers(1..5)
      addLazyDataSource()
      copyIntegerAssets { BoundTableAssembly table ->
         table.getSourceInfo().setPrefix(LAZY_DS)
         table.getSourceInfo().setSource(VIEW)
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()

      and: '6, 7 and 8 are added: reading the new rows fails at 7, after 6 was read'
      insertIntegers(6..8)
      def readBeforeFailure = readUntilFailure("SELECT \"INTEGER\" FROM ${VIEW} WHERE \"INTEGER\" > '5'")
      mvtest.createIncrementMV(1)

      then:
      thrown(MaterializedViewResource.MVCreateFailedException)
      created == ['MVSCRIPT1|TopMV']
      // otherwise the query fails before any row and nothing could be appended anyway
      readBeforeFailure == ['6']

      when: 'the new rows are removed and the MV is updated again'
      source.execute("DELETE FROM ${TABLE} WHERE \"INTEGER\" >= '6'")
      update()

      then: 'the MV is used again and has no row of the failed update'
      verifyAll {
         mvtest.takeMVHits() == ['MVSCRIPT1|TopMV']
         integers(true) == (1..5).toList()
      }
   }

   /** A copy of the mvtest data source that runs its queries lazily, see {@link #LAZY_DS}. */
   private static void addLazyDataSource() {
      DataSourceRegistry registry = DataSourceRegistry.getRegistry()
      JDBCDataSource lazy = (JDBCDataSource) registry.getDataSource('mvtest').clone()
      lazy.setName(LAZY_DS)
      lazy.setURL(lazy.getURL() + ';LAZY_QUERY_EXECUTION=1')
      registry.setDataSource(lazy, false)
   }

   /** The values of the first column that the query returns before it fails. */
   private List<String> readUntilFailure(String query) {
      JDBCDataSource ds = (JDBCDataSource) DataSourceRegistry.getRegistry().getDataSource(LAZY_DS)
      Connection conn = new JDBCHandler().getConnection(ds, admin)
      List<String> values = []

      try {
         conn.createStatement().withCloseable { Statement stmt ->
            stmt.executeQuery(query).withCloseable { ResultSet rs ->
               while(rs.next()) {
                  values << rs.getString(1)
               }
            }
         }

         throw new AssertionError("the query did not fail, it returned " + values)
      }
      catch(SQLException ignore) {
         return values
      }
      finally {
         conn.close()
      }
   }

   private void update() {
      mvtest.createIncrementMV(1)
      clearCaches()
      mvtest.executeVS(null, null, true, false)
   }

   private static void clearCaches() {
      AssetDataCache.getCache().clearCache()
      XSessionManager.getSessionManager().clearCache()
   }

   private void insertIntegers(IntRange values) {
      values.each { source.execute("INSERT INTO ${TABLE} (\"INTEGER\") VALUES ('${it}')") }
   }

   /**
    * orderno -> orderdate, with customer_id 100 + orderno and discount 0, or orderno ->
    * [orderdate, customer_id, discount].
    */
   private void insertOrders(Map<Integer, Object> rows) {
      rows.each { no, row ->
         List values = row instanceof List ? (List) row : [row, 100 + no, 0]
         source.execute("INSERT INTO ${TABLE} (orderno, customer_id, discount, orderdate) " +
                        "VALUES (${no}, ${values[1]}, ${values[2]}, TIMESTAMP '${values[0]} 00:00:00')")
      }
   }

   private static long time(String date) {
      return Timestamp.valueOf(date + ' 00:00:00').getTime()
   }

   /**
    * MV_Creator/Append/maxValue_Integer with the append condition {@code INTEGER > MV.MaxValue}
    * and no delete condition. changeTable, if not null, is called with the table afterwards.
    */
   private void copyIntegerAssets(Closure changeTable = null) {
      copyAssets('MV_Creator/Append/maxValue_Integer', 'MVSCRIPT1') { BoundTableAssembly table ->
         ConditionList append = (ConditionList) ((ConditionList) table.getMVUpdatePreConditionList()).clone()
         append.getConditionItem(0).getXCondition().setOperation(XCondition.GREATER_THAN)
         table.setMVUpdatePreConditionList(append)
         table.setMVDeletePreConditionList(new ConditionList())
         changeTable?.call(table)
      }
   }

   /**
    * MV_Creator/Append/mvLastUpdatetime with the append condition {@code orderdate > append}
    * and, when delete is not null, the delete condition {@code orderdate < delete}, both
    * JavaScript expressions.
    */
   private void copyOrdersAssets(String append, String delete, Closure changeVS = null,
                                 Closure changeWS = null)
   {
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1', { BoundTableAssembly table ->
         ConditionList appends = (ConditionList) ((ConditionList) table.getMVUpdatePreConditionList()).clone()
         setExpression((AssetCondition) appends.getConditionItem(0).getXCondition(),
                       XCondition.GREATER_THAN, append)
         table.setMVUpdatePreConditionList(appends)

         ConditionList deletes = new ConditionList()

         if(delete != null) {
            deletes = (ConditionList) ((ConditionList) table.getMVDeletePreConditionList()).clone()
            setExpression((AssetCondition) deletes.getConditionItem(0).getXCondition(),
                          XCondition.LESS_THAN, delete)
         }

         table.setMVDeletePreConditionList(deletes)
      }, changeVS, changeWS)
   }

   /**
    * The orders assets with a mirror {@code Monthly} of the detail table, grouped by
    * Month(orderdate) with Sum(orderno), Average(discount) and Distinct Count(customer_id), and
    * the viewsheet table bound to it. The MV conditions stay on the detail table: the append
    * condition {@code orderdate > MV.LastUpdateTime} and the delete condition, if not null.
    * The columns of MV conditions must be visible in their table, so a table grouped by month
    * can not have them on orderdate.
    */
   private void copyMonthlyAssets(String delete) {
      copyOrdersAssets('MV.LastUpdateTime', delete, { Viewsheet vs ->
         TableVSAssembly table = (TableVSAssembly) vs.getAssembly('Query1')
         table.setTableName('Monthly')
         ColumnSelection cols = new ColumnSelection()

         ['Month(orderdate)', 'orderno', 'discount', 'customer_id'].each {
            cols.addAttribute(new ColumnRef(new AttributeRef(null, it)))
         }

         table.setColumnSelection(cols)
      }, { Worksheet ws, BoundTableAssembly detail ->
         MirrorTableAssembly mirror = new MirrorTableAssembly(ws, 'Monthly', detail)
         ColumnSelection cols = mirror.getColumnSelection(false)
         ColumnRef date = (ColumnRef) cols.getAttribute('orderdate')
         DateRangeRef range = new DateRangeRef(
            DateRangeRef.getName('orderdate', DateRangeRef.MONTH_INTERVAL), date.getDataRef(),
            DateRangeRef.MONTH_INTERVAL)
         range.setOriginalType(XSchema.TIME_INSTANT)
         range.setAutoCreate(true)
         ColumnRef month = new ColumnRef(range)
         month.setDataType(XSchema.TIME_INSTANT)
         cols.addAttribute(month)
         mirror.setColumnSelection(cols, false)

         GroupRef group = new GroupRef(month)
         group.setDateGroup(DateRangeRef.MONTH_INTERVAL)
         AggregateInfo ainfo = new AggregateInfo()
         ainfo.setGroups([group] as GroupRef[])
         ainfo.setAggregates([
            new AggregateRef(cols.getAttribute('orderno'), AggregateFormula.SUM),
            new AggregateRef(cols.getAttribute('discount'), AggregateFormula.AVG),
            new AggregateRef(cols.getAttribute('customer_id'), AggregateFormula.COUNT_DISTINCT)
         ] as AggregateRef[])
         mirror.setAggregateInfo(ainfo)
         mirror.setAggregate(true)
         ws.addAssembly(mirror)
      })
   }

   /**
    * The orders assets with the table grouped by Month(orderdate), Sum(orderno) and
    * Sum(discount). The MV conditions are pre-aggregate conditions on the group column, the only
    * pre-aggregate columns the condition dialog offers for a grouped table: delete and append
    * {@code Month(orderdate) >= MV.MaxValue}.
    */
   private void copyMonthGroupedAssets() {
      copyOrdersAssets('MV.LastUpdateTime', null, { Viewsheet vs ->
         TableVSAssembly table = (TableVSAssembly) vs.getAssembly('Query1')
         ColumnSelection cols = new ColumnSelection()

         ['Month(orderdate)', 'orderno', 'discount'].each {
            cols.addAttribute(new ColumnRef(new AttributeRef(null, it)))
         }

         table.setColumnSelection(cols)
      }, { Worksheet ws, BoundTableAssembly table ->
         ColumnSelection cols = table.getColumnSelection(false)
         ColumnRef date = (ColumnRef) cols.getAttribute('orderdate')
         DateRangeRef range = new DateRangeRef(
            DateRangeRef.getName('orderdate', DateRangeRef.MONTH_INTERVAL), date.getDataRef(),
            DateRangeRef.MONTH_INTERVAL)
         range.setOriginalType(XSchema.TIME_INSTANT)
         range.setAutoCreate(true)
         ColumnRef month = new ColumnRef(range)
         month.setDataType(XSchema.TIME_INSTANT)
         cols.addAttribute(month)
         table.setColumnSelection(cols, false)

         GroupRef group = new GroupRef(month)
         group.setDateGroup(DateRangeRef.MONTH_INTERVAL)
         AggregateInfo ainfo = new AggregateInfo()
         ainfo.setGroups([group] as GroupRef[])
         ainfo.setAggregates([new AggregateRef(cols.getAttribute('orderno'), AggregateFormula.SUM),
                              new AggregateRef(cols.getAttribute('discount'), AggregateFormula.SUM)] as AggregateRef[])
         table.setAggregateInfo(ainfo)
         table.setAggregate(true)

         table.setMVUpdatePreConditionList(latestMonthOrLater(month))
         table.setMVDeletePreConditionList(latestMonthOrLater(month))
      })
   }

   /** {@code month >= MV.MaxValue}. */
   private static ConditionList latestMonthOrLater(ColumnRef month) {
      AssetCondition cond = new AssetCondition(XSchema.TIME_INSTANT)
      cond.setOperation(XCondition.GREATER_THAN)
      cond.setEqual(true)
      ExpressionValue value = new ExpressionValue()
      value.setType(ExpressionValue.JAVASCRIPT)
      value.setExpression('MV.MaxValue')
      cond.addValue(value)
      ConditionList list = new ConditionList()
      list.append(new ConditionItem(month, cond, 0))
      return list
   }

   private static void setExpression(AssetCondition cond, int op, String expression) {
      cond.setOperation(op)
      cond.setEqual(false)
      ExpressionValue value = (ExpressionValue) cond.getValue(0)
      value.setType(ExpressionValue.JAVASCRIPT)
      value.setExpression(expression)
   }

   /**
    * Copy the worksheet and the viewsheet at path, with the table bound to the case's table.
    * changeWS is called with the worksheet and the table, after changeTable.
    */
   private void copyAssets(String path, String tableName, Closure changeTable, Closure changeVS = null,
                           Closure changeWS = null)
   {
      def repository = AssetUtil.getAssetRepository(false)
      Worksheet ws = (Worksheet) repository.getSheet(
         AssetEntry.createAssetEntry("1^2^__NULL__^${path}^host-org"), admin, false, AssetContent.ALL).clone()
      def table = (BoundTableAssembly) ws.getAssembly(tableName)
      table.getSourceInfo().setSource(TABLE)
      changeTable(table)
      changeWS?.call(ws, table)
      repository.setSheet(AssetEntry.createAssetEntry(WS_ENTRY), ws, admin, true)

      Viewsheet vs = (Viewsheet) repository.getSheet(
         AssetEntry.createAssetEntry("1^128^__NULL__^${path}^host-org"), admin, false, AssetContent.ALL).clone()
      vs.setBaseEntry(AssetEntry.createAssetEntry(WS_ENTRY))
      changeVS?.call(vs)
      repository.setSheet(AssetEntry.createAssetEntry(VS_ENTRY), vs, admin, true)
   }

   private static void addSelectionList(Viewsheet vs) {
      SelectionListVSAssembly list = new SelectionListVSAssembly(vs, 'SL')
      SelectionListVSAssemblyInfo info = (SelectionListVSAssemblyInfo) list.getInfo()
      info.setTableName('MVorders1')
      info.setDataRef(new ColumnRef(new AttributeRef(null, 'customer_id')))
      vs.addAssembly(list)
   }

   private static void addNumberSlider(Viewsheet vs) {
      vs.addAssembly(slider(vs, 'NS', 'customer_id', XSchema.INTEGER, TimeInfo.NUMBER))
   }

   private static void addDateSlider(Viewsheet vs) {
      vs.addAssembly(slider(vs, 'TS', 'orderdate', XSchema.TIME_INSTANT, TimeInfo.DAY))
   }

   private static TimeSliderVSAssembly slider(Viewsheet vs, String name, String column,
                                              String type, int rangeType)
   {
      TimeSliderVSAssembly slider = new TimeSliderVSAssembly(vs, name)
      slider.setTableName('MVorders1')
      SingleTimeInfo info = new SingleTimeInfo()
      ColumnRef ref = new ColumnRef(new AttributeRef(null, column))
      ref.setDataType(type)
      info.setDataRef(ref)
      info.setRangeTypeValue(rangeType)
      slider.setTimeInfo(info)
      return slider
   }

   /** The lines of an exported file of the last run. */
   private List<String> exported(String name, boolean incremental) {
      File folder = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                             'exportedData/' + PATH)
      File file = new File(folder, 'admin_' + name + (incremental ? '__MVIncremental.txt' : '__MV.txt'))
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   /** The rows of the exported table, without the size and the header. */
   private List<List<String>> rows(boolean incremental) {
      return exported('Query1', incremental).drop(2).findAll { it }.collect {
         (it.substring(1, it.length() - 1).split(/\]\[/) as List<String>)
      }
   }

   private List<Integer> integers(boolean incremental) {
      return rows(incremental).collect { it[0] as int }.sort()
   }

   /** The ordernos of the exported table (columns customer_id, discount, orderdate, orderno). */
   private List<Integer> orders(boolean incremental) {
      return rows(incremental).collect { it[3] as int }.sort()
   }

   private List<String> listValues(boolean incremental) {
      return exported('SL', incremental).collect { (it =~ /^\[([^\]]*)\]/)[0][1] as String }
   }

   /** The labels of the min and max values of a slider. */
   private List<String> sliderRange(String name, boolean incremental) {
      return exported(name, incremental).take(2).collect { (it =~ /: \[(.*)\]$/)[0][1] as String }
   }
}

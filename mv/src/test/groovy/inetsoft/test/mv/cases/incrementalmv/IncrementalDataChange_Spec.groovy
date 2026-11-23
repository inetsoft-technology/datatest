package inetsoft.test.mv.cases.incrementalmv

import inetsoft.mv.MVManager
import inetsoft.report.XSessionManager
import inetsoft.sree.security.Organization
import inetsoft.sree.security.Permission
import inetsoft.sree.security.Resource
import inetsoft.sree.security.ResourceAction
import inetsoft.sree.security.SecurityEngine
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
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.erm.AttributeRef
import inetsoft.uql.erm.XDataModel
import inetsoft.uql.erm.vpm.VirtualPrivateModel
import inetsoft.uql.erm.vpm.VpmCondition
import inetsoft.uql.jdbc.JDBCDataSource
import inetsoft.uql.jdbc.JDBCHandler
import inetsoft.uql.schema.StringValue
import inetsoft.uql.schema.UserVariable
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
 * the other incremental cases, whose data never changes. Some cases have the kinds of MV
 * conditions of those cases instead: constant conditions, post-aggregate conditions,
 * conditions on a join, mirror or minus table, a column or a subquery as the value, and
 * {@code MV.MaxValue} and {@code MV.MinValue} in append and delete conditions on columns of
 * other types.
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
   static final String VPM = 'mvinc_data_change_vpm'
   // an H2 function that waits, so that reading a view that calls it takes a while
   static final String SLEEP = 'MVINC_DATA_CHANGE_SLEEP'
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
         { source.execute("DROP ALIAS IF EXISTS ${SLEEP}") },
         { source.drop() },
         {
            DataSourceRegistry registry = DataSourceRegistry.getRegistry()

            if(registry.getDataSource(LAZY_DS) != null) {
               registry.removeDataSource(LAZY_DS)
            }
         },
         { removeVPM() },
         { removeGrant() },
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

   // without MV conditions an update creates the MV again from the source; with "force append
   // updates" it appends every row the source returns, for a source that holds only the new rows
   def 'no MV condition, force append updates #forced: the MV has #expected after the source is replaced'() {
      given:
      source.createLike('MVSCRIPT')
      insertIntegers(1..5)
      copyIntegerAssets { BoundTableAssembly table ->
         table.setMVUpdatePreConditionList(new ConditionList())
         table.setMVForceAppendUpdates(forced)
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = integers(false)

      and: 'the source has only the new rows'
      source.execute("DELETE FROM ${TABLE}")
      insertIntegers(6..8)
      update()

      then:
      verifyAll {
         created == ['MVSCRIPT1|TopMV']
         mvtest.takeMVHits() == ['MVSCRIPT1|TopMV']
         createdRows == (1..5).toList()
         integers(true) == expected
      }

      where:
      forced | expected
      true   | (1..8).toList()
      false  | (6..8).toList()
   }

   // MV.<column>.Min and MV.<column>.Max are variables of the update for every column of the MV,
   // so a condition can use the range of another column, unlike MV.MaxValue
   def 'append condition orderno > $(MV.customer_id.Max): the variable of another column takes the new rows'() {
      given: 'customer_id is the same as orderno'
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-01', 1, 0], 2: ['2020-01-02', 2, 0], 3: ['2020-01-03', 3, 0])
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1') { BoundTableAssembly table ->
         ColumnRef orderno = (ColumnRef) table.getColumnSelection(false).getAttribute('orderno')
         AssetCondition cond = new AssetCondition(XSchema.INTEGER)
         cond.setOperation(XCondition.GREATER_THAN)
         cond.addValue(variable('MV.customer_id.Max'))
         ConditionList append = new ConditionList()
         append.append(new ConditionItem(orderno, cond, 0))
         table.setMVUpdatePreConditionList(append)
         table.setMVDeletePreConditionList(new ConditionList())
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and:
      insertOrders(4: ['2020-01-04', 4, 0], 5: ['2020-01-05', 5, 0])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'no new rows: nothing is appended again'
      update()
      def updated2 = mvtest.takeMVHits()

      and:
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         updated2 == ['MVorders1|TopMV']
         createdRows == [1, 2, 3]
         updatedRows == [1, 2, 3, 4, 5]
         orders(true) == [1, 2, 3, 4, 5]
         mvtest.compareWithRebuild()
      }
   }

   def 'viewsheet open while the MV is updated: a refresh shows the appended rows'() {
      given:
      source.createLike('MVSCRIPT')
      insertIntegers(1..5)
      copyIntegerAssets()
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def createdRows = integers(false)
      mvtest.takeMVHits()

      and: 'the viewsheet stays open, and the caches are not cleared'
      insertIntegers(6..8)
      mvtest.createIncrementMV(1)
      mvtest.refreshVS(true, false)

      then:
      verifyAll {
         createdRows == (1..5).toList()
         mvtest.takeMVHits() == ['MVSCRIPT1|TopMV']
         integers(true) == (1..8).toList()
      }
   }

   // with VPM an MV is created for each identity with READ on the viewsheet, with the rows it
   // may see, and the update appends to each MV the new rows its identity may see. The
   // viewsheet is granted to the users, or to their groups, which the analysis expands to the
   // member users. Without a grant of its own the viewsheet inherits Everyone from the root
   // folder, the analysis creates one MV for that role, and user0, whose VPM differs, does not
   // use it.
   def 'VPM, viewsheet granted to #grantee: after the update each user sees the old and the new rows of their own'() {
      given: 'user0 sees the customers up to 102, user1 the others'
      def user0 = MVTest.createPrincipal('user0', ['Everyone', 'role1'] as String[], ['group0'] as String[])
      def user1 = MVTest.createPrincipal('user1', ['Everyone'] as String[], ['group0_1'] as String[])
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-01', 101, 0], 2: ['2020-01-02', 102, 0],
                   3: ['2020-01-03', 103, 0], 4: ['2020-01-04', 104, 0])
      addVPM("vpm.user == 'user0' ? 'customer_id <= 102' : 'customer_id > 102'")
      copyOrdersAssets('MV.MaxValue', null)
      grantRead(users, groups)
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(true, expand)
      def mvUsers = mvUsers()
      mvtest.executeVS(null, null, false, false, user0)
      def created0 = mvtest.takeMVHits()
      mvtest.executeVS(null, null, false, false, user1)
      def created1 = mvtest.takeMVHits()

      and: '5 is a customer of user0, 6 of user1'
      insertOrders(5: ['2020-01-05', 101, 0], 6: ['2020-01-06', 106, 0])
      mvtest.createIncrementMV(1)
      clearCaches()
      mvtest.executeVS(null, null, true, false, user0)
      def updated0 = mvtest.takeMVHits()
      mvtest.executeVS(null, null, true, false, user1)
      def updated1 = mvtest.takeMVHits()

      then:
      verifyAll {
         // users with the same VPM conditions share an MV, e.g. user1 with the other members
         // of its group that are not user0
         mvUsers.any { it.contains('user0') && !it.contains('user1') }
         mvUsers.any { it.contains('user1') && !it.contains('user0') }
         [created0, created1, updated0, updated1].every { it == ['MVorders1|TopMV'] }
         orders(false, 'user0') == [1, 2]
         orders(false, 'user1') == [3, 4]
         orders(true, 'user0') == [1, 2, 5]
         orders(true, 'user1') == [3, 4, 6]
      }

      where:
      grantee                    | users              | groups                  | expand
      'user0 and user1'          | ['user0', 'user1'] | []                      | false
      'their groups, expanded'   | []                 | ['group0', 'group0_1']  | true
   }

   /** Grant READ on the viewsheet of the case, which is removed with it. */
   private static void grantRead(List<String> users, List<String> groups) {
      Resource resource = AssetUtil.getSecurityResource(AssetEntry.createAssetEntry(VS_ENTRY))
      Permission permission = new Permission()
      String org = Organization.getDefaultOrganizationID()
      permission.setUserGrantsForOrg(ResourceAction.READ, users as Set, org)
      permission.setGroupGrantsForOrg(ResourceAction.READ, groups as Set, org)
      SecurityEngine.getSecurity().setPermission(resource.getType(), resource.getPath(), permission)
   }

   private static void removeGrant() {
      Resource resource = AssetUtil.getSecurityResource(AssetEntry.createAssetEntry(VS_ENTRY))
      SecurityEngine engine = SecurityEngine.getSecurity()

      if(engine.getPermission(resource.getType(), resource.getPath()) != null) {
         engine.removePermission(resource.getType(), resource.getPath())
      }
   }

   /** The users of each MV created for the viewsheet, the MV names sorted. */
   private List<List<String>> mvUsers() {
      List<String> views = ((MaterializedViewResource) mvtest.materializedViewResource).getViews()
      return views.toSorted().collect { String name ->
         (MVManager.getManager().get(name).getUsers() ?: []).collect { it.getName() }.sort()
      }
   }

   /** Add a VPM to the data source mvtest with a trigger script on the table of the case. */
   private static void addVPM(String script) {
      DataSourceRegistry registry = DataSourceRegistry.getRegistry()
      XDataModel model = registry.getDataModel('mvtest')
      VpmCondition cond = new VpmCondition('by user')
      cond.setType(VpmCondition.TABLE)
      cond.setTable(TABLE)
      cond.setScript(script)
      VirtualPrivateModel vpm = new VirtualPrivateModel(VPM)
      vpm.addCondition(cond)
      model.addVirtualPrivateModel(vpm, true)
      registry.setDataModel(model)
   }

   private static void removeVPM() {
      DataSourceRegistry registry = DataSourceRegistry.getRegistry()
      XDataModel model = registry.getDataModel('mvtest')

      if(model?.getVirtualPrivateModel(VPM) != null) {
         model.removeVirtualPrivateModel(VPM)
         registry.setDataModel(model)
      }
   }

   /** A variable as the condition dialog creates it. */
   private static UserVariable variable(String name) {
      UserVariable var = new UserVariable()
      var.setName(name)
      var.setAlias(name)
      var.setValueNode(new StringValue(name))
      return var
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

   // Constant MV conditions, as most cases of Append_Spec and Delete_Spec have, but on data that
   // changes. The MV creation applies a constant delete condition (the new MV leaves its rows
   // out) and does not apply the append condition, so the rows the append condition skips are
   // deleted from the database before the rebuild. No new row is taken by both conditions: an
   // update keeps such a row until the next update (see the case after the join case).
   def 'constant append and delete conditions: the new rows of the append condition are appended, the same as a rebuilt MV'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03', 4: '2020-01-04')
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1') { BoundTableAssembly table ->
         ColumnRef customer = column(table, 'customer_id')
         table.setMVUpdatePreConditionList(atLeast(customer, 105))
         table.setMVDeletePreConditionList(oneOf(customer, [102, 106]))
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: '5 (customer 105) and 6 (107) are new, 7 (customer 100) is below the append condition'
      insertOrders(5: '2020-01-05', 6: ['2020-01-06', 107, 0], 7: ['2020-01-07', 100, 0])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 7")
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [1, 3, 4]
         updatedRows == [1, 3, 4, 5, 6]
         mvtest.compareWithRebuild()
      }
   }

   // Post-aggregate MV conditions (mvUpdatePostConds, mvDeletePostConds), which the update merges
   // into the post conditions of the table, on a table grouped by customer. The new rows are of
   // new customers only, so the update appends whole new groups (an update does not merge rows
   // into the groups of the MV, see the case of the table grouped by month). The delete condition
   // takes no new group, as in the constant case.
   def 'post-aggregate append and delete conditions on a grouped table: the new groups are appended, the same as a rebuilt MV'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: ['2020-01-01', 101, 0.25], 2: ['2020-01-02', 102, 0.5], 3: ['2020-01-03', 101, 0.5],
                   4: ['2020-01-04', 103, 0.25])
      copyCustomerGroupedAssets()
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = rows(false)

      and: 'customers 105 (two rows) and 107 are new, 100 is below the append condition'
      insertOrders(5: ['2020-01-05', 105, 0.25], 6: ['2020-01-06', 105, 0.5], 7: ['2020-01-07', 107, 0.25],
                   8: ['2020-01-08', 100, 0.25])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = rows(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 8")
      mvtest.rebuildMV(null, null, false)

      then: 'customer_id, Sum(discount)'
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [['101', '0.75'], ['103', '0.25']]
         updatedRows == [['101', '0.75'], ['103', '0.25'], ['105', '0.75'], ['107', '0.25']]
         mvtest.compareWithRebuild()
      }
   }

   // MV conditions on a join table (a composed table), which the update also applies to the
   // tables of the join (MVIncremental.mergeAppendCondition), as in Append_Spec M_Testcase2: the
   // join of MVorders1 and MVorders2 on customer_id, here both bound to the case's table. The
   // delete condition takes no new row, as in the constant case.
   def 'append and delete conditions on a join table: the new joined rows are appended, the same as a rebuilt MV'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03', 4: '2020-01-04')
      copyJoinAssets()
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = values('orderno', false)

      and: '5 (customer 105) and 6 (107) are new, 7 (customer 100) is below the append condition'
      insertOrders(5: '2020-01-05', 6: ['2020-01-06', 107, 0], 7: ['2020-01-07', 100, 0])
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = values('orderno', true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 7")
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['Query1|TopMV']
         updated == ['Query1|TopMV']
         createdRows == ['1', '3', '4']
         updatedRows == ['1', '3', '4', '5', '6']
         mvtest.compareWithRebuild()
      }
   }

   // An update deletes first and appends afterwards (LocalMVIncremental.update0), and the query
   // of the append does not exclude the rows of the delete condition, while the MV creation does
   // (MVCreatorUtil.mergeConditionList). So a new row that both conditions take is appended and
   // kept, and only the next update deletes it. This is how the update works, not a bug: the
   // case keeps the behavior (decided 2026-11-17, see T9 in docs/test-gap-assessment/mv.md §8).
   // If an update ever leaves such a row out, this case fails on updatedRows: then check whether
   // the product changed on purpose and update the case. The append condition
   // orderno > MV.MaxValue takes only new rows, so the second update appends nothing again.
   def 'a new row of both the append and the delete condition: the update keeps it, the next update deletes it'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03', 4: '2020-01-04')
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1') { BoundTableAssembly table ->
         table.setMVUpdatePreConditionList(afterMaxValue(column(table, 'orderno')))
         table.setMVDeletePreConditionList(oneOf(column(table, 'customer_id'), [102, 106]))
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def createdRows = orders(false)
      mvtest.takeMVHits()

      and: '5 (customer 105) and 6 (106, of the delete condition) are new'
      insertOrders(5: '2020-01-05', 6: '2020-01-06')
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'no new rows: the delete condition takes 6'
      update()
      def updated2 = mvtest.takeMVHits()
      def updatedRows2 = orders(true)

      and:
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         updated == ['MVorders1|TopMV']
         updated2 == ['MVorders1|TopMV']
         createdRows == [1, 3, 4]
         updatedRows == [1, 3, 4, 5, 6]
         updatedRows2 == [1, 3, 4, 5]
         mvtest.compareWithRebuild()
      }
   }

   // MV.MaxValue and MV.MinValue on the column types of the MV_Creator/Append maxValue_* and
   // minValue_* cases, which use = and so append the same rows again on data that does not
   // change. Here the new values beyond the values of the MV are appended, and the others not;
   // these are deleted from the database before the rebuild.
   def 'append condition #column #op MV.#bound: the new values beyond the MV are appended'() {
      given:
      source.createLike('MVSCRIPT')
      insertValues(column, initial)
      copyAssets("MV_Creator/Append/${asset}", tableName) { BoundTableAssembly table ->
         ConditionList append = (ConditionList) ((ConditionList) table.getMVUpdatePreConditionList()).clone()
         setExpression((AssetCondition) append.getConditionItem(0).getXCondition(),
                       op == '>' ? XCondition.GREATER_THAN : XCondition.LESS_THAN, 'MV.' + bound)
         table.setMVUpdatePreConditionList(append)
         table.setMVDeletePreConditionList(new ConditionList())
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdValues = firstColumn(false)

      and:
      insertValues(column, taken + skipped)
      update()
      def updated = mvtest.takeMVHits()
      def updatedValues = firstColumn(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      skipped.each { source.execute("DELETE FROM ${TABLE} WHERE \"${column.toUpperCase()}\" = ${literal(column, it)}") }
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == [tableName + '|TopMV']
         updated == [tableName + '|TopMV']
         createdValues == initial.collect { shown(column, it) }.sort()
         updatedValues == (initial + taken).collect { shown(column, it) }.sort()
         mvtest.compareWithRebuild()
      }

      where:
      asset               | tableName   | column        | op  | bound      | initial                                    | taken                      | skipped
      'minValue_Integer'  | 'MVSCRIPT1' | 'INTEGER'     | '<' | 'MinValue' | [10, 20, 30]                               | [1, 5]                     | [25]
      'maxValue_Double'   | 'MVSCRIPT1' | 'DOUBLE'      | '>' | 'MaxValue' | [1.5, 2.5, 3.5]                            | [4.25, 5.75]               | [2.75]
      'minValue_Double'   | 'MVSCRIPT1' | 'DOUBLE'      | '<' | 'MinValue' | [1.5, 2.5, 3.5]                            | [0.25, -1.5]               | [2.75]
      'maxValue_String'   | 'MVScript1' | 'string'      | '>' | 'MaxValue' | ['b', 'd', 'f']                            | ['g', 'h']                 | ['c']
      'minValue_String'   | 'MVScript1' | 'string'      | '<' | 'MinValue' | ['b', 'd', 'f']                            | ['a', 'ab']                | ['e']
      'maxValue_Datetime' | 'MVScript1' | 'timeinstant' | '>' | 'MaxValue' | ['2020-01-02', '2020-01-04', '2020-01-06'] | ['2020-01-07', '2020-01-09'] | ['2020-01-03']
      'minValue_Datetime' | 'MVScript1' | 'timeinstant' | '<' | 'MinValue' | ['2020-01-02', '2020-01-04', '2020-01-06'] | ['2019-12-30', '2020-01-01'] | ['2020-01-05']
   }

   // The value of the append condition is another column, as in Append_Spec S_TestCase28. The
   // old rows have customer_id != orderno, so the condition takes only new rows.
   def 'append condition customer_id = orderno (a column): the new rows with equal columns are appended'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03')
      copyAssets('MV_Creator/Append/S_TestCase28', 'MVorders1') { BoundTableAssembly table ->
         table.setMVDeletePreConditionList(new ConditionList())
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: '4 and 5 have customer_id = orderno, 6 has customer 106'
      insertOrders(4: ['2020-01-04', 4, 0], 5: ['2020-01-05', 5, 0], 6: '2020-01-06')
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 6")
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [1, 2, 3]
         updatedRows == [1, 2, 3, 4, 5]
         mvtest.compareWithRebuild()
      }
   }

   // The value of the append condition is a subquery on another table, as in Append_Spec
   // S_TestCase29: orderno in the customer_id of MVCustomers1 (the database table MVCustomers,
   // customer_id 1 to 20), correlated on orderno = customer_id. The old rows have ordernos above
   // 20, so the condition takes only new rows.
   def 'append condition orderno in a subquery on another table: the new rows found there are appended'() {
      given:
      source.createLike('MVorders')
      insertOrders(101: '2020-01-01', 102: '2020-01-02', 103: '2020-01-03')
      copyAssets('MV_Creator/Append/S_TestCase29', 'MVorders1') { BoundTableAssembly table ->
         table.setMVDeletePreConditionList(new ConditionList())
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: 'customers 5 and 6 exist, 1000 does not'
      insertOrders(5: '2020-01-05', 6: '2020-01-06', 1000: '2020-01-07')
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = orders(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 1000")
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [101, 102, 103]
         updatedRows == [5, 6, 101, 102, 103]
         mvtest.compareWithRebuild()
      }
   }

   // The delete conditions col = MV.MaxValue and col = MV.MinValue of the MV_Creator/Delete
   // maxValue_* and minValue_* cases on data that changes, with the append condition
   // col > MV.MaxValue. The update computes the MV values before it deletes, so it deletes the
   // largest or smallest value of the MV and appends the new value. The MV creation ignores both
   // conditions, so the deleted row is deleted from the database before the rebuild.
   def 'delete condition #column = MV.#bound: the update deletes that value of the MV and appends the new one'() {
      given:
      source.createLike('MVSCRIPT')
      insertValues(column, initial)
      copyAssets("MV_Creator/Delete/${asset}", tableName) { BoundTableAssembly table ->
         // qualified: the data variable column hides the method
         ColumnRef col = IncrementalDataChange_Spec.column(table, column)
         table.setMVUpdatePreConditionList(expression(col, XCondition.GREATER_THAN, 'MV.MaxValue'))
      }
      mvtest = new MVTest(VS_ID)
      def removed = bound == 'MaxValue' ? initial.last() : initial.first()

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdValues = firstColumn(false)

      and:
      insertValues(column, [added])
      update()
      def updated = mvtest.takeMVHits()
      def updatedValues = firstColumn(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE \"${column.toUpperCase()}\" = ${literal(column, removed)}")
      mvtest.rebuildMV(null, null, false)

      then:
      verifyAll {
         created == [tableName + '|TopMV']
         updated == [tableName + '|TopMV']
         createdValues == initial.collect { shown(column, it) }.sort()
         updatedValues == (initial - [removed] + [added]).collect { shown(column, it) }.sort()
         mvtest.compareWithRebuild()
      }

      where: 'initial is sorted'
      asset               | tableName   | column        | bound      | initial                                    | added
      'maxValue_Integer'  | 'MVSCRIPT1' | 'INTEGER'     | 'MaxValue' | [10, 20, 30]                               | 40
      'minValue_Integer'  | 'MVSCRIPT1' | 'INTEGER'     | 'MinValue' | [10, 20, 30]                               | 40
      'maxValue_Double'   | 'MVSCRIPT1' | 'DOUBLE'      | 'MaxValue' | [1.5, 2.5, 3.5]                            | 4.25
      'minValue_Double'   | 'MVSCRIPT1' | 'DOUBLE'      | 'MinValue' | [1.5, 2.5, 3.5]                            | 4.25
      'maxValue_String'   | 'MVScript1' | 'string'      | 'MaxValue' | ['b', 'd', 'f']                            | 'g'
      'minValue_String'   | 'MVScript1' | 'string'      | 'MinValue' | ['b', 'd', 'f']                            | 'g'
      'maxValue_Datetime' | 'MVScript1' | 'timeinstant' | 'MaxValue' | ['2020-01-02', '2020-01-04', '2020-01-06'] | '2020-01-07'
      'minValue_Datetime' | 'MVScript1' | 'timeinstant' | 'MinValue' | ['2020-01-02', '2020-01-04', '2020-01-06'] | '2020-01-07'
   }

   // An MV condition on a mirror table, as in Append_Spec M_Testcase1: Query1 mirrors MVorders1
   // grouped by Year(orderdate) with Sum(orderno), and the MV is on Query1. The new rows are of
   // a new year only, as an update appends whole groups (see the case of the table grouped by
   // month). No delete condition: one on orderno, which is not a group column, is neither
   // applied when the MV is created nor by an update, as the MV has no orderno to delete.
   def 'append condition on a grouped mirror table: the new year is appended, the same as a rebuilt MV'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-02-01', 3: '2021-01-01')
      copyAssets('MV_Creator/Append/M_Testcase1', 'MVorders1', { TableAssembly table -> clearConditions(table) }, null) { Worksheet ws, BoundTableAssembly detail ->
         TableAssembly mirror = (TableAssembly) ws.getAssembly('Query1')
         ColumnRef orderno = (ColumnRef) ((ConditionList) mirror.getMVUpdatePreConditionList()).getConditionItem(0).getAttribute()
         clearConditions(mirror)
         mirror.setMVUpdatePreConditionList(atLeast(orderno, 5))
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = rows(false)

      and: '5 and 6 are in 2022, 4 (in 2023) is below the append condition'
      insertOrders(5: '2022-01-01', 6: '2022-03-01', 4: '2023-01-01')
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = rows(true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 4")
      mvtest.rebuildMV(null, null, false)

      then: 'Year(orderdate), Sum(orderno)'
      verifyAll {
         created == ['Query1|TopMV']
         updated == ['Query1|TopMV']
         createdRows == [['2020-01-01 00:00:00', '3'], ['2021-01-01 00:00:00', '3']]
         updatedRows == [['2020-01-01 00:00:00', '3'], ['2021-01-01 00:00:00', '3'],
                         ['2022-01-01 00:00:00', '11']]
         mvtest.compareWithRebuild()
      }
   }

   // MV conditions on a minus table, as in Append_Spec M_Testcase6: MinusTable is Query1 minus
   // Query2, mirrors of MVorderdetails1 and MVorderdetails2, and the MV is on MinusTable. Here
   // MVorderdetails1 is bound to the case's table and MVorderdetails2 to a view of its rows with
   // quantity >= 100, so the minus table has the rows with quantity < 100. (A union of tables
   // has the MVs on its tables, M_Testcase5, and a rotated table on the table it rotates,
   // M_Testcase9 and M_Testcase22, so their own MV conditions are not used.)
   def 'append and delete conditions on a minus table: the new rows of the minus are appended, the same as a rebuilt MV'() {
      given:
      source.createLike('MVorderdetails')
      source.execute("CREATE VIEW ${VIEW} AS SELECT * FROM ${TABLE} WHERE quantity >= 100")
      insertDetails(101: 1, 102: 1, 103: 1, 104: 200)
      copyAssets('MV_Creator/Append/M_Testcase6', 'MVorderdetails1', { TableAssembly table -> clearConditions(table) }, null) { Worksheet ws, BoundTableAssembly left ->
         BoundTableAssembly right = (BoundTableAssembly) ws.getAssembly('MVorderdetails2')
         right.getSourceInfo().setSource(VIEW)
         // the asset filters both tables on orderno (not MV conditions), which would leave none
         // of the case's rows
         left.setPreConditionList(new ConditionList())
         right.setPreConditionList(new ConditionList())
         ['MVorderdetails2', 'Query1', 'Query2'].each { clearConditions((TableAssembly) ws.getAssembly(it)) }
         TableAssembly minus = (TableAssembly) ws.getAssembly('MinusTable')
         ColumnRef orderno = (ColumnRef) ((ConditionList) minus.getMVUpdatePreConditionList()).getConditionItem(0).getAttribute()
         clearConditions(minus)
         minus.setMVUpdatePreConditionList(atLeast(orderno, 105))
         minus.setMVDeletePreConditionList(oneOf(orderno, [102, 108]))
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = values('orderno', false)

      and: '105 is new, 107 is in the view (quantity 300), 100 is below the append condition'
      insertDetails(105: 1, 107: 300, 100: 1)
      update()
      def updated = mvtest.takeMVHits()
      def updatedRows = values('orderno', true)

      and: 'the database gets the rows of the MV, so a rebuilt MV has them too'
      source.execute("DELETE FROM ${TABLE} WHERE orderno = 100")
      mvtest.rebuildMV(null, null, false)

      then: '104 and 107 are in the view, 102 is left out by the delete condition'
      verifyAll {
         created == ['MinusTable|TopMV']
         updated == ['MinusTable|TopMV']
         createdRows == ['101', '103']
         updatedRows == ['101', '103', '105']
         mvtest.compareWithRebuild()
      }
   }

   // A delete condition with an MV. variable, which the MV creation ignores, so the MV has every
   // row and only the update deletes. customer_id is empty on two rows: the update deletes them
   // too, as the MV creation and the database do for the same condition (#80570 for "not equal
   // to", #81043 for the comparisons). A rebuilt MV keeps every row, so the rows are checked.
   @Issue(['https://issues.inetsoft.com/issues/80570', 'https://issues.inetsoft.com/issues/81043'])
   def 'delete condition customer_id #name 103: the update deletes the rows with an empty customer_id too'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-03', 4: '2020-01-04',
                   5: ['2020-01-05', 'NULL', 0], 6: ['2020-01-06', 'NULL', 0])
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1') { BoundTableAssembly table ->
         clearConditions(table)
         ConditionList delete = expression(column(table, 'customer_id'), op,
                                           'MV.LastUpdateTime != null ? 103 : null')
         delete.getConditionItem(0).getXCondition().setNegated(negated)
         table.setMVDeletePreConditionList(delete)
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: 'no new rows, the update only deletes'
      update()
      def updated = mvtest.takeMVHits()

      then:
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [1, 2, 3, 4, 5, 6]
         orders(true) == left
      }

      where:
      name           | op                     | negated | left
      'not equal to' | XCondition.EQUAL_TO    | true    | [3]
      'less than'    | XCondition.LESS_THAN   | false   | [3, 4]
   }

   // The table is read through a view that waits 1.5 s for each row, so an update reads for a few
   // seconds. A row inserted while the update reads (seen in H2 as a running query on the view)
   // is not read by that update, and must be taken by the next one: the last update time is the
   // time the update started reading, not the time it ended, which is after the new row (#79854).
   // Real times are used, the case does not set the last update time.
   @Issue('https://issues.inetsoft.com/issues/79854')
   def 'a row inserted while an update reads the source: the next update appends it'() {
      given:
      source.createLike('MVorders')
      source.execute("CREATE ALIAS ${SLEEP} FOR 'java.lang.Thread.sleep(long)'")
      source.execute("CREATE VIEW ${VIEW} AS SELECT * FROM ${TABLE} WHERE ${SLEEP}(1500) IS NULL")
      insertOrders(1: '2020-01-01')
      copyOrdersAssets('MV.LastUpdateTime', null, null, { Worksheet ws, BoundTableAssembly table ->
         table.getSourceInfo().setSource(VIEW)
      })
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeMVHits()
      def createdRows = orders(false)

      and: 'row 2 is new; row 3 is inserted while the update reads the view'
      insertOrderAt(2, new Timestamp(System.currentTimeMillis()))
      Throwable insertFailure = null
      Thread inserter = Thread.start {
         try {
            waitForQueryOn(VIEW)
            insertOrderAt(3, new Timestamp(System.currentTimeMillis()))
         }
         catch(Throwable e) {
            insertFailure = e
         }
      }
      update()
      inserter.join(60000)
      def firstRows = orders(true)

      and: 'the next update'
      update()
      def updated = mvtest.takeMVHits()

      then:
      insertFailure == null
      !inserter.isAlive()
      verifyAll {
         created == ['MVorders1|TopMV']
         updated == ['MVorders1|TopMV']
         createdRows == [1]
         assert firstRows == [1, 2]: 'the first update read row 3, so the row was inserted before that ' +
            'update read the view (a timing problem of the case, not the defect of #79854)'
         orders(true) == [1, 2, 3]
      }
   }

   /** Insert a row of the orders table with customer_id 100 + no, discount 0 and the time. */
   private void insertOrderAt(int no, Timestamp time) {
      source.execute("INSERT INTO ${TABLE} (orderno, customer_id, discount, orderdate) " +
                     "VALUES (${no}, ${100 + no}, 0, TIMESTAMP '${time}')")
   }

   /** Wait until a session of the mvtest database runs a query on the view (30 s at most). */
   private void waitForQueryOn(String view) {
      JDBCDataSource ds = (JDBCDataSource) DataSourceRegistry.getRegistry().getDataSource('mvtest')
      Connection conn = new JDBCHandler().getConnection(ds, admin)

      try {
         long end = System.currentTimeMillis() + 30000

         while(System.currentTimeMillis() < end) {
            boolean running = conn.createStatement().withCloseable { Statement stmt ->
               stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS " +
                                 "WHERE UPPER(EXECUTING_STATEMENT) LIKE '%${view}%' " +
                                 "AND EXECUTING_STATEMENT NOT LIKE '%INFORMATION_SCHEMA%'")
                  .withCloseable { ResultSet rs -> rs.next() && rs.getInt(1) > 0 }
            }

            if(running) {
               return
            }

            Thread.sleep(100)
         }

         throw new AssertionError('no query on ' + view + ' seen in INFORMATION_SCHEMA.SESSIONS ' +
            'within 30 s: the update did not read the view, or H2 no longer reports EXECUTING_STATEMENT')
      }
      finally {
         conn.close()
      }
   }

   private static void clearConditions(TableAssembly table) {
      table.setMVUpdatePreConditionList(new ConditionList())
      table.setMVDeletePreConditionList(new ConditionList())
      table.setMVUpdatePostConditionList(new ConditionList())
      table.setMVDeletePostConditionList(new ConditionList())
   }

   /** orderno -> quantity, with product_id 1. */
   private void insertDetails(Map<Integer, Integer> rows) {
      rows.each { no, quantity ->
         source.execute("INSERT INTO ${TABLE} (orderno, product_id, quantity) VALUES (${no}, 1, ${quantity})")
      }
   }

   /** {@code col <op> expression}, with the type of the column. */
   private static ConditionList expression(ColumnRef col, int op, String expression) {
      AssetCondition cond = new AssetCondition(col.getDataType())
      cond.setOperation(op)
      ExpressionValue value = new ExpressionValue()
      value.setType(ExpressionValue.JAVASCRIPT)
      value.setExpression(expression)
      cond.addValue(value)
      return single(col, cond)
   }

   // the asset names the columns string and timeinstant, the database STRING and TIMEINSTANT
   private void insertValues(String column, List values) {
      values.each { source.execute("INSERT INTO ${TABLE} (\"${column.toUpperCase()}\") VALUES (${literal(column, it)})") }
   }

   private static String literal(String column, Object value) {
      switch(column) {
         case 'DOUBLE': return String.valueOf(value)
         case 'timeinstant': return "TIMESTAMP '${value} 00:00:00'"
         default: return "'${value}'"
      }
   }

   /** A value as the export shows it. */
   private static String shown(String column, Object value) {
      return column == 'timeinstant' ? value + ' 00:00:00' : String.valueOf(value)
   }

   /** The values of the first column of the exported table, sorted as strings. */
   private List<String> firstColumn(boolean incremental) {
      return rows(incremental).collect { it[0] }.sort()
   }

   /**
    * MV_Creator/Append/mvLastUpdatetime grouped by customer_id with Sum(discount), with the
    * post-aggregate append condition {@code customer_id >= 105} and delete condition
    * {@code customer_id one of 102, 106}, and no pre-aggregate MV conditions.
    */
   private void copyCustomerGroupedAssets() {
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1', { BoundTableAssembly table ->
         ColumnSelection cols = table.getColumnSelection(false)
         ColumnRef customer = column(table, 'customer_id')
         AggregateInfo ainfo = new AggregateInfo()
         ainfo.setGroups([new GroupRef(customer)] as GroupRef[])
         ainfo.setAggregates([new AggregateRef(cols.getAttribute('discount'), AggregateFormula.SUM)] as AggregateRef[])
         table.setAggregateInfo(ainfo)
         table.setAggregate(true)
         table.setMVUpdatePreConditionList(new ConditionList())
         table.setMVDeletePreConditionList(new ConditionList())
         table.setMVUpdatePostConditionList(atLeast(customer, 105))
         table.setMVDeletePostConditionList(oneOf(customer, [102, 106]))
      }, { Viewsheet vs ->
         TableVSAssembly table = (TableVSAssembly) vs.getAssembly('Query1')
         ColumnSelection cols = new ColumnSelection()
         ['customer_id', 'discount'].each { cols.addAttribute(new ColumnRef(new AttributeRef(null, it))) }
         table.setColumnSelection(cols)
      })
   }

   /**
    * MV_Creator/Append/M_Testcase2, the inner join of MVorders1 and MVorders2 on customer_id,
    * with both tables bound to the case's table and without their own MV conditions, and on the
    * join the append condition {@code MVorders1.customer_id >= 105} and the delete condition
    * {@code MVorders1.customer_id one of 102, 106}.
    */
   private void copyJoinAssets() {
      Closure noConditions = { TableAssembly table ->
         table.setMVUpdatePreConditionList(new ConditionList())
         table.setMVDeletePreConditionList(new ConditionList())
      }

      copyAssets('MV_Creator/Append/M_Testcase2', 'MVorders1', noConditions, null) { Worksheet ws, BoundTableAssembly left ->
         BoundTableAssembly right = (BoundTableAssembly) ws.getAssembly('MVorders2')
         right.getSourceInfo().setSource(TABLE)
         noConditions(right)
         TableAssembly join = (TableAssembly) ws.getAssembly('Query1')
         ColumnRef customer = column(join, 'MVorders1.customer_id')
         join.setMVUpdatePreConditionList(atLeast(customer, 105))
         join.setMVDeletePreConditionList(oneOf(customer, [102, 106]))
      }
   }

   private static ColumnRef column(TableAssembly table, String name) {
      ColumnRef col = (ColumnRef) table.getColumnSelection(false).getAttribute(name)
      assert col != null: table.getName() + ' has no column ' + name
      return col
   }

   /** {@code col >= from}. */
   private static ConditionList atLeast(ColumnRef col, int from) {
      AssetCondition cond = new AssetCondition(XSchema.INTEGER)
      cond.setOperation(XCondition.GREATER_THAN)
      cond.setEqual(true)
      cond.addValue(from)
      return single(col, cond)
   }

   /** {@code col > MV.MaxValue}. */
   private static ConditionList afterMaxValue(ColumnRef col) {
      AssetCondition cond = new AssetCondition(XSchema.INTEGER)
      cond.setOperation(XCondition.GREATER_THAN)
      ExpressionValue value = new ExpressionValue()
      value.setType(ExpressionValue.JAVASCRIPT)
      value.setExpression('MV.MaxValue')
      cond.addValue(value)
      return single(col, cond)
   }

   /** {@code col one of values}. */
   private static ConditionList oneOf(ColumnRef col, List<Integer> values) {
      AssetCondition cond = new AssetCondition(XSchema.INTEGER)
      cond.setOperation(XCondition.ONE_OF)
      values.each { cond.addValue(it) }
      return single(col, cond)
   }

   private static ConditionList single(ColumnRef col, AssetCondition cond) {
      ConditionList list = new ConditionList()
      list.append(new ConditionItem(col, cond, 0))
      return list
   }

   /** The values of a column of the exported table, found by its header, sorted as numbers. */
   private List<String> values(String header, boolean incremental) {
      List<List<String>> lines = exported('Query1', incremental).drop(1).findAll { it }.collect {
         (it.substring(1, it.length() - 1).split(/\]\[/) as List<String>)
      }
      int idx = lines[0].indexOf(header)
      assert idx >= 0: 'no column ' + header + ' in ' + lines[0]
      return lines.drop(1).collect { it[idx] }.sort { it as double }
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
   private List<String> exported(String name, boolean incremental, String user = 'admin') {
      File folder = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                             'exportedData/' + PATH)
      File file = new File(folder, user + '_' + name + (incremental ? '__MVIncremental.txt' : '__MV.txt'))
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   /** The rows of the exported table, without the size and the header. */
   private List<List<String>> rows(boolean incremental, String user = 'admin') {
      return exported('Query1', incremental, user).drop(2).findAll { it }.collect {
         (it.substring(1, it.length() - 1).split(/\]\[/) as List<String>)
      }
   }

   private List<Integer> integers(boolean incremental) {
      return rows(incremental).collect { it[0] as int }.sort()
   }

   /** The ordernos of the exported table (columns customer_id, discount, orderdate, orderno). */
   private List<Integer> orders(boolean incremental, String user = 'admin') {
      return rows(incremental, user).collect { it[3] as int }.sort()
   }

   private List<String> listValues(boolean incremental) {
      return exported('SL', incremental).collect { (it =~ /^\[([^\]]*)\]/)[0][1] as String }
   }

   /** The labels of the min and max values of a slider. */
   private List<String> sliderRange(String name, boolean incremental) {
      return exported(name, incremental).take(2).collect { (it =~ /: \[(.*)\]$/)[0][1] as String }
   }
}

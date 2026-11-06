package inetsoft.test.mv.cases.wsmv

import inetsoft.report.XSessionManager
import inetsoft.report.composition.execution.AssetDataCache
import inetsoft.sree.SreeEnv
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVSourceTable
import inetsoft.test.mv.MVTest
import inetsoft.uql.ConditionList
import inetsoft.uql.XCondition
import inetsoft.uql.asset.AssetCondition
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.BoundTableAssembly
import inetsoft.uql.asset.ExpressionValue
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.viewsheet.Viewsheet
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Timestamp

/**
 * One path through a worksheet MV (WS MV): the MV is created for the worksheet only, the
 * viewsheet based on the worksheet reads it, and incremental updates append and delete rows.
 * <p>
 * The worksheet and the viewsheet are copies of the {@code MV_Creator/Append/mvLastUpdatetime}
 * assets bound to a table the case creates in the H2 database {@code mvorder1} (data source
 * {@code mvtest}); both are removed when the case ends, and {@code mvStorage.zip} is not changed.
 * {@code ws.mv.enabled} is turned on for the case and restored afterwards.
 * <p>
 * The viewsheet reads the MV and not the database: a row added only to the source table (the
 * sentinel) never shows while the MV is used.
 * <p>
 * Running the case changes {@code docker/src/test/resources/H2Data/mvorder1.mv.db}, even though
 * the table is dropped; restore it with git before committing.
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
class WSMV_Path_Spec extends Specification {
   static final String PATH = 'MV_Creator/Append/wsmv_path'
   // MVTest names the export folder after the part of the id after the last ^, so no org
   static final String VS_ID = '1^128^__NULL__^' + PATH
   static final String WS_ENTRY = '1^2^__NULL__^' + PATH + '^host-org'
   static final String VS_ENTRY = VS_ID + '^host-org'
   static final String TABLE = 'MVINC_WSMV_PATH'
   static final String WS_MV_ENABLED = 'ws.mv.enabled'
   // added to the source table after the MV is created, older than every last update time
   static final int SENTINEL = 9

   @Shared def admin
   def mvtest
   MVSourceTable source
   String wsMVEnabled

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }

      source = new MVSourceTable('mvtest', TABLE, admin)
      wsMVEnabled = SreeEnv.getProperty(WS_MV_ENABLED)
      SreeEnv.setProperty(WS_MV_ENABLED, 'true')
   }

   def cleanup() {
      def repository = AssetUtil.getAssetRepository(false)

      // every step runs even when an earlier one fails, so nothing is left for the next case
      runAll(
         { mvtest?.removeMV() },
         { source.drop() },
         { repository.removeSheet(AssetEntry.createAssetEntry(VS_ENTRY), admin, true) },
         { repository.removeSheet(AssetEntry.createAssetEntry(WS_ENTRY), admin, true) },
         { restoreWSMVEnabled() })
   }

   private void restoreWSMVEnabled() {
      if(wsMVEnabled == null) {
         SreeEnv.remove(WS_MV_ENABLED)
      }
      else {
         SreeEnv.setProperty(WS_MV_ENABLED, wsMVEnabled)
      }
   }

   def 'worksheet MV: created, read by the viewsheet, appended and deleted incrementally'() {
      given:
      source.createLike('MVorders')
      insertOrders(1: '2020-01-01', 2: '2020-01-02', 3: '2020-01-08')
      copyAssets()
      mvtest = new MVTest(VS_ID, WS_ENTRY)

      when: 'no MV, then a worksheet MV'
      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      clearCaches()
      mvtest.executeVS(null, null, false, false)
      def created = mvtest.takeWSMVHits()
      def createdRows = orders(false)

      and: 'a row added to the source only: the viewsheet reads the MV, so it does not show it'
      insertOrders((SENTINEL): '2020-01-05')
      clearCaches()
      mvtest.executeVS(null, null, false, false)
      def sentinelHits = mvtest.takeWSMVHits()
      def sentinelRows = orders(false)
      // compared here: the control at the end exports the same file again
      mvtest.compareData(false)
      mvtest.compareWithoutMV()

      and: 'append: last update time 2020-01-10, 4 and 5 are newer, the delete condition (before 2019-12-11) deletes nothing'
      mvtest.setLastUpdateTime(time('2020-01-10'))
      insertOrders(4: '2020-01-12', 5: '2020-01-15')
      update()
      def appended = mvtest.takeWSMVHits()
      def appendedRows = orders(true)

      and: 'delete: last update time 2020-02-05, the delete condition (before 2020-01-06) deletes 1 and 2, nothing is newer'
      mvtest.setLastUpdateTime(time('2020-02-05'))
      update()
      def deleted = mvtest.takeWSMVHits()
      def deletedRows = orders(true)
      // compared here: the export of the append is replaced by this one and checked by its rows
      mvtest.compareData(true)

      and: 'control: with ws.mv.enabled off the viewsheet reads the source table, which has the sentinel'
      SreeEnv.setProperty(WS_MV_ENABLED, 'false')
      clearCaches()
      mvtest.executeVS(null, null, false, false)
      def liveRows = orders(false)

      then:
      verifyAll {
         !created.isEmpty()
         created == mvtest.materializedViewNames()
         createdRows == [1, 2, 3]
         sentinelHits == created
         sentinelRows == [1, 2, 3]
         appended == created
         appendedRows == [1, 2, 3, 4, 5]
         deleted == created
         deletedRows == [3, 4, 5]
         liveRows == [1, 2, 3, 4, 5, SENTINEL]
      }
   }

   private void update() {
      mvtest.createIncrementMV(1)
      clearCaches()
      mvtest.executeVS(null, null, true, false)
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

   private static void clearCaches() {
      AssetDataCache.getCache().clearCache()
      XSessionManager.getSessionManager().clearCache()
   }

   /** orderno -> orderdate, with customer_id 100 + orderno and discount 0. */
   private void insertOrders(Map<Integer, String> rows) {
      rows.each { no, date ->
         source.execute("INSERT INTO ${TABLE} (orderno, customer_id, discount, orderdate) " +
                        "VALUES (${no}, ${100 + no}, 0, TIMESTAMP '${date} 00:00:00')")
      }
   }

   private static long time(String date) {
      return Timestamp.valueOf(date + ' 00:00:00').getTime()
   }

   /**
    * Copy the MV_Creator/Append/mvLastUpdatetime worksheet and viewsheet, with the table bound
    * to the case's table, the append condition {@code orderdate > MV.LastUpdateTime} and the
    * delete condition {@code orderdate < MV.LastUpdateTime - 30 days}. The MV creation ignores
    * a delete condition with {@code MV.} variables, so it deletes nothing when the MV is created.
    */
   private void copyAssets() {
      def repository = AssetUtil.getAssetRepository(false)
      String path = 'MV_Creator/Append/mvLastUpdatetime'
      Worksheet ws = (Worksheet) repository.getSheet(
         AssetEntry.createAssetEntry("1^2^__NULL__^${path}^host-org"), admin, false, AssetContent.ALL).clone()
      def table = (BoundTableAssembly) ws.getAssembly('MVorders1')
      table.getSourceInfo().setSource(TABLE)

      ConditionList appends = (ConditionList) ((ConditionList) table.getMVUpdatePreConditionList()).clone()
      setExpression((AssetCondition) appends.getConditionItem(0).getXCondition(),
                    XCondition.GREATER_THAN, 'MV.LastUpdateTime')
      table.setMVUpdatePreConditionList(appends)

      ConditionList deletes = (ConditionList) ((ConditionList) table.getMVDeletePreConditionList()).clone()
      setExpression((AssetCondition) deletes.getConditionItem(0).getXCondition(),
                    XCondition.LESS_THAN, 'new Date(MV.LastUpdateTime.getTime() - 30 * 86400000)')
      table.setMVDeletePreConditionList(deletes)
      repository.setSheet(AssetEntry.createAssetEntry(WS_ENTRY), ws, admin, true)

      Viewsheet vs = (Viewsheet) repository.getSheet(
         AssetEntry.createAssetEntry("1^128^__NULL__^${path}^host-org"), admin, false, AssetContent.ALL).clone()
      vs.setBaseEntry(AssetEntry.createAssetEntry(WS_ENTRY))
      repository.setSheet(AssetEntry.createAssetEntry(VS_ENTRY), vs, admin, true)
   }

   private static void setExpression(AssetCondition cond, int op, String expression) {
      cond.setOperation(op)
      cond.setEqual(false)
      ExpressionValue value = (ExpressionValue) cond.getValue(0)
      value.setType(ExpressionValue.JAVASCRIPT)
      value.setExpression(expression)
   }

   private List<String> exported(String name, boolean incremental, String user = 'admin') {
      File folder = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                             'exportedData/' + PATH)
      File file = new File(folder, user + '_' + name + (incremental ? '__MVIncremental.txt' : '__MV.txt'))
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   /** The rows of the exported table, without the size and the header. */
   private List<List<String>> rows(boolean incremental) {
      return exported('Query1', incremental).drop(2).findAll { it }.collect {
         (it.substring(1, it.length() - 1).split(/\]\[/) as List<String>)
      }
   }

   /** The ordernos of the exported table (columns customer_id, discount, orderdate, orderno). */
   private List<Integer> orders(boolean incremental) {
      return rows(incremental).collect { it[3] as int }.sort()
   }
}

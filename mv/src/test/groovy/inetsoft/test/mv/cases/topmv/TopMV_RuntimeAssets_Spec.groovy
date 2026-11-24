package inetsoft.test.mv.cases.topmv

import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVSourceTable
import inetsoft.test.mv.MVTest
import inetsoft.uql.ColumnSelection
import inetsoft.uql.ConditionItem
import inetsoft.uql.ConditionList
import inetsoft.uql.XCondition
import inetsoft.uql.asset.AggregateInfo
import inetsoft.uql.asset.AssetCondition
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.AssetVariable
import inetsoft.uql.asset.BoundTableAssembly
import inetsoft.uql.asset.ColumnRef
import inetsoft.uql.asset.DefaultVariableAssembly
import inetsoft.uql.asset.SQLBoundTableAssembly
import inetsoft.uql.asset.SortInfo
import inetsoft.uql.asset.TableAssembly
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.erm.AttributeRef
import inetsoft.uql.erm.ExpressionRef
import inetsoft.uql.jdbc.UniformSQL
import inetsoft.uql.schema.UserVariable
import inetsoft.uql.schema.XSchema
import inetsoft.uql.schema.XValueNode
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.uql.viewsheet.Viewsheet
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Types

/**
 * MV cases on a worksheet and a viewsheet that the case makes at run time, as a copy of
 * existing assets with the table, columns and conditions it needs, removed when it ends;
 * {@code mvStorage.zip} is not changed. A case can also create a table in the local H2 database
 * {@code mvorder1} (data source {@code mvtest}) and drop it at the end. Each case runs the
 * viewsheet without and with MV and checks the rows of the MV run itself, so it also fails in
 * the default {@code golden} mode,
 * where {@link MVTest#compareWithoutMV()} does nothing; it has no golden files.
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
class TopMV_RuntimeAssets_Spec extends Specification {
   static final String PATH = 'MV_Creator/Append/runtime_assets'
   // MVTest names the export folder after the part of the id after the last ^, so no org
   static final String VS_ID = '1^128^__NULL__^' + PATH
   static final String WS_ENTRY = '1^2^__NULL__^' + PATH + '^host-org'
   static final String VS_ENTRY = VS_ID + '^host-org'
   // a table a case creates in the local H2 database mvorder1 (data source mvtest)
   static final String TZ_TABLE = 'MVRT_TZ'

   @Shared def admin
   def mvtest
   MVSourceTable table

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

      // every step runs even when an earlier one fails, so nothing is left for the next case
      [{ mvtest?.removeMV() },
       { table?.drop() },
       { repository.removeSheet(AssetEntry.createAssetEntry(VS_ENTRY), admin, true) },
       { repository.removeSheet(AssetEntry.createAssetEntry(WS_ENTRY), admin, true) }].each { Closure step ->
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

   // The H2 table MVorders (120 rows, customer_id 1 to 20 and one empty) with the condition
   // customer_id <op> ?v, where v is a Long variable with a default value outside the int range.
   // The MV column of customer_id is an int column: a bound outside its range used to wrap around
   // (3000000000 became a negative int, so "> 3000000000" kept every row; 2^32 + 1 became 1, so
   // "< 4294967297" kept none), and the default value seemed not to be applied with MV (#80155).
   // Without MV, "= 4294967297" used to narrow the value to an int too and returned the 6 rows of
   // customer_id 1 (#82099); "= 1" checks that the equal condition still selects rows.
   @Issue(['https://issues.inetsoft.com/issues/80155', 'https://issues.inetsoft.com/issues/82099'])
   def 'Long variable default #value in customer_id #name ?v: the same rows with and without MV'() {
      given:
      copyAssets('MV_Creator/Append/mvLastUpdatetime', 'MVorders1') { Worksheet ws, BoundTableAssembly table ->
         clearMVConditions(table)
         addLongVariable(ws, 'v', value)

         if(op == XCondition.EQUAL_TO) {
            // the asset stores customer_id as VARCHAR (sqlType 12), so "=" would bind the value
            // as a string and H2 rejects 4294967297; use the type of the H2 column, INTEGER
            ((AttributeRef) column(table, 'customer_id').getDataRef()).setSqlType(Types.INTEGER)
         }

         AssetCondition cond = new AssetCondition(XSchema.INTEGER)
         cond.setOperation(op)
         cond.addValue(new UserVariable('v'))
         table.setPreConditionList(single(column(table, 'customer_id'), cond))
      }
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['MVorders1|TopMV'])
         rowCount('Query1') == rows
         mvtest.compareWithoutMV()
      }

      where:
      name           | op                      | value        | rows
      'greater than' | XCondition.GREATER_THAN | 3000000000L  | 0
      'greater than' | XCondition.GREATER_THAN | -3000000000L | 119
      'less than'    | XCondition.LESS_THAN    | 4294967297L  | 119
      'equal to'     | XCondition.EQUAL_TO     | 4294967297L  | 0
      'equal to'     | XCondition.EQUAL_TO     | 1L           | 6
   }

   // topmv/v-contains (DB2 ORDERS.PRODUCTS, PRODUCT_NAME not contains ?product_name, here "a")
   // with the condition moved to a script expression column E = PRODUCT_NAME + ''. DB2 compares
   // case-sensitively; the MV used to apply that rule (case-insensitive otherwise) to E too,
   // although E is computed after the query, so it kept MeToo AppServer and True Action, which
   // have an "A" and no "a" (#80152). Without MV the table has 9 rows.
   @Issue('https://issues.inetsoft.com/issues/80152')
   def 'not contains variable on a script expression column (DB2): the same rows with and without MV'() {
      given:
      copyAssets('topmv/v-contains', 'PRODUCTS1') { Worksheet ws, BoundTableAssembly table ->
         ColumnSelection cols = table.getColumnSelection(false)
         ExpressionRef expr = new ExpressionRef(null, 'E')
         expr.setExpression("field['PRODUCT_NAME'] + ''")
         ColumnRef e = new ColumnRef(expr)
         e.setDataType(XSchema.STRING)
         // a script column, as the expression dialog makes it; SQL would be sent to DB2
         e.setSQL(false)
         cols.addAttribute(e)
         table.setColumnSelection(cols, false)

         ConditionList conds = (ConditionList) table.getPreConditionList().getConditionList().clone()
         assert conds.getSize() == 1: 'expected one condition: ' + conds
         conds.getConditionItem(0).setAttribute(e)
         table.setPreConditionList(conds)
      }
      mvtest = new MVTest(VS_ID)
      def params = ['product_name': ['a'] as String[]]

      when:
      mvtest.executeVSWithoutMV(params, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(params, null, false, false)

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['PRODUCTS1|TopMV'])
         rowCount('Query1') == 9
         mvtest.compareWithoutMV()
      }
   }

   // MV_Transform/TopMV/dategroup with its SQL query table query11 (data source mvtest) changed to
   // a table with a TIMESTAMP WITH TIME ZONE column, whose type is set to TimeInstant as in the
   // worksheet column type dialog, and the viewsheet table bound to it (the mirror is removed).
   // The grouping and sorting of the copied table refer to its old columns and are removed too.
   // The driver returns the values as java.time objects, which the MV used to drop, so the column
   // was empty with MV (#79858).
   @Issue('https://issues.inetsoft.com/issues/79858')
   def 'SQL query column TIMESTAMP WITH TIME ZONE of type TimeInstant (H2): the same values with and without MV'() {
      given:
      table = new MVSourceTable('mvtest', TZ_TABLE, admin)
      table.execute("DROP TABLE IF EXISTS ${TZ_TABLE}")
      table.execute("CREATE TABLE ${TZ_TABLE} (ORDERNO INT, ORDERDATE TIMESTAMP WITH TIME ZONE)")
      table.execute("INSERT INTO ${TZ_TABLE} VALUES (1, TIMESTAMP WITH TIME ZONE '2020-01-01 00:00:00+00'), " +
                    "(2, TIMESTAMP WITH TIME ZONE '2020-01-02 10:30:00+08')")
      copyAssets('MV_Transform/TopMV/dategroup', 'query11', { Worksheet ws, SQLBoundTableAssembly sql ->
         ws.removeAssembly('Query1')
         clearMVConditions(sql)
         sql.setAggregateInfo(new AggregateInfo())
         sql.setAggregate(false)
         sql.setSortInfo(new SortInfo())
         sql.getTableInfo().getQuery().setSQLDefinition(new UniformSQL(
            "select ${TZ_TABLE}.ORDERNO, ${TZ_TABLE}.ORDERDATE from ${TZ_TABLE}".toString(), true))
         ColumnSelection cols = new ColumnSelection()
         cols.addAttribute(typed('ORDERNO', XSchema.INTEGER))
         cols.addAttribute(typed('ORDERDATE', XSchema.TIME_INSTANT))
         sql.setColumnSelection(cols, false)
         sql.setColumnSelection((ColumnSelection) cols.clone(), true)
      }, { Viewsheet vs ->
         TableVSAssembly view = (TableVSAssembly) vs.getAssembly('Query1')
         view.setTableName('query11')
         ColumnSelection cols = new ColumnSelection()
         ['ORDERNO', 'ORDERDATE'].each { cols.addAttribute(new ColumnRef(new AttributeRef(null, it))) }
         view.setColumnSelection(cols)
      })
      mvtest = new MVTest(VS_ID)

      when:
      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)
      def dates = columnValues('Query1', 'ORDERDATE')

      then:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query11|TopMV'])
         dates.size() == 2
         dates.every { it && it != '[NULL]' && it != 'NULL' }
         dates[0] != dates[1]
         mvtest.compareWithoutMV()
      }
   }

   private static ColumnRef typed(String name, String type) {
      ColumnRef col = new ColumnRef(new AttributeRef(null, name))
      col.setDataType(type)
      return col
   }

   /** The values of a column of an exported table of the MV run, found by its header. */
   private List<String> columnValues(String name, String header) {
      List<List<String>> lines = exported(name).drop(1).findAll { it }.collect {
         (it.substring(1, it.length() - 1).split(/\]\[/, -1) as List<String>)
      }
      int idx = lines[0].indexOf(header)
      assert idx >= 0: 'no column ' + header + ' in ' + lines[0]
      return lines.drop(1).collect { it[idx] }
   }

   private static void addLongVariable(Worksheet ws, String name, long value) {
      DefaultVariableAssembly assembly = new DefaultVariableAssembly(ws, name)
      AssetVariable var = new AssetVariable()
      var.setName(name)
      var.setTypeNode(XSchema.createPrimitiveType(XSchema.LONG))
      var.setValueNode(XValueNode.createValueNode(value, name, XSchema.LONG))
      var.setPrompt(false)
      assembly.setVariable(var)
      ws.addAssembly(assembly)
   }

   private static void clearMVConditions(TableAssembly table) {
      table.setMVUpdatePreConditionList(new ConditionList())
      table.setMVDeletePreConditionList(new ConditionList())
      table.setMVUpdatePostConditionList(new ConditionList())
      table.setMVDeletePostConditionList(new ConditionList())
   }

   private static ColumnRef column(TableAssembly table, String name) {
      ColumnRef col = (ColumnRef) table.getColumnSelection(false).getAttribute(name)
      assert col != null: table.getName() + ' has no column ' + name
      return col
   }

   private static ConditionList single(ColumnRef col, AssetCondition cond) {
      ConditionList list = new ConditionList()
      list.append(new ConditionItem(col, cond, 0))
      return list
   }

   /**
    * Copy the viewsheet at path and its worksheet to {@link #PATH}. changeWS is called with the
    * worksheet and its table tableName, changeVS (if not null) with the viewsheet.
    */
   private void copyAssets(String path, String tableName, Closure changeWS, Closure changeVS = null) {
      def repository = AssetUtil.getAssetRepository(false)
      Viewsheet vs = (Viewsheet) repository.getSheet(
         AssetEntry.createAssetEntry("1^128^__NULL__^${path}^host-org"), admin, false, AssetContent.ALL).clone()
      Worksheet ws = (Worksheet) repository.getSheet(vs.getBaseEntry(), admin, false, AssetContent.ALL).clone()
      changeWS(ws, ws.getAssembly(tableName))
      repository.setSheet(AssetEntry.createAssetEntry(WS_ENTRY), ws, admin, true)

      vs.setBaseEntry(AssetEntry.createAssetEntry(WS_ENTRY))
      changeVS?.call(vs)
      repository.setSheet(AssetEntry.createAssetEntry(VS_ENTRY), vs, admin, true)
   }

   /** The number of rows of an exported table of the MV run, from its size line. */
   private int rowCount(String name) {
      def size = (exported(name)[0] =~ /\((\d+) x \d+\)/)
      assert size.find(): 'no size line in the export of ' + name
      return (size.group(1) as int) - 1
   }

   /** The lines of an exported file of the MV run. */
   private List<String> exported(String name) {
      File folder = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                             'exportedData/' + PATH)
      File file = new File(folder, 'admin_' + name + '__MV.txt')
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }
}

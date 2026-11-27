package inetsoft.test.worksheet.cases

import inetsoft.test.modules.WorksheetTest
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Pushed / in-memory pairs (docs/test-gap-assessment/ws.md, 8c step 5). Every condition of a worksheet is used by two
 * tables on the same rows: &lt;case&gt;_&lt;db&gt; with the condition pushed to the database, and
 * &lt;case&gt;Mem_&lt;db&gt; with the condition evaluated in memory. Instead of one baseline per table, each case
 * compares every pair itself: a pair must give the same rows, unless it is in ALLOWED with the reason (a bug number or
 * a database rule). The case writes one summary.txt (the rows each pair keeps, whether the pushed table really went to
 * SQL) and compares it with its baseline. The table files are moved to the sub folder "tables" of the export folder.
 * Worksheets from .claude/workflow-shared/datatest-gap/ws_pair_gen.py.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class WSPair_Spec extends Specification {
   static final List<String> DBS = ['Oracle', 'MySQL', 'Derby', 'PostgreSQL', 'SQLServer', 'DB2', 'Informix']
   static WorksheetTest wstest
   static String caseName

   /**
    * pushed table name -> why its in-memory twin may give other rows
    */
   static final Map<String, String> ALLOWED = allowed()

   /**
    * every allowed difference of pairDates and pairNumbers; a case name stands for the case and its negation
    */
   private static Map<String, String> allowed() {
      Map<String, String> map = [:]
      def allow = { String reason, List<String> dbs, List<String> cases ->
         dbs.each { db -> cases.each { c -> [c, c + 'Not'].each { map[it + '_' + db] = reason } } }
      }
      List<String> all = DBS
      String sw = '#82270: starting with on a date pushed with the current date or time as the value; in memory a ' +
         'date-like value (2020-02) is parsed to a date first'
      allow(sw, all, ['pdSw', 'pdtSw'])
      allow(sw, all - 'Oracle', ['tmSw'])
      allow(sw, ['Derby', 'PostgreSQL', 'Informix'], ['tsSw', 'dtSw'])
      allow('#82476: Oracle DATE with a time of day compared exactly in SQL, by day in memory', ['Oracle'],
            ['dtEq', 'dtOneOf', 'dtLe', 'dtGt', 'dtBetween'])
      allow('#82475: SQL Server TIME compared with a time value pushed to SQL fails', ['SQLServer'],
            ['tmEq', 'tmOneOf', 'tmLt', 'tmLe', 'tmGt', 'tmGe', 'tmBetween'])
      allow('#82477: TIME milliseconds ignored by = and one of in memory', ['MySQL', 'PostgreSQL', 'Informix'],
            ['tmEq', 'tmOneOf'])
      String mysqlTime = 'MySQL compares the TIME(3) CASE expression with {t ...} as text: the row equal to the ' +
         'value is lost, IN gives no row (database rule)'
      allow(mysqlTime, ['MySQL'], ['tmLe', 'tmGt', 'tmBetween'])
      allow('#82477 and ' + mysqlTime, ['MySQL'], ['tmOneOf'])

      // pairNumbers
      String numSw = '#82270: starting with on a number column: the value is converted to the column type (1 -> 1.0) ' +
         'on a double column, LIKE on a number fails on Derby, PostgreSQL, Informix'
      allow(numSw, ['Oracle', 'Derby', 'PostgreSQL', 'SQLServer', 'Informix'], ['catSw'])
      allow(numSw, ['Derby', 'PostgreSQL', 'Informix'], ['intSw'])
      allow('#82378: in memory a BigDecimal above 2^53 (Oracle NUMBER(19)) is compared as a double', ['Oracle'],
            ['bigEq', 'bigOneOf', 'bigLt', 'bigGe'])
      allow('DB2 compares a REAL with the condition value as DOUBLE: 0.1, 0.15 do not match (database rule)', ['DB2'],
            ['rlEqInexact', 'rlOneOf', 'rlGt'])
      return map
   }

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }

   def 'pairDates_#db' () {
      caseName = 'pairDates_' + db
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // TIMESTAMP, DATE and TIME columns made by an SQL CASE on PRODUCT_ID 1-11 (NULL for the others) and the real
      // PRODUCE_DATETIME, PRODUCE_DATE; =, one of, <, <=, >, >=, between, is null, starting with, each also negated.
      // two worksheets: each must run in less than the 3 minutes a runtime worksheet lives without a heartbeat
      wstest.executeWS('1^2^__NULL__^DB/pairDates1_' + db, null, null)
      wstest.executeWS('1^2^__NULL__^DB/pairDates2_' + db, null, null)
      List<String> problems = summarize(db, 'pairDates1_' + db)

      expect:
      wstest.compareData(null)
      problems.isEmpty()

      where:
      db << DBS
   }

   def 'pairNumbers_#db' () {
      caseName = 'pairNumbers_' + db
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // INTEGER, DECIMAL(12,4), BIGINT around 2^53 and REAL made by an SQL CASE on PRODUCT_ID 1-11 (NULL for the
      // others) and the real CATEGORY_ID; =, one of, <, <=, >, >=, between, is null, starting with, each also negated.
      wstest.executeWS('1^2^__NULL__^DB/pairNumbers1_' + db, null, null)
      wstest.executeWS('1^2^__NULL__^DB/pairNumbers2_' + db, null, null)
      List<String> problems = summarize(db, 'pairNumbers1_' + db)

      expect:
      wstest.compareData(null)
      problems.isEmpty()

      where:
      db << DBS
   }

   /**
    * Compare the pairs of the exported tables, write summary.txt and move the table files to "tables".
    * @return the problems: an unexpected difference, an allowed difference that is gone, a twin that is missing or
    * was not computed in memory
    */
   private static List<String> summarize(String db, String firstSheet) {
      File summary = wstest.createExportFileByCase('1^2^__NULL__^DB/' + firstSheet, 'summary', null)
      File dir = summary.parentFile
      File tables = new File(dir, 'tables')
      tables.mkdirs()
      List<String> problems = []
      List<String> lines = []
      String suffix = '_' + db + '.txt'
      def names = dir.list().findAll { it.endsWith(suffix) && !it.endsWith('.plan.txt') }.sort()

      names.findAll { !it.endsWith('Mem' + suffix) }.each { name ->
         String table = name - '.txt'
         String case_ = table - ('_' + db)
         File pushed = new File(dir, name)
         File twin = new File(dir, case_ + 'Mem' + suffix)

         if(!twin.exists()) {
            problems << table + ': no in-memory twin'
            return
         }

         Map<String, String> pushedPlan = plan(new File(dir, table + '.plan.txt'))
         Map<String, String> twinPlan = plan(new File(dir, case_ + 'Mem_' + db + '.plan.txt'))

         if(twinPlan['source'] != 'true' || twinPlan['where'] != 'false') {
            problems << table + ': the twin is not computed in memory ' + twinPlan
         }

         List<String> a = rows(pushed)
         List<String> b = rows(twin)
         // a failed query exports the placeholder row of XNodeMetaTable: PRODUCT_ID 999.99 or 999 (no such product)
         String path = ids(a) == '(1) 999' ? 'failed' :
            pushedPlan['where'] == 'true' ? 'sql' : 'memory'
         String reason = ALLOWED[table]

         if(a == b) {
            lines << String.format('%-18s %-6s %s', case_, path, ids(a))

            if(reason != null) {
               problems << table + ': allowed to differ (' + reason + '), but the pair gives the same rows'
            }
         }
         else {
            String what = ids(a) == ids(b) ? ' (same rows, other values)' : ''
            lines << String.format('%-18s %-6s %s | memory %s%s | %s', case_, path, ids(a), ids(b), what,
                                   reason ?: 'NOT ALLOWED')

            if(reason == null) {
               problems << table + ': pushed ' + a + ' but in memory ' + b
            }
         }
      }

      names.findAll { it.endsWith('Mem' + suffix) }.each { name ->
         if(!new File(dir, (name - ('Mem' + suffix)) + suffix).exists()) {
            problems << (name - '.txt') + ': no pushed table'
         }
      }

      // ALLOWED holds the tables of every case: only those of this case's columns can be missing
      Set<String> cols = names.collect { it.replaceAll(/^([a-z]+).*$/, '$1') } as Set
      ALLOWED.keySet().findAll { it.endsWith('_' + db) && cols.contains(it.replaceAll(/^([a-z]+).*$/, '$1')) &&
                                 !names.contains(it + '.txt') }.each {
         problems << it + ': in ALLOWED but not in the worksheet'
      }

      dir.listFiles().findAll { it.isFile() && it.name != 'summary.txt' }.each {
         it.renameTo(new File(tables, it.name))
      }

      summary.text = lines.join('\n') + '\n'
      return problems
   }

   private static Map<String, String> plan(File file) {
      return file.exists() ? file.readLines().findAll { it.contains(': ') }.collectEntries {
         def kv = it.split(': ', 2)
         [(kv[0]): kv[1]]
      } : [:]
   }

   /**
    * the data rows of an exported table (the export sorts them)
    */
   private static List<String> rows(File file) {
      List<String> lines = file.readLines()
      return lines.size() > 2 ? lines.subList(2, lines.size()) : []
   }

   /**
    * the PRODUCT_IDs of the rows (first column), ranges shortened: "1, 2, 12-25"
    */
   private static String ids(List<String> rows) {
      List<Integer> ids = rows.collect { (it.split(',')[0].trim() as double) as int }.sort()
      List<String> out = []
      int i = 0

      while(i < ids.size()) {
         int j = i

         while(j + 1 < ids.size() && ids[j + 1] == ids[j] + 1) {
            j++
         }

         out << (j - i >= 2 ? ids[i] + '-' + ids[j] : ids[i..j].join(', '))
         i = j + 1
      }

      return '(' + ids.size() + ') ' + out.join(', ')
   }
}

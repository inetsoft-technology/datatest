package inetsoft.test.worksheet.cases

import inetsoft.test.modules.WorksheetTest
import inetsoft.test.core.TUtil
import spock.lang.Specification
import inetsoft.sree.security.SRPrincipal
import spock.lang.Ignore
import spock.lang.Retry
import spock.lang.IgnoreRest
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class WSData_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }

   def 'Bug' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/Bug', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'innerJoin1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/innerJoin1', null, null)

      expect:
      wstest.compareData(null)
   }

   @Retry(count = 3)
   def 'innerJoin2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/innerJoin2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'crossJoin' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/crossJoin', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'concatenate' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/concatenate', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'unpivot' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/unpivot',null, null)

      expect:
      wstest.compareData(null)
   }

   //Bug #49283, Bug #49276
   def 'wsMaxRow' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/wsMaxRow', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'mirror' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/mirror', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'rotate' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/rotate', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'grouping1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/grouping1', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'grouping2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/grouping2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'condition1' () {
      def paras = new HashMap<String, Object>()
      paras.put('v_string', 'T')
      paras.put('v_num', '5')
      paras.put('v_date', '2004-11-10')
      paras.put('v_datetime', ['2002-01-01','2003-10-01'] as Object[])
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition1', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * Query1 is a rotated table of query21 (701 rows): its baseline has 200 columns because the
    * sandbox cuts a table at the organization column limit (max.col.count, default 200), not the
    * full rotated result. The first occurrence of every name comes first (#81193).
    */
    def 'condition2' () {
      def paras = new HashMap<String, Object>()
      paras.put('num1', 3)
      paras.put('num2', 5)
      paras.put('vBoolean', true)
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition2', paras, null)

      expect:
      wstest.compareData(null)
   }

    def 'range&formula' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/range&formula',null, null)

      expect:
      wstest.compareData(null)
   }

    def 'upload file' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/upload file', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'condition_session' () {
      caseName =  specificationContext.currentIteration.name
      SRPrincipal paiduser = TUtil.createPrincipal('paiduser', ['Everyone', 'Administrator'] as String[], ['DTest'] as String[])
      paiduser.setIgnoreLogin(true)
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition_session', null, null, paiduser, null)
      expect:
      wstest.compareData(null)
   }
   
   def 'specialChar' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/specialChar', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'crosstab' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/crosstab',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'databaseQuery' () {
      def paras = new HashMap<String, Object>()
      paras.put('sum_cid', '25')
      paras.put('contactid', '10')
      paras.put('y2005', '2005-01-01')
      paras.put('cid', '30')
      def params1 = ['sum_cid': ['25'] as String[], 'contactid': ['20'] as String[],
        'y2005': ['2005-01-01'] as String[], 'cid': ['30'] as String[]]
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/databaseQuery', paras, null)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/sql database query', params1)

      expect:
      wstest.compareData(['VS'] as String[])
   }

   def 'sqliteDateTest' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^LogicalChange/sqliteDateTest',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'emptyheader' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/emptyheader', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'columnalias' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/columnalias',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'SqlQuery' () {
      def paras = new HashMap<String, Object>()
      paras.put('sum_cid', '25')
      paras.put('contactid', '10')
      paras.put('y2005', '2005-01-01')
      paras.put('cid', '30')
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/sql query', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * Text input variables in the where of a DB2 query, compared with columns of other types:
    * PRICE > $(expr) (decimal), RESELLER = $(exb) (boolean in the worksheet, VARCHAR in DB2) and
    * PRODUCE_DATETIME < $(exd) (timestamp). The values sit on the boundaries of the data: a price
    * equal to expr and a timestamp equal to exd must be left out. The baselines were derived from
    * the rows of ORDERS.CUSTOMERS and ORDERS.PRODUCTS read with plain JDBC, not from the product.
    */
   def 'queryVariableTypes' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      // reseller true: price 125.00 (id 2) and 2001-01-01 01:13:13 (ids 21, 22) are left out
      wstest.executeWS('1^2^__NULL__^suquery_execute',
                       [expr: '125', exb: 'true', exd: '2001-01-01 01:13:13'], 'resellerTrue')
      // reseller false: 2006-01-01 09:09:09 (id 19) and later (ids 9, 10) are left out
      wstest.executeWS('1^2^__NULL__^suquery_execute',
                       [expr: '30', exb: 'false', exd: '2006-01-01 09:09:09'], 'resellerFalse')

      expect:
      wstest.compareData(null)
   }

   def 'setOperation' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database: <db>_A = CUSTOMERS where STATE one of NJ, NY, CA (14 rows), <db>_B = STATE one of NY, TX (8 rows);
      // <db>_union (distinct) = NJ, NY, CA, TX (18 rows), <db>_minus = NJ, CA (10 rows).
      // <db>_DA, <db>_DB = PRODUCTS.DESCRIPTION only, each side with 3 NULL rows: set operations treat NULLs as
      // equal and return distinct rows, so <db>_Dunion has one NULL row (5 rows) and <db>_Dminus has none (2 rows).
      // plan: both pushed to SQL on Oracle and PostgreSQL, only union on MySQL and SQL Server, neither on Derby
      // and Informix
      wstest.executeWS('1^2^__NULL__^DB/setOperation', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'fullOuterJoin' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database: <db>_PA = PRODUCTS where CATEGORY_ID one of 1, 3 (9 rows), <db>_PB = one of 1, 2 (9 rows).
      // <db>_joinNull: on DESCRIPTION; NULL never matches NULL, so products 1, 15, 16 stay unmatched (14 rows).
      // <db>_joinDup: on CATEGORY_ID, only the two CATEGORY_ID columns: 36 identical (1, 1) rows, 3 (3, NULL),
      // 3 (NULL, 2) - an emulation with UNION would drop the duplicates (42 rows).
      // <db>_joinCond: <db>_joinNull with PB's PRODUCT_ID is null, which must apply after the join (5 rows).
      // plan: pushed to SQL on Oracle, PostgreSQL and SQL Server; MySQL and Derby have no FULL OUTER JOIN, and
      // Informix runs these joins in memory too
      wstest.executeWS('1^2^__NULL__^DB/fullOuterJoin', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'statAggregate' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database: 16 picked ORDERS grouped by CUSTOMER_ID, aggregates on DISCOUNT: one value (1), all NULL (3),
      // NULL and one value (12), NULLs and a duplicate (7), even count with a constant EMPLOYEE_ID (19).
      // NULLs are ignored (Average of 7 divides by 5, Count of 3 is 0, its Sum and Average are NULL); sample
      // Variance and Standard Deviation of one value are 0 on every path (#80342), not NULL as in SQL;
      // Correlation of one pair or of a constant column is NULL.
      // Average of EMPLOYEE_ID: 9/7 for customer 7. DB2 (INTEGER column) returns 1 in DB2_basic: the SQL is a bare
      // AVG(col) and DB2 keeps the integer type (same root cause as the rejected #76921); DB2_mem gives 1.2857.
      // DB2 is in this worksheet, DB/dateGroup and DB/maxRows (its set operations and full join are not sampled yet).
      // Informix is in all five G11 worksheets; here it matches Oracle, MySQL, Derby, PostgreSQL and SQL Server.
      // <db>_basic, <db>_var, <db>_corr: aggregated in SQL (group by pushed); <db>_mem adds Median and Mode,
      // so all of its aggregates are computed in memory. A second aggregate of a column uses the expression
      // column DISCOUNT_<n> = field['DISCOUNT'], as the composer's aggregate dialog creates it.
      wstest.executeWS('1^2^__NULL__^DB/statAggregate', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'dateGroup' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database (DB2 and Informix included): one order on each of 15 dates around the year ends, grouped by WeekOfYear,
      // DayOfWeek, Week and Quarter of ORDER_DATE plus ORDER_ID. <db>_<option>NoSQL group a script expression copy
      // ORDER_DATE_JS = field['ORDER_DATE'] instead, so the date part is computed in memory on the same data.
      // DayOfWeek, Week and Quarter agree everywhere, in SQL and in memory, under the test servers' US session
      // settings: Oracle to_char(d,'D') follows NLS_TERRITORY and SQL Server datepart(dw) follows @@DATEFIRST
      // (GERMANY / DATEFIRST 1 make both Monday-based), while memory follows week.start (default Sunday).
      // WeekOfYear in memory is the same in every database: Sunday-based weeks, the week of January 1 is week 1,
      // so 2002-12-30, 2003-12-31 and 2004-12-27 are week 1. In SQL it differs from that on the same data (#82200).
      // The baselines keep today's SQL results: MySQL, SQL Server, DB2 give 53 for those three days; PostgreSQL
      // (ISO weeks) 53 for 2004-12-27 and 2005-01-01, 1 for 2005-01-04 and 2005-01-08; Oracle (WW, 7-day blocks
      // from January 1) is off by one for most dates after the first Sunday of the year. Informix pushes Quarter and
      // DayOfWeek but not Week and WeekOfYear, which it computes in memory like Derby.
      wstest.executeWS('1^2^__NULL__^DB/dateGroup', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'maxRows' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database: tables with a max row count, sorted against the physical order, exported at the top level and
      // inner joined with CUSTOMERS (<db>_<table>Join), where their SQL becomes a subquery wrapped by the row limit.
      // <db>_top: ORDER_ID descending, max 5 -> 12700..12696; <db>_distinct: distinct CUSTOMER_ID descending, max 5
      // -> 35..31; <db>_groupSort: Count(ORDER_ID) by CUSTOMER_ID sorted descending, max 3 -> customers 35, 34, 33;
      // <db>_groupMem: Count and Mode (never SQL) by CUSTOMER_ID, no sort, max 3 -> in memory, groups 1, 2, 3.
      // A grouped table with max rows and no sort is left out: its SQL has no ORDER BY, so the kept groups are
      // database-defined (known limit in #80446; Oracle, MySQL, PostgreSQL keep other groups than memory).
      // Informix has no SQL row limit: only its top-level tables are here. Its joins run in memory with both sides at
      // once on one variable table, and the row limit hint of one side reaches or is removed by the other (rows vary
      // from run to run, #82290), so <db>_<table>Join is left out for Informix.
      wstest.executeWS('1^2^__NULL__^DB/maxRows', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'nonEquiJoin' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database (DB2 and Informix included): <db>_L = PRODUCTS 4, 21, 13, 16 (PRICE 75, 40, 35, 29) with BIG =
      // PRICE cast to BIGINT, <db>_R = PRODUCTS 18, 22, 20 (PRICE 75, 40, 79) with HALF = PRICE + 0.5 and PSTR = PRICE
      // as a string.
      // Each >= join is pushed to SQL (<db>_<case>) and done in memory (<db>_<case>Mem: <db>_Lm, <db>_Rm have a script
      // column, so the join is a cross join filtered by SelfJoinTableLens); the plan files show the path. Informix
      // pushes no join here: its <db>_<case> tables run in memory too.
      // ge: PRICE >= PRICE keeps the ties, 3 rows everywhere.
      // bigHalf: BIG >= HALF is 1 row (75 >= 40.5) in SQL. In memory (and Informix_bigHalf) 3 rows except on Oracle
      // (BIG is a BigDecimal there): a Long against a decimal is compared as long, 75 >= 75.5 (#82263).
      // strNumMem: PRICE >= PSTR (number vs string) keeps all 12 pairs: different column types make the comparer ignore
      // the condition (#82264); compared as numbers it is 3 rows.
      // caseCmp: PRODUCT_NAME >= LOWER(PRODUCT_NAME): binary order in Oracle, DB2, Derby (0 rows), case-insensitive in
      // MySQL, SQL Server, PostgreSQL (its collation) and in memory, Informix included (6 rows), as documented for
      // #78887 / #79386.
      wstest.executeWS('1^2^__NULL__^DB/nonEquiJoin', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'stringCompare' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database (DB2 and Informix included), PRODUCTS with one string condition: <db>_<case> on PRODUCT_NAME
      // (pushed to SQL), <db>_<case>Mem on the script copy NAME_JS (in memory, same data).
      // eq = 'netstorage', sw starting with 'net', ct contains 'inch' ('17 Inch LCD', '19 inch LCD'), like '%lcd':
      // case-sensitive in Oracle, PostgreSQL, DB2, Derby, Informix, case-insensitive in MySQL, SQL Server and in memory
      // (string.compare.casesensitive=false) - documented behaviour (#78887, #79386).
      // padEq: PRODUCT_NAME plus a trailing space = 'NetStorage': MySQL, SQL Server, Derby, DB2, Informix ignore the
      // trailing space, Oracle, PostgreSQL and memory don't.
      wstest.executeWS('1^2^__NULL__^DB/stringCompare', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'stringOps' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database (DB2 and Informix included), PRODUCTS with one condition: <db>_<case> on the column (pushed to
      // SQL), <db>_<case>Mem on a script copy of it (in memory).
      // likeBs: like 'Green\ Planet' - PostgreSQL, MySQL and Informix read the backslash as an escape and return
      // product 4, the others and memory don't (#82271). likeBr: like 'Green [P]lanet' - a character class in SQL
      // Server only (#82271). swUnder: starting with 'Wireless_' matches nothing anywhere (the _ is escaped, #79610).
      // swNum (PRODUCT_ID starting with 2), ctPrice (PRICE contains .), swDate (PRODUCE_DATE starting with 2001):
      // pushed down, the value is converted to the column type first ('2' -> 2.0, '.' -> 0.0, '2001' -> today), so
      // Oracle and SQL Server return no row, MySQL / SQL Server / DB2 return the prices containing 0.0, and no database
      // finds the 2001 dates; memory compares as text: 2, 20-25 / all 25 / 10 rows (#82270). PostgreSQL, Derby and
      // Informix reject LIKE on a number or date, so only their memory tables are in the worksheet.
      // nullEmpty: EMPTY = '' for products 1-3 is null: Oracle (stores '' as NULL) and memory say 1-3, the other
      // databases none.
      wstest.executeWS('1^2^__NULL__^DB/stringOps', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'postCondition' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // per database (DB2 and Informix included): <db>_A = PRODUCTS of category 1 and 7, <db>_B = category 7 and 6
      // (CATEGORY_ID, PRICE), UNION, grouped by CATEGORY_ID with Sum(PRICE) between 3000 and 5000 as the post
      // condition. The union removes duplicate rows also inside one side (two products of category 7 cost 40), so
      // category 7 is 40 + 210 + 310 + 2850 = 3410 (6900 without the duplicates removed): only category 7 is kept.
      // <db>_union is pushed down (HAVING) where the database takes the UNION (all but Derby and Informix),
      // <db>_unionMem runs in memory (script column CAT_JS in both sides).
      wstest.executeWS('1^2^__NULL__^DB/postCondition', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'dateInRolling' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // date in on a table without changing the system date: products 1-11 get a date OFFSET days before today
      // (script column, hidden; OFFSET -1, 0, 1, 7, 8, 14, 15, 30, 31, 60, 61), and day-based ranges are applied in
      // memory, so the kept OFFSETs do not depend on the day of the run: Today 0, Yesterday 1, Tomorrow -1,
      // Last 7 days 0, 1, 7, Last 8-14 days 8, 14, Last 30 days 0-30, Last 31-60 days 31, 60.
      wstest.executeWS('1^2^__NULL__^DB/dateInRolling', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'clickhouseSamples' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportMergePlan = true
      // ClickHouse (no ORDERS / PRODUCTS there): the G8 / G11 cases that differ on it, on default.base_type (20 rows),
      // each pushed to ClickHouse and in memory (<table>Mem, a script copy or a script column).
      // Case: upper(string_col) = / starting with / contains / like a lower-case value: ClickHouse none, memory id 1.
      // CH_swUnder (starting with 'hel_') and CH_ctPercent (contains 'l%o'): ClickHouse has no ESCAPE, so _ and % stay
      // wildcards and return hello; memory compares literally and returns nothing.
      // CH_padEq: trailing space kept everywhere. CH_nullEmpty: a String is never NULL in ClickHouse ('' is not null),
      // memory treats '' as null (ids 1-3).
      // >= joins: ge 5 rows both; bigHalf 4 pushed, 5 in memory (#82263); strNumMem keeps all 9 pairs (#82264);
      // caseCmp: binary order in ClickHouse (9 rows), case-insensitive in memory (1).
      // CH_fullJoin: ClickHouse fills the side without a match with default values (join_use_nulls = 0): id 0 where
      // memory (CH_fullJoinMem) has NULL.
      // CH_minus = 1.732 pushed and in memory. Statistics by boolean_col match an independent computation; dates are
      // not grouped in SQL on ClickHouse. CH_top / CH_distinct keep 5 / 3 rows.
      // Not in the worksheet: CH_union (the generated UNION without ALL / DISTINCT fails on ClickHouse 22.1), CH_corr
      // (covariance SQL adds Float64 and Decimal, which ClickHouse rejects), CH_topJoin (#82290, 20 rows instead of 5).
      wstest.executeWS('1^2^__NULL__^DB/clickhouseSamples', null, null)

      expect:
      wstest.compareData(null)
   }

   @Ignore
   def 'clickhouse' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^DB/clickhouse', null, null)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/clickhouse',null)

      expect:
      wstest.compareData(null)
   }

}

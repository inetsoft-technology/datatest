package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import inetsoft.util.script.ScriptException
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE for the port-gap sweep (#81430): script bugs that were fixed on the 13.8 line, re-tested here. It only
 * prints "PROBE BP <issue> <expression> => <result> | expected <expected> | MATCH or DIFF" and asserts nothing.
 * Not a case (no _Spec suffix); what it finds is turned into cases in state/.
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateBackportProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateBackportProbe extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   /** [issue, expression, expected as the string the result should print as, or null when only printing] */
   static final List CASES = [
      [79672, "rtrim('')", ''],
      [79672, "rtrim('   ')", ''],
      [79672, "rtrim('ab  ')", 'ab'],
      [79674, "split('a-b--c', '--').join('|')", 'a-b|c'],
      [79674, "split('a----b', '--').join('|')", 'a||b'],
      [79675, "CALC.binomdist(2, 5, 0.3, false)", '0.3087'],
      [79675, "CALC.binomdist(2, 5, 0.3, true)", '0.83692'],
      [79676, "formatDate(CALC.edate(parseDate('2011-01-15', 'yyyy-MM-dd'), -1), 'yyyy-MM-dd')", '2010-12-15'],
      [79676, "formatDate(CALC.edate(parseDate('2011-12-31', 'yyyy-MM-dd'), 2), 'yyyy-MM-dd')", '2012-02-29'],
      [79676, "formatDate(CALC.edate(parseDate('2011-11-30', 'yyyy-MM-dd'), 3), 'yyyy-MM-dd')", '2012-02-29'],
      [79677, "CALC.days360(parseDate('2011-01-01', 'yyyy-MM-dd'), parseDate('2011-12-31', 'yyyy-MM-dd'))", '360'],
      [79677, "CALC.days360(parseDate('2020-01-15', 'yyyy-MM-dd'), parseDate('2020-03-31', 'yyyy-MM-dd'))", '76'],
      [79677, "CALC.days360(parseDate('2020-01-15', 'yyyy-MM-dd'), parseDate('2020-03-31', 'yyyy-MM-dd'), true)", '75'],
      [79678, "CALC.factdouble(0)", '1'],
      [79678, "CALC.mdeterm([[0.5, 0], [0, 0.5]])", '0.25'],
      [79679, "CALC.mround(1.3, 0.2)", '1.4'],
      [79680, "CALC.countblank([1, null, '', 3])", '2'],
      [79682, "CALC.db(1000000, 100000, 6, 7, 7)", '15845.10'],
      [79683, "('nosuchfunction' in CALC)", 'false'],
      [79685, "StyleConstant['MAP_TYPE_U.S.']", null],
      [79685, "Chart['MAP_TYPE_U.S.']", null],
      [81011, "union([1, 2, 3], [3, 4]).length", '4'],
      [81233, "formatNumber(0.1234, '#,###.00%', 'ROUND_HALF_UP')", '12.34%'],
      [81233, "formatNumber(0.1234, '#,###.00%')", '12.34%'],
   ]

   static String run(ScriptStateBase t, String expr) {
      try {
         def v = JavaScriptEngine.unwrap(t.scope.execute(expr, 'Chart1'))
         return v == null ? 'null' : v.toString()
      }
      catch(ScriptException e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) { c = c.cause }
         return "EX ${c.class.simpleName}: ${c.message}".toString()
      }
   }

   def "port-gap sweep: script functions"() {
      expect:
      def t = new ScriptStateBase(VS)
      CASES.each { c ->
         def got = run(t, c[1])
         // numbers print as doubles ("360.0"), so compare numerically when both sides are numbers
         def same = got == c[2] ||
            (c[2] != null && got.isNumber() && c[2].isNumber() && Math.abs(got.toDouble() - c[2].toDouble()) < 1e-9)
         def verdict = c[2] == null ? 'PRINT' : (same ? 'MATCH' : 'DIFF')
         println "PROBE BP ${c[0]} ${c[1]} => ${got} | expected ${c[2]} | ${verdict}"
      }
      true
   }
}

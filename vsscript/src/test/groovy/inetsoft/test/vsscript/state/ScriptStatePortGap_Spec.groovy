package inetsoft.test.vsscript.state

import inetsoft.report.CellBinding
import inetsoft.report.TableCellBinding
import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import org.springframework.test.context.ContextConfiguration
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * Port-gap sweep (#81430): script bugs that were reported and fixed on the 13.8 line (Redmine version 13.8), re-tested
 * on StyleBI. Two kinds of case:
 * <ul>
 *   <li>Regression: the 13.8 bug does not occur on StyleBI. A plain case with the correct value (#79672, #79676,
 *       #79677, #79678 factdouble, #79683, #79685, #81011, #81233).</li>
 *   <li>Still open on StyleBI: the three-case pattern of ScriptStateKnownBugs_Spec - a control, a "[bug present]" case
 *       that pins today's wrong value (it turns red when the bug is fixed), and a @PendingFeature case that asserts the
 *       expected value (it "passes unexpectedly", i.e. fails, once the bug is fixed; then drop the annotation and the
 *       "[bug present]" case). CALC.binomdist, mdeterm, mround, countblank: #81609 (13.8: #79675, #79678, #79679,
 *       #79680). Freehand table: #81610 (13.8: #79806).</li>
 * </ul>
 * Checked and not reproduced as bugs (no case): popLocation script (#63022, #63103), TextInput defaultText from script
 * does not change the design value (#63079), TextInput.value == null on load is true (#72042). TextInput has no
 * placeholderText property on StyleBI at all (#63510, #63079: the 13.8 feature #59271 does not exist here).
 * Not testable with the assets we have: #79699 (needs a dashboard embedded two levels deep; no asset embeds a viewsheet).
 * Scripts run through scope.execute because the refresh path swallows script errors.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStatePortGap_Spec extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'
   static final String FREEHAND_VS = '1^128^__NULL__^ScriptAuto/SpecialScript/FreehandFormula'

   /** any assembly script scope works for the global functions; the chart of this asset is used */
   static Object run(ScriptStateBase t, String script, String assembly = 'Chart1') {
      JavaScriptEngine.unwrap(t.scope.execute(script, assembly))
   }

   static boolean near(Object actual, double expected) {
      actual instanceof Number && Math.abs(((Number) actual).doubleValue() - expected) < 1e-9
   }

   static final String DATE = "parseDate('%s', 'yyyy-MM-dd')"

   static String d(String s) { String.format(DATE, s) }

   // ---- fixed on StyleBI: regressions of 13.8 bugs ----

   def '#79672 rtrim of an empty or blank string is an empty string'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, script) == expected

      where:
      script          | expected
      "rtrim('')"     | ''
      "rtrim('   ')"  | ''
      "rtrim('ab  ')" | 'ab'
   }

   def '#79676 CALC.edate keeps the day across a year boundary and clamps to the month end'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, "formatDate(CALC.edate(${d(from)}, ${months}), 'yyyy-MM-dd')") == expected

      where:
      from         | months | expected
      '2011-01-15' | -1     | '2010-12-15'
      '2011-12-31' | 2      | '2012-02-29'
      '2011-11-30' | 3      | '2012-02-29'
   }

   def '#79677 CALC.days360 applies the US rule by default and the European rule when asked'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, "CALC.days360(${d(from)}, ${d(to)}${extra})"), expected)

      where:
      from         | to           | extra  | expected
      '2011-01-01' | '2011-12-31' | ''     | 360d
      '2020-01-15' | '2020-03-31' | ''     | 76d
      '2020-01-15' | '2020-03-31' | ', true' | 75d
   }

   def '#79678 CALC.factdouble(0) is 1'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.factdouble(0)'), 1d)
   }

   def "#79683 an unknown function name is not 'in' CALC"() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, "('nosuchfunction' in CALC)") == false
   }

   def '#79685 StyleConstant carries the MAP_TYPE constants like Chart'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, "StyleConstant['MAP_TYPE_U.S.']") == 'U.S.'
      run(t, "Chart['MAP_TYPE_U.S.']") == 'U.S.'
   }

   def '#81011 union() of two arrays keeps one copy of a number in both'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'union([1, 2, 3], [3, 4]).length'), 4d)
   }

   def '#81233 formatNumber with a percent pattern rounds the right digit, with and without a rounding mode'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, script) == '12.34%'

      where:
      script << ["formatNumber(0.1234, '#,###.00%', 'ROUND_HALF_UP')", "formatNumber(0.1234, '#,###.00%')"]
   }

   // ---- #81609: CALC.binomdist (13.8: #79675) ----
   def 'control: CALC.fact and CALC.combin are right'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.fact(5)'), 120d)
      near(run(t, 'CALC.combin(5, 2)'), 10d)
   }

   def '#81609 [bug present] CALC.binomdist uses the success probability for the failure term'() {
      given:
      def t = new ScriptStateBase(VS)

      expect: 'C(5,2) * 0.3^2 * 0.3^3 and its cumulative sum, instead of 0.3087 and 0.83692'
      near(run(t, 'CALC.binomdist(2, 5, 0.3, false)'), 0.0243d)
      near(run(t, 'CALC.binomdist(2, 5, 0.3, true)'), 0.03888d)
   }

   @PendingFeature(reason = '#81609: CALC.binomdist(2, 5, 0.3, false) should be 0.3087 and cumulative 0.83692')
   def '#81609 CALC.binomdist is C(n,k) p^k (1-p)^(n-k)'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.binomdist(2, 5, 0.3, false)'), 0.3087d)
      near(run(t, 'CALC.binomdist(2, 5, 0.3, true)'), 0.83692d)
   }

   // ---- #81609: CALC.mdeterm (13.8: #79678, second part) ----
   def 'control: CALC.mdeterm of an integer matrix is right'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.mdeterm([[1, 2], [3, 4]])'), -2d)
   }

   def '#81609 [bug present] CALC.mdeterm truncates a fractional determinant to an int'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.mdeterm([[0.5, 0], [0, 0.5]])'), 0d)
   }

   @PendingFeature(reason = '#81609: CALC.mdeterm([[0.5,0],[0,0.5]]) should be 0.25')
   def '#81609 CALC.mdeterm keeps the fractional part'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.mdeterm([[0.5, 0], [0, 0.5]])'), 0.25d)
   }

   // ---- #81609: CALC.mround (13.8: #79679) ----
   def 'control: CALC.mround of a value that is not on a half is right'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.mround(10, 3)'), 9d)
      near(run(t, 'CALC.mround(1.5, 1)'), 2d)
   }

   def '#81609 [bug present] CALC.mround(1.3, 0.2) rounds down and carries rounding noise'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'CALC.mround(1.3, 0.2)') == 1.2000000000000002d
   }

   @PendingFeature(reason = '#81609: CALC.mround(1.3, 0.2) should be 1.4 (half rounds away from zero)')
   def '#81609 CALC.mround rounds a half away from zero'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.mround(1.3, 0.2)'), 1.4d)
   }

   // ---- #81609: CALC.countblank (13.8: #79680) ----
   def 'control: CALC.countblank counts null'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, 'CALC.countblank([1, null, 3])'), 1d)
   }

   def '#81609 [bug present] CALC.countblank does not count an empty string'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, "CALC.countblank([1, null, '', 3])"), 1d)
   }

   @PendingFeature(reason = "#81609: CALC.countblank([1, null, '', 3]) should be 2")
   def '#81609 CALC.countblank counts null and empty string'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      near(run(t, "CALC.countblank([1, null, '', 3])"), 2d)
   }

   // ---- #79806 (13.8): a freehand formula cell $amt does not read a cell the table script wrote ----
   /**
    * Freehand1 of the asset gets: the original Category group and Discount summary, a cell named amt in column 2 and a
    * formula cell "$amt * 2" in column 3. Variant "column": amt is a summary column (the reference works). Variant
    * "text": amt is a text cell, optionally written by the table script table[r][2] = r + 1.
    * Returns [c2, c3] of row 0 as the script sees them.
    */
   static List freehandAmt(String variant, boolean script) {
      def t = new ScriptStateBase(FREEHAND_VS)
      def a = t.vs.getAssembly('Freehand1')
      def layout = a.getTableLayout()
      layout.setColCount(4)
      def amt
      if(variant == 'column') {
         amt = TableCellBinding.getSummaryBinding('Discount')
         amt.setFormula('Max')
      }
      else {
         amt = new TableCellBinding(CellBinding.BIND_TEXT, 'x')
      }
      amt.setCellName('amt')
      layout.setCellBinding(0, 2, amt)
      layout.setCellBinding(0, 3, new TableCellBinding(CellBinding.BIND_FORMULA, '$amt * 2'))
      if(script) {
         a.getVSAssemblyInfo().setScript('for(var r = 0; r < 12; r++) { table[r][2] = r + 1; }')
      }
      t.res.refreshViewsheet(t.principal)
      [run(t, 'Freehand1.table[0][2]', 'Freehand1'), run(t, 'Freehand1.table[0][3]', 'Freehand1')]
   }

   def 'control: a $ reference to a bound summary cell works in a freehand formula'() {
      when:
      def r = freehandAmt('column', false)

      then: 'amt is the maximum discount (0.25) and the formula doubles it'
      near(r[0], 0.25d)
      near(r[1], 0.5d)
   }

   def '#79806 [bug present] the formula of a freehand table does not read the value the table script wrote'() {
      when:
      def r = freehandAmt('text', true)

      then: 'the script wrote 1 into the cell, but the formula still sees the bound text and has no number'
      near(r[0], 1d)
      r[1] == null
   }

   @PendingFeature(reason = '#79806: $amt * 2 should read the scripted 1 and give 2')
   def '#79806 the formula of a freehand table reads the value the table script wrote'() {
      when:
      def r = freehandAmt('text', true)

      then:
      near(r[0], 1d)
      near(r[1], 2d)
   }
}

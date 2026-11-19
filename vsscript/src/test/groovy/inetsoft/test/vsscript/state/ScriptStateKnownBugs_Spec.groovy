package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Regressions of fixed Chart and Crosstab script bugs on the real chart and crosstab assets: #80708 / #80709 and the
 * Crosstab getters (fixed by #81530, PR #8301) and #80711 (fixed by #81032, PR #8004). The #81530 cases need an
 * inetsoft-core that contains community f0aad94be6; on an older jar they fail.
 *
 * The cases of the other bugs found by the same sweep were removed on 2026-11-18, when their fixes were merged with Java
 * tests (placement rule of vsscript-upgrade-plan.md section 1a): #81531 (VSChartBindingScriptableTest), #81534
 * (ChartConstantsScopeTest), #81540 (AxisScriptableTest) and #81550 (RangeOutputVSAssemblyInfoTest). The pattern they
 * used, for a bug that is still open and that only a real viewsheet can show: a control on the working path, a
 * "[bug present]" case that asserts the exact failure seen today (it turns red when the bug is fixed), and a
 * @PendingFeature case with the expected behaviour.
 *
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
class ScriptStateKnownBugs_Spec extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   static final String CROSSTAB_VS = '1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1'
   static final String CROSSTAB = 'NullCorsstab'   // sic: the assembly name in the asset

   static Object run(ScriptStateBase t, String script, String assembly = 'Chart1') {
      JavaScriptEngine.unwrap(t.scope.execute(script, assembly))
   }

   /** the date level of the first x field as the chart info holds it (null = not set) */
   static String dateLevel(ScriptStateBase t) {
      t.vs.getAssembly('Chart1').getVSChartInfo().getXFields()[0].getDateLevelValue()
   }

   static final String TOPN_REVERSE = 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)'
   static final String GET_GROUP_ORDER = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)'
   static final String SET_NONE = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.NONE_DATE_GROUP, Chart.BINDING_FIELD)'

   // ---- #80709: bindingInfo.isTopNReverse() threw NPE for a dimension with no ranking ----
   // Fixed in stylebi by #81530 (PR #8301, community f0aad94be6). These cases are now regressions and need an
   // inetsoft-core that contains that commit; on an older jar they fail (isTopNReverse throws).
   def 'control: getTopN of an unranked dimension is 0'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.getTopN("State", Chart.BINDING_FIELD)') == 0d
   }

   def '#80709 isTopNReverse of an unranked dimension is false'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, TOPN_REVERSE) == false
   }

   // ---- #80708: bindingInfo.getGroupOrder() threw "Cannot parse null string" before setGroupOrder ----
   // Fixed in stylebi by #81530 (PR #8301); regression, needs an inetsoft-core that contains f0aad94be6.
   def 'control: getGroupOrder works once a date level is set'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD); ' +
             'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)') == 5d
   }

   def '#80708 getGroupOrder of a date field returns the default YEAR level'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, GET_GROUP_ORDER) == 5d
   }

   // ---- #80711: setGroupOrder(field, Chart.NONE_DATE_GROUP) is silently ignored ----
   def 'control: setGroupOrder YEAR and MONTH take effect'() {
      given:
      def a = new ScriptStateBase(VS)
      def b = new ScriptStateBase(VS)

      when:
      run(a, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD)')
      run(b, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.MONTH_DATE_GROUP, Chart.BINDING_FIELD)')

      then:
      dateLevel(a) == '5'
      dateLevel(b) == '3'
   }

   // Fixed in stylebi by #81032 (PR #8004, community 65dc2f1c6b); the [bug present] case went red on a build that
   // contains it and the pending case passed unexpectedly, so both were replaced by this regression case.
   def '#80711 setGroupOrder NONE_DATE_GROUP sets the date level to 0'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      run(t, SET_NONE)

      then:
      dateLevel(t) == '0'
   }

   // ---- Crosstab siblings of #80708/#80709, and #78585 item 2 (chart type 0) ----
   // #80708/#80709 were fixed on the 13.8 line only; filed for stylebi as #81530 (the throwing getters; crosstab getTopN
   // had no issue at all), reproduced live in the Composer on 2.1.202611170332, and fixed in stylebi the same day (PR #8301,
   // community f0aad94be6): the crosstab getters and the two chart cases above are regressions (they need an inetsoft-core
   // that contains that commit).
   static final String CT_SETUP = 'bindingInfo.rowFields = ["state"]; bindingInfo.colFields = ["orderdate"]; bindingInfo.measureFields = ["customer_id"]; '
   static final String CT_TOPN = CT_SETUP + 'bindingInfo.getTopN("state", StyleConstant.ROW_HEADER)'
   static final String CT_TOPN_REVERSE = CT_SETUP + 'bindingInfo.isTopNReverse("state", StyleConstant.ROW_HEADER)'
   static final String CT_GROUP_ORDER = CT_SETUP + 'bindingInfo.getGroupOrder("orderdate", StyleConstant.COL_HEADER)'

   def 'control: crosstab getTopN and isTopNReverse work once ranking is set'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)
      def set = CT_SETUP + 'bindingInfo.setTopN("state", StyleConstant.ROW_HEADER, 3); bindingInfo.setTopNReverse("state", StyleConstant.ROW_HEADER, true); '

      expect:
      run(t, set + 'bindingInfo.getTopN("state", StyleConstant.ROW_HEADER)', CROSSTAB) == 3d
      run(t, set + 'bindingInfo.isTopNReverse("state", StyleConstant.ROW_HEADER)', CROSSTAB) == true
   }

   def 'crosstab getTopN of an unranked field is 0'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_TOPN, CROSSTAB) == 0d
   }

   def 'crosstab isTopNReverse of an unranked field is false'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_TOPN_REVERSE, CROSSTAB) == false
   }

   def 'crosstab getGroupOrder of a date field with no level set returns the default YEAR level'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_GROUP_ORDER, CROSSTAB) == 5d
   }
}

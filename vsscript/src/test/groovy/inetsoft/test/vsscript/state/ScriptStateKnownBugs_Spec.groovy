package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import inetsoft.util.script.ScriptException
import org.springframework.test.context.ContextConfiguration
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * Regression cases for Redmine chart-script bugs whose repro still fails on the current product build.
 * Each bug has three cases:
 * <ul>
 *   <li>a control on the working path of the same call, so a failure cannot come from a wrong setup;</li>
 *   <li>a "[bug present]" case that asserts the exact failure signature seen today. It turns RED when the
 *       bug is fixed (or when the failure changes to something else, e.g. a broken VS id or script API) —
 *       that is the signal to delete it and un-pend the next case;</li>
 *   <li>a @PendingFeature case with the expected behavior. A pending case passes on ANY failure, so it
 *       alone cannot tell "bug present" from "setup broken"; the two cases above cover that.</li>
 * </ul>
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

   static Object run(ScriptStateBase t, String script) {
      JavaScriptEngine.unwrap(t.scope.execute(script, 'Chart1'))
   }

   /** run a script that is expected to fail; return the innermost message, or null if it did not fail */
   static String failureOf(ScriptStateBase t, String script) {
      try {
         t.scope.execute(script, 'Chart1')
         return null
      }
      catch(ScriptException e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) c = c.cause
         return c.message
      }
   }

   /** the date level of the first x field as the chart info holds it (null = not set) */
   static String dateLevel(ScriptStateBase t) {
      t.vs.getAssembly('Chart1').getVSChartInfo().getXFields()[0].getDateLevelValue()
   }

   static final String TOPN_REVERSE = 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)'
   static final String GET_GROUP_ORDER = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)'
   static final String SET_NONE = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.NONE_DATE_GROUP, Chart.BINDING_FIELD)'

   // ---- #80709: bindingInfo.isTopNReverse() throws NPE for a dimension with no ranking ----
   def 'control: getTopN of an unranked dimension is 0'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.getTopN("State", Chart.BINDING_FIELD)') == 0d
   }

   def '#80709 [bug present] isTopNReverse fails with "Failed to invoke script function"'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      failureOf(t, TOPN_REVERSE) == 'Failed to invoke script function: isTopNReverse'
   }

   @PendingFeature(reason = 'Redmine #80709: isTopNReverse throws NullPointerException (VSChartBindingScriptable.isTopNReverse, getRankingOptionValue() is null)')
   def '#80709 isTopNReverse of an unranked dimension is false'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, TOPN_REVERSE) == false
   }

   // ---- #80708: bindingInfo.getGroupOrder() throws "Cannot parse null string" before setGroupOrder ----
   def 'control: getGroupOrder works once a date level is set'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD); ' +
             'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)') == 5d
   }

   def '#80708 [bug present] getGroupOrder fails with "Failed to invoke script function"'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      failureOf(t, GET_GROUP_ORDER) == 'Failed to invoke script function: getGroupOrder'
   }

   @PendingFeature(reason = 'Redmine #80708: getGroupOrder throws for a date x field until setGroupOrder is called (Integer.parseInt of a null date level)')
   def '#80708 getGroupOrder of a date field returns a level'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, GET_GROUP_ORDER) instanceof Number
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

   def '#80711 [bug present] NONE_DATE_GROUP leaves the date level unset (null)'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      run(t, SET_NONE)

      then:
      dateLevel(t) == null
   }

   @PendingFeature(reason = 'Redmine #80711: setGroupOrder with NONE_DATE_GROUP is ignored (unsorted attributes array + Arrays.binarySearch in AbstractChartBindingScriptable)')
   def '#80711 setGroupOrder NONE_DATE_GROUP sets the date level to 0'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      run(t, SET_NONE)

      then:
      dateLevel(t) == '0'
   }
}

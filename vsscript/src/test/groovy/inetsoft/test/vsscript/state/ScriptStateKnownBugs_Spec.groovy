package inetsoft.test.vsscript.state

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
 * Regression cases for Redmine chart-script bugs whose repro still fails on the current product build.
 *
 * Each bug case is a @PendingFeature: it passes while the bug exists (the body fails) and FAILS once the
 * bug is fixed, which is the signal to remove the annotation. Each is paired with a control on the working
 * path, so a bug case cannot fail because the setup is wrong.
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

   /** the date level of the first x field as the chart info holds it (null = not set) */
   static String dateLevel(ScriptStateBase t) {
      t.vs.getAssembly('Chart1').getVSChartInfo().getXFields()[0].getDateLevelValue()
   }

   // ---- #80709: bindingInfo.isTopNReverse() throws NPE for a dimension with no ranking ----
   def 'control: getTopN of an unranked dimension is 0'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.getTopN("State", Chart.BINDING_FIELD)') == 0d
   }

   @PendingFeature(reason = 'Redmine #80709: isTopNReverse throws NullPointerException (VSChartBindingScriptable.isTopNReverse, getRankingOptionValue() is null)')
   def '#80709 isTopNReverse of an unranked dimension is false'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)') == false
   }

   // ---- #80708: bindingInfo.getGroupOrder() throws "Cannot parse null string" before setGroupOrder ----
   @PendingFeature(reason = 'Redmine #80708: getGroupOrder throws for a date x field until setGroupOrder is called (Integer.parseInt of a null date level)')
   def '#80708 getGroupOrder of a date field returns a level'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)') instanceof Number
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

   @PendingFeature(reason = 'Redmine #80711: setGroupOrder with NONE_DATE_GROUP is ignored (unsorted attributes array + Arrays.binarySearch in AbstractChartBindingScriptable)')
   def '#80711 setGroupOrder NONE_DATE_GROUP sets the date level to 0'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      run(t, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.NONE_DATE_GROUP, Chart.BINDING_FIELD)')

      then:
      dateLevel(t) == '0'
   }
}

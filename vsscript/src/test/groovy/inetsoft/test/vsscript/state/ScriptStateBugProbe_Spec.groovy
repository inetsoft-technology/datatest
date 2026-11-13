package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Exploration: run the repro scripts of recently closed Redmine chart-script bugs directly
 * (scope.execute) against the current product build and print what happens.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateBugProbe_Spec extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   static void probe(String msg) { println "PROBE ${msg}" }

   static String desc(Throwable e) {
      if(e == null) return 'none'
      def c = e
      while(c.cause != null && c.cause != c) c = c.cause
      "${e.class.simpleName}: ${e.message?.take(80)?.replace('\n', ' ')} | root=${c.class.simpleName}: ${c.message?.take(120)?.replace('\n', ' ')}"
   }

   /** run a script in Chart1's scope; print result or exception */
   def run(String label, String script) {
      def t = new ScriptStateBase(VS)
      try {
         def r = t.scope.execute(script, 'Chart1')
         probe "${label}: result=${inetsoft.util.script.JavaScriptEngine.unwrap(r)}"
      }
      catch(Throwable e) { probe "${label}: THROWN ${desc(e)}" }
   }

   def 'B80709_isTopNReverse_without_ranking'() {
      when:
      run('80709', 'bindingInfo.xFields = [["State", Chart.STRING]];\n' +
                   'bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)')
      run('80709 control getTopN', 'bindingInfo.xFields = [["State", Chart.STRING]];\n' +
                   'bindingInfo.getTopN("State", Chart.BINDING_FIELD)')

      then:
      true
   }

   def 'B80708_getGroupOrder_without_set'() {
      when:
      run('80708', 'bindingInfo.xFields = [["Date", Chart.DATE]];\n' +
                   'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)')

      then:
      true
   }

   def 'B80711_setGroupOrder_none'() {
      when:
      run('80711 after set NONE', 'bindingInfo.xFields = [["Date", Chart.DATE]];\n' +
                   'bindingInfo.setGroupOrder("Date", Chart.NONE_DATE_GROUP, Chart.BINDING_FIELD);\n' +
                   'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)')
      run('80711 control set YEAR', 'bindingInfo.xFields = [["Date", Chart.DATE]];\n' +
                   'bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD);\n' +
                   'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)')

      then:
      true
   }

   def 'B80711_read_date_level_from_chart_info'() {
      when:
      ['NONE_DATE_GROUP', 'YEAR_DATE_GROUP', 'MONTH_DATE_GROUP'].each { c ->
         def t = new ScriptStateBase(VS)
         try {
            t.scope.execute('bindingInfo.xFields = [["Date", Chart.DATE]]; ' +
               "bindingInfo.setGroupOrder('Date', Chart.${c}, Chart.BINDING_FIELD)", 'Chart1')
            def info = t.vs.getAssembly('Chart1').getVSChartInfo()
            def x = info.getXFields()
            def rt = null
            try { rt = t.vs.getAssembly('Chart1').getVSChartInfo().getRTXFields()*.getDateLevelValue() } catch(Throwable e) { rt = 'n/a' }
            probe "80711 ${c}: design xFields dateLevelValue=${x*.getDateLevelValue()} rt=${rt}"
         }
         catch(Throwable e) { probe "80711 ${c}: THROWN ${desc(e)}" }
      }

      then:
      true
   }
}

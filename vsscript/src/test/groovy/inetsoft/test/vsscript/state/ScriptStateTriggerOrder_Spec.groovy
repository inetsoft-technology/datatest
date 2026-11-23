package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * D5, when the viewsheet scripts run (claude/viewsheet-sandbox.md: processOnInit once per lifetime, processOnLoad on every reset). The scripts are put on
 * a real asset in memory, so no new asset is needed. (The viewsheet's "onRefresh" script of the Composer is the onLoad script: ViewsheetInfo has only onInit
 * and onLoad.)
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateTriggerOrder_Spec extends Specification {
   static final String VS = '1^128^__NULL__^Manual/Chart/TestCase_DrillEnabled'

   static String count(String v) {
      "if(typeof ${v} === 'undefined') { var ${v} = 0; } ${v} = ${v} + 1;".toString()
   }

   static String read(ScriptStateBase t, String assembly, String key) {
      def v = JavaScriptEngine.unwrap(t.scope.execute("${assembly}['${key}']".toString(), assembly))
      v == null ? 'null' : "${v}".toString()
   }

   def 'the_viewsheet_onInit_runs_once_and_the_onLoad_on_every_refresh'() {
      given:
      def t = new ScriptStateBase(VS)
      t.vs.getViewsheetInfo().setOnInit(count('initCount'))
      t.vs.getViewsheetInfo().setOnLoad(count('loadCount'))

      when:
      def seen = (1..4).collect {
         t.res.refreshViewsheet(t.principal)
         [t.eval('initCount') as int, t.eval('loadCount') as int]
      }

      then: 'onInit variables survive the refreshes and it ran once; the onLoad ran once more with every refresh'
      seen == [[1, 1], [1, 2], [1, 3], [1, 4]]
   }

   def 'a_value_written_by_the_viewsheet_onInit_is_back_at_the_design_value_after_the_next_refresh'() {
      // the documented pitfall (viewsheet-sandbox.md "Critical pitfall"): resetRuntimeValues() clears the runtime value of a property that has a design value
      // / run value pair before every refresh, and onInit does not run again, so only the first refresh keeps what onInit wrote. onLoad runs every time.
      // Since #81463 / #81835 this holds for popAlpha and the tip properties too. If development changes the rule (for example by re-running onInit after
      // a reset), change this case with it.
      given:
      def t = new ScriptStateBase(VS)
      def designVisible = read(t, 'Text1', 'visible')
      def designText = read(t, 'Text1', 'text')
      def designAlpha = read(t, 'Text1', 'popAlpha')
      t.vs.getViewsheetInfo().setOnInit("Text1.visible = false; Text1.text = 'from onInit'; Text1.popAlpha = 55;")

      when:
      t.res.refreshViewsheet(t.principal)
      def first = [read(t, 'Text1', 'visible'), read(t, 'Text1', 'text'), read(t, 'Text1', 'popAlpha')]
      t.res.refreshViewsheet(t.principal)
      def second = [read(t, 'Text1', 'visible'), read(t, 'Text1', 'text'), read(t, 'Text1', 'popAlpha')]

      then: 'the first refresh has what onInit wrote'
      first[0] == 'false'
      first[1] == 'from onInit'
      first[2] as double == 55d

      and: 'the next one is back at the design value'
      second[0] == designVisible
      second[1] == designText
      second[2] as double == designAlpha as double
   }

   def 'a_value_written_by_the_viewsheet_onLoad_is_there_after_every_refresh'() {
      given:
      def t = new ScriptStateBase(VS)
      t.vs.getViewsheetInfo().setOnLoad("Text1.visible = false; Text1.text = 'from onLoad';")

      when:
      def seen = (1..3).collect {
         t.res.refreshViewsheet(t.principal)
         [read(t, 'Text1', 'visible'), read(t, 'Text1', 'text')]
      }

      then:
      seen.every { it == ['false', 'from onLoad'] }
   }
}

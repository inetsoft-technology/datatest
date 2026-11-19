package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.LogRecorder
import inetsoft.test.vsscript.support.RecordingDispatcher
import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.Tool
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Swallowed-error tripwire (#81430). The refresh path swallows script errors (ViewsheetSandbox.updateAssembly ignores
 * a ScriptException, VSAScriptable.putMember only logs a failed assignment), so a PNG baseline can pass on a broken
 * script. This case opens every viewsheet of the test storage through the real service, refreshes it through the real
 * refresh path, and records three things: the commands the client would get (MessageCommand), the thread's user message
 * (Tool.getUserMessage) and every WARN / ERROR log event. It fails on anything that is not in the lists below, and on a
 * listed entry that no longer occurs (so the lists can only shrink; remove an entry when its cause is fixed).
 *
 * Why it lives in datatest and not in a Java unit test: it needs the real viewsheets of the storage and the whole open
 * and refresh flow. The log events are readable only because LogRecorder (bound by src/test/groovy/org/slf4j/impl/
 * StaticLoggerBinder) records them; without a SLF4J binding every log line of the product is dropped (NOPLoggerFactory).
 *
 * Event key: "MSG <type>: <text>" for a command, "USER: <text>" for the user message, "<LEVEL> <logger>: <text>" for a
 * log event; text is the first line, with object hash codes removed, cut at 90 characters.
 * The "Failed to flush pending tasks" ERROR of CommandDispatcher comes from the mocked messaging of the harness and is
 * ignored.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateRefreshTripwire_Spec extends Specification {
   static final String PFX = '1^128^__NULL__^'

   /** events a viewsheet is known to produce on every refresh, by viewsheet without the prefix; see the reason in the comment */
   static final Map<String, List<String>> EXPECTED = [
      // the script reads pviewsheet, which only exists when the viewsheet is embedded in another one (false positive, also seen in the D1 check)
      'Manual/LinkedAsseted/lVs': [
         'MSG INFO: Script execution error in assembly: thisViewsheet',
         'WARN ViewsheetSandbox: Failed to execute viewsheet script: Script execution error in assembly: thisViewsheet',
         'WARN ViewsheetScope: Script execution error: TypeError: invokeMember (forEach) on inetsoft.util.script.graal.Sc'
      ],
      // the column binding expression =Text1.value returns a value that is not a column name (asset setup, left as it is)
      'Manual/OtherScript/scope3': [
         'MSG INFO: Expression in column binding must return a column name: =Text1.value',
         'WARN VSDimensionRef: Column not found: =Text1.value (city(string),state(string),customer_id(integer),orderdate('
      ],
      // the calendar is bound to a column that table assembly S_Query2 does not have (asset setup)
      'Manual/Selection/Selection_Calendar': [
         'WARN CalendarVSAQuery: Column not found in table assembly "S_Query2": null'
      ],
      // onInit: Calendar1.min is still null while the viewsheet opens, so the selectedObjects assignment throws a NullPointerException that VSAScriptable.putMember only logs (#81633); in the harness a later pass sets the value
      'ScriptAuto/Selection/Relation': [
         'ERROR VSAScriptable: Failed to set property value: selectedObjects=[Ljava.lang.Object;@'
      ],
      // alert() of the script itself
      'ScriptAuto/Selection/SelectionContainer': [
         'MSG INFO: ============false'
      ],
      // alert() of the script itself
      'ScriptAuto/VS_Self/VS_Baisc': [
         'MSG INFO: =========A'
      ],
      // alert() of the script itself
      'ScriptAuto/VS_Self/VS_embedded': [
         'MSG INFO: =========A'
      ]
   ]

   /** events that may or may not occur (they depend on the environment) */
   static final Map<String, List<String>> OPTIONAL = [
      // the shape images portal/host-org/shapes/olympicScaled/*.jpg are not in the test storage; the list depends on the images installed
      'ScriptAuto/Chart/ShapseApply': [
         'ERROR GShape: Image file not found: portal/host-org/shapes/olympicScaled/au.jpg',
         'ERROR GShape: Image file not found: portal/host-org/shapes/olympicScaled/jp.jpg',
         'ERROR GShape: Image file not found: portal/host-org/shapes/olympicScaled/us.jpg'
      ]
   ]

   static String firstLine(String s) {
      (s == null ? '' : s.readLines().find { it.trim() } ?: '').replaceAll(/@[0-9a-f]{5,}/, '@').replaceAll(/\s+/, ' ').trim().take(90)
   }

   static List<String> keys(def commands, String userMessage) {
      List<String> keys = []
      commands.each { keys << "MSG ${it.type}: ${firstLine(it.message)}".toString() }

      if(userMessage) {
         keys << "USER: ${firstLine(userMessage)}".toString()
      }

      LogRecorder.events().each { ev ->
         if(!ev.message.startsWith('Failed to flush pending tasks')) {
            keys << "${ev.level} ${ev.logger.tokenize('.').last()}: ${firstLine(ev.message)}".toString()
         }
      }

      keys
   }

   def 'a real refresh of every viewsheet shows only the known swallowed messages'() {
      given:
      def problems = []
      int refreshed = 0

      expect: 'the log calls of the product reach the recorder; if not, the tripwire would be blind'
      LogRecorder.bound()

      when:
      ScriptStateBase.viewsheetIds().each { id ->
         def name = id.substring(PFX.length())
         LogRecorder.clear()
         Tool.clearUserMessage()

         try {
            def t = new ScriptStateBase(id)
            def rec = t.refreshRecorded()
            def observed = keys(RecordingDispatcher.messages(rec), Tool.getUserMessage()).toSet()
            def expected = (EXPECTED[name] ?: []).toSet()
            def optional = (OPTIONAL[name] ?: []).toSet()
            refreshed++

            if(System.getProperty('tripwire.print')) {
               observed.sort().each { println "TRIPWIRE ${name} | ${it}" }
            }

            def unexpected = observed - expected - optional
            def gone = expected - observed

            if(unexpected) { problems << "${name}: new ${unexpected.sort()}".toString() }
            if(gone) { problems << "${name}: listed but no longer seen ${gone.sort()}".toString() }
         }
         catch(Throwable e) {
            problems << "${name}: open or refresh failed: ${e.class.simpleName} ${e.message?.take(120)}".toString()
         }
      }

      then:
      refreshed > 100
      problems == []
   }
}

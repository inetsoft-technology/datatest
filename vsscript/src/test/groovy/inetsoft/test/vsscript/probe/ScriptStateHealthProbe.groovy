package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.report.script.viewsheet.ViewsheetScope
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.VSAssembly
import inetsoft.util.script.ScriptException
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE for direction D1 (script health check). NOT part of the suite (no _Spec suffix; surefire only includes
 * **&#47;*_Spec), prints PROBE lines only and asserts nothing.
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateHealthProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 * Optional: -Dprobe.offset=0 -Dprobe.limit=20 to run a slice of the viewsheets (heap is 1g).
 *
 * Executes every script of every viewsheet in vsscriptStorage.zip directly (component script, ONINIT, ONLOAD)
 * and records which ones throw ScriptException. Direct execution is the only way to see these errors; the
 * refresh path swallows them (ViewsheetSandbox:1685).
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateHealthProbe extends Specification {
   static void probe(String msg) { println "PROBE ${msg}" }

   static String rootMessage(Throwable e) {
      Throwable c = e
      while(c.cause != null && c.cause != c) c = c.cause
      "${c.class.simpleName}: ${c.message}".replaceAll(/\s+/, ' ').take(200)
   }

   def 'D1_enumerate_ids'() {
      expect:
      def ids = ScriptStateBase.viewsheetIds()
      probe "D1 enumerate count=${ids.size()} sample=${ids.take(3)}"
      probe "D1 enumerate with-org-suffix=${ids.count { it.split('\\^').length > 5 }} total-segments-sample=${ids.take(1).collect { it.split('\\^').toList() }}"
      true
   }

   /** is the swallowed error of Script_Order_OneWay4 visible in the state the existing PNG case baselines? */
   def 'D1b_oneway4_state_after_open'() {
      expect:
      def t = new ScriptStateBase('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order_OneWay4')
      def info = ((VSAssembly) t.vs.getAssembly('Text1')).getVSAssemblyInfo()
      probe "D1b script=${info.getScript()}"
      probe "D1b Text1.value-after-open=${t.eval('Text1.value')}"
      probe "D1b typeof-parameter=${t.eval('typeof parameter')} parameter.state=${t.eval('typeof parameter.state')}"
      probe "D1b vs-variables=${t.vs.getVariables()?.collect { it.name }}"
      probe "D1b other-assemblies=${t.vs.getAssemblies().collect { it.name + ':' + it.class.simpleName }}"
      true
   }

   /** lVs onLoad uses pviewsheet (parent vs); look at the parent that embeds it */
   def 'D1c_linked_vs_structure'() {
      expect:
      ['1^128^__NULL__^Manual/LinkedAsseted/linked_vs', '1^128^__NULL__^Manual/LinkedAsseted/lVs'].each { id ->
         def t = new ScriptStateBase(id)
         probe "D1c vs=${id}"
         probe "D1c   onLoad=${t.vs.getViewsheetInfo().getOnLoad()?.replaceAll(/\s+/, ' ')}"
         probe "D1c   onInit=${t.vs.getViewsheetInfo().getOnInit()?.replaceAll(/\s+/, ' ')}"
         t.vs.getAssemblies().each { a ->
            probe "D1c   asm=${a.name}:${a.class.simpleName} script=${((VSAssembly) a).getVSAssemblyInfo().getScript()?.replaceAll(/\s+/, ' ')}"
         }
         probe "D1c   typeof pviewsheet=${t.eval('typeof pviewsheet')}"
      }
      true
   }

   def 'D1_run_all_scripts'() {
      given:
      def ids = ScriptStateBase.viewsheetIds()
      int offset = Integer.getInteger('probe.offset', 0)
      int limit = Integer.getInteger('probe.limit', ids.size())
      ids = ids.drop(offset).take(limit)
      int opened = 0, openFailed = 0, scripts = 0, errors = 0, vsWithScript = 0, vsWithError = 0
      def byMessage = [:].withDefault { 0 }
      def byKind = [:].withDefault { [0, 0] }   // kind -> [run, error]
      def byAssemblyType = [:].withDefault { [0, 0] }

      when:
      ids.each { id ->
         ScriptStateBase t
         try {
            t = new ScriptStateBase(id)
            opened++
         }
         catch(Throwable e) {
            openFailed++
            probe "D1 OPEN-FAILED vs=${id} ${rootMessage(e)}"
            return
         }
         def items = []   // [kind, assemblyName, assemblyType, script]
         def vsInfo = t.vs.getViewsheetInfo()
         if(vsInfo.getOnInit()?.trim()) items << ['ONINIT', ViewsheetScope.VIEWSHEET_SCRIPTABLE, 'Viewsheet', vsInfo.getOnInit()]
         if(vsInfo.getOnLoad()?.trim()) items << ['ONLOAD', ViewsheetScope.VIEWSHEET_SCRIPTABLE, 'Viewsheet', vsInfo.getOnLoad()]
         t.vs.getAssemblies().each { a ->
            def info = ((VSAssembly) a).getVSAssemblyInfo()
            if(info.getScript()?.trim()) items << ['SCRIPT', a.getName(), a.getClass().simpleName, info.getScript()]
         }
         if(items) vsWithScript++
         boolean anyErr = false
         items.each { kind, name, type, script ->
            scripts++
            byKind[kind][0]++
            byAssemblyType[type][0]++
            try {
               t.scope.execute(script, name)
            }
            catch(Throwable e) {
               errors++
               anyErr = true
               byKind[kind][1]++
               byAssemblyType[type][1]++
               def msg = rootMessage(e)
               byMessage[msg]++
               probe "D1 ERR vs=${id} asm=${name} kind=${kind} ex=${e.class.simpleName} root=${msg} script=${script.replaceAll(/\s+/, ' ').take(120)}"
            }
         }
         if(anyErr) vsWithError++
         probe "D1 vs=${id} scripts=${items.size()} done"
      }

      probe "D1 SUMMARY slice=${offset}+${limit} opened=${opened} openFailed=${openFailed} vsWithScript=${vsWithScript} scripts=${scripts} errors=${errors} vsWithError=${vsWithError}"
      byKind.each { k, v -> probe "D1 SUMMARY kind=${k} run=${v[0]} err=${v[1]}" }
      byAssemblyType.each { k, v -> probe "D1 SUMMARY assemblyType=${k} run=${v[0]} err=${v[1]}" }
      byMessage.sort { -it.value }.each { k, v -> probe "D1 SUMMARY message x${v}: ${k}" }

      then:
      true
   }
}

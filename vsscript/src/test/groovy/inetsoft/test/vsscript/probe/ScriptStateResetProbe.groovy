package inetsoft.test.vsscript.probe

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
 * PROBE, NOT part of the suite (no _Spec suffix). Looks for script properties that do not go back to the design value
 * when the script that set them is removed (the class of #81463, #81464, #81577, #81835: popAlpha, popLocation, tipAlpha).
 *
 * For up to -Dprobe.perType (default 2) assemblies of every type, over all viewsheets of the test storage:
 * <ol>
 * <li>read every boolean, number, text and color property of the assembly script object (the design value);</li>
 * <li>set a different value of every one of them with the component script, refresh, and keep the ones that took the new value;</li>
 * <li>remove the script, refresh, and report the properties that still have the new value.</li>
 * </ol>
 * Input state that survives a refresh on purpose (selectedObject, value, ...) shows up too; judge those by hand.
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateResetProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dprobe.out=&lt;file&gt;
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateResetProbe extends Specification {
   static final Set SKIP = ['script', 'scriptEnabled', 'name', 'type', 'assembly'] as Set

   static String read(ScriptStateBase t, String assembly, String key) {
      try {
         def v = JavaScriptEngine.unwrap(t.scope.execute("${assembly}['${key}']".toString(), assembly))
         return v == null ? 'null' : "${v.getClass().simpleName}:${v}"
      }
      catch(Throwable e) {
         return 'ERR'
      }
   }

   /** a script literal that differs from the design value, or null when the type is not handled */
   static String candidate(String base) {
      if(base == 'null') return "'zz'"
      def (type, value) = base.split(':', 2)

      switch(type) {
         case 'Boolean': return value == 'true' ? 'false' : 'true'
         case 'Integer': case 'Long': case 'Short': return String.valueOf((value as long) + 1)
         case 'Double': case 'Float': return String.valueOf((value as double) + 1)
         case 'String': return "'${value.replace("'", '')}zz'"
         case 'Color': return 'new Packages.java.awt.Color(1, 2, 3)'
         default: return null
      }
   }

   def 'reset_sweep'() {
      when:
      def out = new File(System.getProperty('probe.out', 'reset-sweep.txt'))
      out.text = ''
      int perType = Integer.getInteger('probe.perType', 2)
      def seen = [:].withDefault { 0 }
      def report = []

      ScriptStateBase.viewsheetIds().each { id ->
         def t
         try { t = new ScriptStateBase(id) } catch(Throwable e) { return }

         t.vs.getAssemblies().each { a ->
            def type = a.class.simpleName
            def name = a.name

            if(name.contains('.') || seen[type] >= perType) {
               return
            }

            seen[type]++

            try {
               // the design value is the one without any script, including the script the asset has
               t.setScript(name, '')
               def keys = t.scope.getVSAScriptable(name).getMemberKeys().collect { it.toString() }.findAll { !it.contains('.') && !SKIP.contains(it) }.sort()
               def base = [:]
               keys.each { k -> def v = read(t, name, k); if(v != 'ERR') base[k] = v }
               def lits = [:]
               base.each { k, v -> def l = candidate(v); if(l != null) lits[k] = l }
               def script = lits.collect { k, l -> "try { ${name}['${k}'] = ${l}; } catch(e) { }" }.join('\n')
               t.setScript(name, script)
               def changed = lits.keySet().findAll { read(t, name, it) != base[it] }
               t.setScript(name, '')
               def stuck = changed.findAll { read(t, name, it) != base[it] }
               out << "${id.replaceAll(/.*Manual\//, 'Manual/').replaceAll(/.*\^(ScriptAuto\/)/, 'ScriptAuto/')} ${name} (${type}): props=${base.size()} tried=${lits.size()} took=${changed.size()} NOT-RESET=${stuck}\n"
               stuck.each { report << [type, name, it] }
            }
            catch(Throwable e) {
               out << "${id} ${name} (${type}): ERROR ${e.class.simpleName}: ${e.message?.take(120)}\n"
            }
         }
      }

      def byProp = report.groupBy { it[2] }.collectEntries { k, v -> [(k): v.collect { it[0] }.unique().sort()] }
      out << "\\n== by property (types where it did not reset) ==\\n"
      byProp.sort().each { k, v -> out << "${k}: ${v}\\n" }

      then:
      true
   }

   /**
    * Writes the viewsheet XML and the base worksheet XML of the viewsheets named in the index file (-Dprobe.index, lines 'id|assembly',
    * the lines of the sweep output without the rest) to <-Dprobe.dir>/<n>/, and an index.txt with 'n|assembly' lines, for the
    * differential run of the same sweep on the 13.8 build (docs/test-gap-assessment/diff13/ResetCore.java.txt).
    */
   def 'export_for_reset'() {
      when:
      def dir = new File(System.getProperty('probe.dir', 'resetxml'))
      dir.mkdirs()
      def index = new StringBuilder()
      def ids = new LinkedHashMap()

      new File(System.getProperty('probe.index')).readLines().findAll { it.contains('|') }.each { l ->
         def (id, a) = l.split('[|]', 2)
         ids.get(id, []) << a
      }

      ids.eachWithIndex { id, assemblies, n ->
         def t = new ScriptStateBase(id)
         def sub = new File(dir, "${n}")
         sub.mkdirs()
         def vsw = new StringWriter(); def pw = new PrintWriter(vsw); t.vs.writeXML(pw); pw.flush()
         new File(sub, 'vs.xml').setText(vsw.toString(), 'UTF-8')
         def ws = t.vs.getBaseWorksheet()
         def wsw = new StringWriter(); def pw2 = new PrintWriter(wsw); ws?.writeXML(pw2); pw2.flush()
         new File(sub, 'ws.xml').setText(wsw.toString(), 'UTF-8')
         assemblies.each { index << "${n}|${it}|${id}" << System.lineSeparator() }
      }

      new File(dir, 'index.txt').setText(index.toString(), 'UTF-8')

      then:
      true
   }
}

package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE, NOT part of the suite (no _Spec suffix). `parameter.<unset>` and `this` inside a plain function, on a real opened viewsheet
 * (the 13.8 differential of docs/test-gap-assessment/diff13 found both on a bare sandbox).
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateScopeProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateScopeProbe extends Specification {
   def 'scope'() {
      when:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Form/Combox_bind')
      def exprs = ['parameter.nosuch === null', 'parameter.nosuch === undefined', 'typeof parameter.nosuch', 'parameter.nosuch == null',
                   'function g(){ return typeof this.visible; } g()',
                   'function h(){ this.visible = false; } h(); ComboBox1.visible',
                   'this === ComboBox1']
      def out = exprs.collect { "PROBE ${it} = ${t.eval(it)}" }

      // the same in a real component script
      t.setScript('ComboBox1', 'function h(){ this.visible = false; } h();')
      out << "PROBE component script: function h(){ this.visible = false } h()  -> ComboBox1.visible = ${t.eval('ComboBox1.visible')}"
      t.setScript('ComboBox1', '')
      t.setScript('Text1', 'text = String(parameter.nosuch === null) + "/" + String(parameter.nosuch === undefined) + "/" + typeof parameter.nosuch;')
      out << "PROBE component script: Text1.text = ${t.eval('Text1.text')}"

      then:
      out.each { println it }
      true
   }

   /** index file for ScriptStateResetProbe#export_for_reset: every Freehand (CalcTable) assembly, with the size of its real table */
   def 'freehand_index'() {
      when:
      def out = new File(System.getProperty('probe.index', 'freehand-index.txt'))
      out.text = ''

      ScriptStateBase.viewsheetIds().each { id ->
         try {
            def t = new ScriptStateBase(id)
            t.vs.getAssemblies().findAll { it.getClass().simpleName == 'CalcTableVSAssembly' }.each { a ->
               def rows = t.eval("${a.name}.table.length")
               def cols = t.eval("${a.name}.table[0] ? ${a.name}.table[0].length : -1")
               out << "${id}|${a.name}|rows=${rows} cols=${cols}" << System.lineSeparator()
            }
         }
         catch(Throwable e) {
            out << "${id}|ERR ${e.class.simpleName}" << System.lineSeparator()
         }
      }

      then:
      true
   }

   static final String DUMP = "(function(){var o=[];var t=%s.table;o.push('rows='+t.length);for(var r=0;r<t.length;r++){var row=[];for(var c=0;c<t[r].length;c++){var v=t[r][c];row.push(v===null?'<null>':(v===undefined?'<undef>':String(v)));}o.push(row.join('|'));}return o.join(' // ');})()"

   /** the same table dump as the 13.8 side of the differential (P13e probeAll), on the real opened viewsheet; -Dprobe.index = index.txt of the xml dir, -Dprobe.out */
   def 'table_dump'() {
      when:
      def out = new File(System.getProperty('probe.out', 'table-dump.txt'))
      out.text = ''
      def open = [:]

      new File(System.getProperty('probe.index')).readLines().findAll { it.contains('|') }.each { l ->
         def (n, a, id) = l.split('[|]', 3)
         def t = open[id]

         if(t == null) {
            try { t = open[id] = new ScriptStateBase(id) } catch(Throwable e) { out << "OPEN-FAILED ${id}" << System.lineSeparator(); return }
         }

         def v
         try { v = t.eval(String.format(DUMP, a)) } catch(Throwable e) { v = "ERR ${e.message}".replaceAll(/\s+/, ' ').take(300) }
         out << "EVAL ${id} ${a} = ${v}" << System.lineSeparator()
      }

      then:
      true
   }

   /**
    * the formula-cell differential of docs/test-gap-assessment/diff13 (P13e.variants) on the real opened viewsheet: every formula cell (and every
    * summary cell, turned into a formula) of the Freehand assembly gets each expression of -Dprobe.exprs in turn (a fresh open for each, nothing is saved),
    * then the table is dumped. -Dprobe.vs = the viewsheet id, -Dprobe.asm, -Dprobe.exprs, -Dprobe.out
    */
   def 'formula_variants'() {
      when:
      def out = new File(System.getProperty('probe.out', 'formula-variants.txt'))
      out.text = ''
      def id = System.getProperty('probe.vs')
      def asm = System.getProperty('probe.asm')

      new File(System.getProperty('probe.exprs')).readLines().findAll { it.trim() && !it.startsWith('#') }.each { e ->
         def t = new ScriptStateBase(id)
         def layout = t.vs.getAssembly(asm).getTableLayout()
         int n = 0

         layout.getCellInfos(true).each { ci ->
            def f = ci.getClass().getDeclaredField('binding')
            f.setAccessible(true)
            def tb = f.get(ci)

            if(tb != null && (tb.type == 3 || tb.type == 2 && tb.BType == 3)) {
               tb.setType(3)
               tb.setBType(2)
               tb.setValue(e)
               n++
            }
         }

         t.res.refreshViewsheet(t.principal)
         def v

         try { v = t.eval(String.format(DUMP, asm)) } catch(Throwable x) { v = "ERR ${x.message}".replaceAll(/\s+/, ' ').take(300) }
         out << "EVAL ${e} [${n}] = ${v}" << System.lineSeparator()
      }

      then:
      true
   }
}

package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import inetsoft.util.script.ScriptException
import inetsoft.report.TableCellBinding
import inetsoft.report.CellBinding
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE, batch 2 of the port-gap sweep (#81430): viewsheet component scripts (TextInput placeholderText /
 * defaultText / value on load, popLocation, freehand table). Prints "PROBE BP2 ..." lines, asserts nothing.
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateBackportProbe2 -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateBackportProbe2 extends Specification {
   static final String PFX = '1^128^__NULL__^'

   static void p(String m) { println "PROBE BP2 ${m}" }

   static String run(ScriptStateBase t, String assembly, String expr) {
      try {
         def v = JavaScriptEngine.unwrap(t.scope.execute(expr, assembly))
         return v == null ? 'null' : "${v} (${v.class.simpleName})"
      }
      catch(ScriptException e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) { c = c.cause }
         return "EX ${c.class.simpleName}: ${c.message}".toString()
      }
   }

   def "assembly names"() {
      expect:
      ['Manual/OtherScript/scope4', 'Manual/OtherScript/sEvent', 'ScriptAuto/Form/Form_RCB',
       'ScriptAuto/SpecialScript/FreehandFormula', 'ScriptAuto/SpecialScript/TableDataScript'].each { id ->
         def t = new ScriptStateBase(PFX + id)
         p "${id}: " + t.vs.getAssemblies().collect { "${it.name}:${it.class.simpleName - 'VSAssembly'}" }.join(', ')
      }
      true
   }

   def "TextInput script properties"() {
      expect:
      def t = new ScriptStateBase(PFX + 'Manual/OtherScript/scope4')
      def name = t.vs.getAssemblies().find { it.class.simpleName == 'TextInputVSAssembly' }.name
      def info = { t.vs.getAssembly(name).getVSAssemblyInfo() }
      p "${name} design before: defaultText=[${info().getDefaultText()}] defaultTextValue=[${info().getDefaultTextValue()}]"
      p "${name}.value on load => " + run(t, name, "${name}.value")
      p "${name}.value == null on load => " + run(t, name, "${name}.value == null")
      p "set placeholderText => " + run(t, name, "${name}.placeholderText = 'abc123'")
      p "read placeholderText => " + run(t, name, "${name}.placeholderText")
      p "design after: defaultText=[${info().getDefaultText()}] defaultTextValue=[${info().getDefaultTextValue()}]"
      p "set defaultText => " + run(t, name, "${name}.defaultText = 'test123456'")
      p "read defaultText => " + run(t, name, "${name}.defaultText")
      p "design after: defaultText=[${info().getDefaultText()}] defaultTextValue=[${info().getDefaultTextValue()}]"
      true
   }

   def "Text popLocation script"() {
      expect:
      def t = new ScriptStateBase(PFX + 'Manual/OtherScript/sEvent')
      def name = t.vs.getAssemblies().find { it.class.simpleName == 'TextVSAssembly' }.name
      p "${name}.popLocation => " + run(t, name, "${name}.popLocation")
      p "set popLocation MOUSE => " + run(t, name, "${name}.popLocation = 'MOUSE'")
      p "read popLocation => " + run(t, name, "${name}.popLocation")
      true
   }

   def "freehand layouts"() {
      expect:
      ['ScriptAuto/SpecialScript/FreehandFormula', 'ScriptAuto/SpecialScript/TableDataScript'].each { id ->
         def t = new ScriptStateBase(PFX + id)
         t.vs.getAssemblies().findAll { it.class.simpleName == 'CalcTableVSAssembly' }.each { a ->
            def layout = a.getTableLayout()
            p "${id} ${a.name} script=[${a.getVSAssemblyInfo().getScript()}] rows=${layout.getRowCount()} cols=${layout.getColCount()}"
            for(int r = 0; r < layout.getRowCount(); r++) {
               for(int c = 0; c < layout.getColCount(); c++) {
                  def cell = layout.getCellBinding(r, c)
                  if(cell != null) {
                     p "  [${r}][${c}] type=${cell.getType()} name=${cell.getCellName()} value=${cell.getValue()} " +
                        "expV=${cell.getExpansion()} rowGroup=${cell.getRowGroup()} colGroup=${cell.getColGroup()}"
                  }
               }
            }
         }
      }
      true
   }

   /** #79806: Category group, the original Discount summary, a text cell named amt written by the table script
    *  (col 2) and a formula cell $amt * 2 next to it (col 3) */
   def "freehand formula reads scripted cell"() {
      expect:
      ['control-column', 'text', 'text+script'].each { variant ->
         def t = new ScriptStateBase(PFX + 'ScriptAuto/SpecialScript/FreehandFormula')
         def a = t.vs.getAssembly('Freehand1')
         def layout = a.getTableLayout()
         layout.setColCount(4)
         def amt
         if(variant == 'control-column') {
            // a bound column cell named amt instead of the scripted text: the reference must work
            amt = TableCellBinding.getSummaryBinding('Discount')
            amt.setFormula('Max')
         }
         else {
            amt = new TableCellBinding(CellBinding.BIND_TEXT, 'x')
         }
         amt.setCellName('amt')
         layout.setCellBinding(0, 2, amt)
         layout.setCellBinding(0, 3, new TableCellBinding(CellBinding.BIND_FORMULA, '$amt * 2'))
         if(variant == 'text+script') {
            a.getVSAssemblyInfo().setScript('for(var r = 0; r < 12; r++) { table[r][2] = r + 1; }')
         }
         t.res.refreshViewsheet(t.principal)
         p "${variant}: rows=" + run(t, 'Freehand1', 'Freehand1.table.length')
         (0..2).each { r ->
            p "${variant} row ${r}: " + (0..3).collect { c -> "c${c}=" + run(t, 'Freehand1', "Freehand1.table[${r}][${c}]") }.join(' ')
         }
      }
      true
   }

   /** #79669 inArray(table, v), #81014 toList(table, 'maxrows=null') on a real table of an asset */
   def "table functions"() {
      expect:
      def t = new ScriptStateBase(PFX + 'Manual/OtherScript/scope4')
      p "cols => " + run(t, 'TableView1', "var tb = TableView1.table; var s = ''; for(var c = 0; c < tb.length; c++) { s += tb[0][c] + '|' } ; s")
      p "rows => " + run(t, 'TableView1', 'TableView1.table.length')
      def first = run(t, 'TableView1', 'TableView1.table[1][0]')
      p "first value => ${first}"
      p "inArray(table, first) => " + run(t, 'TableView1', "inArray(TableView1.table, TableView1.table[1][0])")
      p "inArray(table, missing) => " + run(t, 'TableView1', "inArray(TableView1.table, 'no-such-value-zz')")
      p "toList asc => " + run(t, 'TableView1', "var c0 = TableView1.table[0][0]; toList(TableView1.table, 'field=' + c0 + ',sort=asc').slice(0, 4).join(',')")
      p "toList desc => " + run(t, 'TableView1', "var c0 = TableView1.table[0][0]; toList(TableView1.table, 'field=' + c0 + ',sort=desc').slice(0, 4).join(',')")
      p "toList desc maxrows=null => " + run(t, 'TableView1', "var c0 = TableView1.table[0][0]; toList(TableView1.table, 'field=' + c0 + ',sort=desc,maxrows=null').slice(0, 4).join(',')")
      true
   }
}

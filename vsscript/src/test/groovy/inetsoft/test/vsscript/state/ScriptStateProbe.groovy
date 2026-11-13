package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.FileFormatInfo
import java.util.zip.ZipFile
import inetsoft.web.viewsheet.service.ExportResponse
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Exploration only: every case prints what it observed ("PROBE ..."), nothing is asserted beyond
 * "the probe ran". Used to decide what the state-assertion approach can and cannot see.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateProbe extends Specification {
   static final String TEXT_VS = '1^128^__NULL__^ScriptAuto/OutPut/OutPut1'
   static final String CROSSTAB_VS = '1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1'
   static final String EGRAPH_VS = '1^128^__NULL__^ScriptAuto/Chart/EGraph1'
   static final String BANDING_VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   static void probe(String msg) { println "PROBE ${msg}" }

   /** run c, return [thrown exception or null, empty log list] (logs are read from the console output) */
   static List capture(Closure c) {
      Throwable thrown = null
      try { c() } catch(Throwable e) { thrown = e }
      [thrown, []]
   }

   static String desc(Throwable e) { e == null ? 'none' : "${e.class.simpleName}:${e.message?.take(100)}" }

   // ---------- 1. script exceptions outside click ----------
   def 'P1_exception_component_script'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)
      def before = t.eval('Text1.value')

      when:
      def r = capture { t.setScript('Text1', 'value = "changed"; value = undefinedFn();') }
      def after = t.eval('Text1.value')
      probe "P1a component script error: thrown=${desc(r[0])} before=${before} after=${after}"
      r[1].findAll { it.contains('WARN') || it.contains('ERROR') }.take(5).each { probe "P1a log: ${it.take(200)}" }
      probe "P1a total log lines=${r[1].size()}"
      def rec = t.refreshRecorded()
      probe "P1a refresh commands=${rec.commands.collect { it.class.simpleName }} messages=${RecordingDispatcher.messages(rec).collect { "${it.type}:${it.message}" }}"

      then:
      true
   }

   def 'P1_exception_oninit'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      def r = capture { t.setOnInit('throw "boom-oninit";') }
      probe "P1b ONINIT throw: thrown=${desc(r[0])}"
      r[1].findAll { it.contains('WARN') || it.contains('ERROR') }.take(5).each { probe "P1b log: ${it.take(200)}" }
      def rec = t.refreshRecorded()
      probe "P1b refresh messages=${RecordingDispatcher.messages(rec).collect { "${it.type}:${it.message}" }}"
      probe "P1b viewsheet still usable, Text1.value=${t.eval('Text1.value')}"

      then:
      true
   }

   def 'P1_exception_onrefresh'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      t.vs.getViewsheetInfo().setOnLoad('Text1.value = "from-onload"; nosuch.call();')
      def r = capture { t.res.refreshViewsheet(t.principal) }
      probe "P1c ONREFRESH error: thrown=${desc(r[0])} Text1.value=${t.eval('Text1.value')}"
      r[1].findAll { it.contains('WARN') || it.contains('ERROR') }.take(5).each { probe "P1c log: ${it.take(200)}" }

      then:
      true
   }

   // ---------- 2. differential: script path vs Java/static path ----------
   def 'P2_differential_text'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      // Text1: through script. Text2: through the assembly info setter (the "static" path).
      t.setScript('Text1', 'autoSize = true')
      def info2 = t.vs.getAssembly('Text2').getVSAssemblyInfo()
      info2.setAutoSizeValue(true)
      t.res.refreshViewsheet(t.principal)
      ['autoSize', 'visible', 'text', 'value'].each { p ->
         probe "P2 ${p}: script(Text1)=${t.eval('Text1.' + p)} static(Text2)=${t.eval('Text2.' + p)}"
      }

      then:
      true
   }

   // ---------- 3. chart deep objects: EGraph / GraphElement / scale ----------
   def 'P3_chart_egraph'() {
      given:
      def t = new ScriptStateBase(EGRAPH_VS)

      when:
      def setErr = capture {
         t.setScript('Chart1',
            'dataset = [["State","Quantity"],["NJ",100],["NY",300],["PA",200]];\n' +
            'graph = new EGraph();\n' +
            'var elem = new PointElement("State", "Quantity");\n' +
            'elem.setHint(GraphElement.HINT_ALPHA, "0.5");\n' +
            'graph.addElement(elem);\n' +
            'var scale = new LinearScale("Quantity");\n' +
            'scale.setMax(400);\n' +
            'graph.setScale("Quantity", scale);')
      }
      probe "P3 set: thrown=${desc(setErr[0])}"
      [
         'Chart1.graph',
         'Chart1.graph.getElementCount()',
         'Chart1.graph.getElement(0)',
         'Chart1.graph.getElement(0).getHint(GraphElement.HINT_ALPHA)',
         'Chart1.graph.getScale("Quantity")',
         'Chart1.graph.getScale("Quantity").getMax()',
         'Chart1.dataset',
         'Chart1.dataset.getRowCount()',
      ].each { expr ->
         def r = capture { probe "P3 ${expr} => ${t.eval(expr)}" }
         if(r[0]) probe "P3 ${expr} => EXCEPTION ${desc(r[0])}"
      }

      then:
      true
   }

   // ---------- 4. format-like objects on a crosstab ----------
   def 'P4_crosstab_format_objects'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      when:
      def setErr = capture {
         t.setScript('Crosstab1',
            'cellFormat[2][2] = [StyleConstant.DECIMAL_FORMAT, "#,###.00"];\n' +
            'colFormat[0] = [StyleConstant.DATE_FORMAT, "yyyy/MM/dd"];\n' +
            'shrink = true;\n' +
            'setPresenter(3,3,"HTMLPresenter");')
      }
      probe "P4 set: thrown=${desc(setErr[0])}"
      [
         'Crosstab1.shrink',
         'Crosstab1.cellFormat',
         'Crosstab1.cellFormat[2][2]',
         'Crosstab1.colFormat[0]',
         'Crosstab1.highlighted',
         'Crosstab1.getPresenter(3,3)',
         'Crosstab1.data[2][2]',
         'Crosstab1.table[2][2]',
      ].each { expr ->
         def r = capture { probe "P4 ${expr} => ${t.eval(expr)}" }
         if(r[0]) probe "P4 ${expr} => EXCEPTION ${desc(r[0])}"
      }

      then:
      true
   }

   // ---------- 5. self round trip over every declared property ----------
   def 'P5_self_roundtrip_all_ids'() {
      given:
      def targets = [[TEXT_VS, 'Text1'], [CROSSTAB_VS, 'Crosstab1'], [BANDING_VS, 'Chart1']]

      when:
      targets.each { vsId, name ->
         def t = new ScriptStateBase(vsId)
         def ids = t.scope.getVSAScriptable(name).getMemberKeys().collect { it.toString() }.sort()
         def stats = [same: [], diff: [], writeErr: [], readErr: [], skipped: []]
         ids.each { id ->
            def v0
            try { v0 = t.eval("${name}.${id}") } catch(Throwable e) { stats.readErr << id; return }
            if(v0 == null || !(v0 instanceof Boolean || v0 instanceof Number || v0 instanceof CharSequence)) {
               stats.skipped << id
               return
            }
            try {
               t.eval("${name}.${id} = ${name}.${id}")
               def v1 = t.eval("${name}.${id}")
               (v0 == v1 ? stats.same : stats.diff) << id
            }
            catch(Throwable e) { stats.writeErr << "${id}(${e.class.simpleName})" }
         }
         probe "P5 ${name}: ids=${ids.size()} same=${stats.same.size()} diff=${stats.diff} writeErr=${stats.writeErr} readErr=${stats.readErr.size()} skipped(non-scalar/null)=${stats.skipped.size()}"
         probe "P5 ${name} same: ${stats.same}"
      }

      then:
      true
   }

   // ---------- 6. excel export probe ----------
   def 'P6_excel_probe'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)
      t.setScript('Text1', 'value = "xl-marker-456"')
      def file = File.createTempFile('vsscript-probe', '.xlsx')

      when:
      def r = capture {
         file.withOutputStream { out ->
            t.res.exportVS(FileFormatInfo.EXPORT_TYPE_EXCEL, true, false, false, false, false,
                           ['(Home)'] as String[], false, false, null, new ExportResponse(out), t.principal)
         }
      }
      probe "P6 export: thrown=${desc(r[0])} size=${file.length()}"
      def r2 = capture {
         // xlsx is a zip of XML; read it with the JDK only (Apache POI is not on the classpath)
         def zip = new ZipFile(file)
         def entries = zip.entries().collect { it.name }
         probe "P6 zip entries=${entries.findAll { it.startsWith('xl/') }}"
         def text = { String n -> zip.getInputStream(zip.getEntry(n)).getText('UTF-8') }
         def shared = entries.contains('xl/sharedStrings.xml') ? text('xl/sharedStrings.xml') : ''
         def sheets = entries.findAll { it ==~ /xl\/worksheets\/sheet\d+\.xml/ }.collect { text(it) }
         probe "P6 sharedStrings has marker=${shared.contains('xl-marker-456')} sheets=${sheets.size()} sheetXml has marker=${sheets.any { it.contains('xl-marker-456') }}"
         probe "P6 sharedStrings sample=${(shared =~ /<t[^>]*>([^<]*)<\/t>/).collect { it[1] }.take(8)}"
         zip.close()
      }
      if(r2[0]) probe "P6 read: EXCEPTION ${desc(r2[0])}"

      then:
      true

      cleanup:
      file?.delete()
   }

   // ---------- 1d. run the script directly: is the error visible to the caller? ----------
   def 'P1_exception_direct_execute'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      [
         ['undefined function', 'undefinedFn()'],
         ['throw string', 'throw "boom-direct"'],
         ['partial', 'Text1.value = "partial"; nosuch.call(); Text1.value = "after"'],
      ].each { label, script ->
         def r = capture { t.scope.execute(script, 'Text1') }
         probe "P1d ${label}: thrown=${desc(r[0])} cause=${desc(r[0]?.cause)} Text1.value=${t.eval('Text1.value')}"
      }

      then:
      true
   }

   // ---------- 6b. excel: crosstab cells ----------
   def 'P6b_excel_crosstab_probe'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)
      def file = File.createTempFile('vsscript-probe-xt', '.xlsx')
      def rows = t.eval('Crosstab1.data.length')

      when:
      file.withOutputStream { out ->
         t.res.exportVS(FileFormatInfo.EXPORT_TYPE_EXCEL, true, false, false, false, false,
                        ['(Home)'] as String[], false, false, null, new ExportResponse(out), t.principal)
      }
      def zip = new ZipFile(file)
      def names = zip.entries().collect { it.name }.findAll { it ==~ /xl\/worksheets\/sheet\d+\.xml/ }
      def sheet = zip.getInputStream(zip.getEntry(names[0])).getText('UTF-8')
      def shared = zip.getInputStream(zip.getEntry('xl/sharedStrings.xml')).getText('UTF-8')
      def cells = (sheet =~ /<c r="([A-Z]+\d+)"[^>]*>(?:<f>[^<]*<\/f>)?(?:<v>([^<]*)<\/v>)?/).collect { [it[1], it[2]] }
      probe "P6b crosstab rows(script)=${rows} sheets=${names.size()} cells=${cells.size()} sharedStrings=${(shared =~ /<t[^>]*>/).count} first cells=${cells.take(12)}"
      zip.close()

      then:
      true

      cleanup:
      file?.delete()
   }
}

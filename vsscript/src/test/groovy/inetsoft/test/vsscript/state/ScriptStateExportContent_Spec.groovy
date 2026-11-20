package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.vsscript.support.XlsxText
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.FileFormatInfo
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

/**
 * What the export holds, against what the viewsheet state says (D7 of vsscript-upgrade-plan.md; ScriptStateExportTripwire_Spec only checks that an
 * export does not throw). Excel is read without POI (support/XlsxText), PDF with PDFBox.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateExportContent_Spec extends Specification {
   static final String CHAIN = '1^128^__NULL__^Manual/ScriptOrder/Script_Order_OneWay1'

   /** the rows of the Excel export, each the texts of its cells in column order; sheet = one sheet name ("(Home)" is the page, the others are the data tabs of the tables), null = all */
   static List<List<String>> excelRows(ScriptStateBase t, String sheet = null) {
      def file = t.exportFile(FileFormatInfo.EXPORT_TYPE_EXCEL, '.xlsx')

      try {
         def book = XlsxText.read(file)
         (sheet == null ? book.values() : [book[sheet]]).collectMany { rows -> rows.values().collect { cells -> cells.values() as List<String> } }
      }
      finally {
         file.delete()
      }
   }

   static List<String> excelTexts(ScriptStateBase t, String sheet = null) {
      excelRows(t, sheet).flatten() as List<String>
   }

   static String pdfText(ScriptStateBase t) {
      def file = t.exportFile(FileFormatInfo.EXPORT_TYPE_PDF, '.pdf')

      try {
         PDDocument.load(file).withCloseable { new PDFTextStripper().getText(it) }
      }
      finally {
         file.delete()
      }
   }

   def 'a_value_written_by_a_script_chain_is_in_the_excel_export'() {
      // FreehandTable1's script is FreehandTable1.data[1][0] = Text1.value: the Text1 value (design text, or the one a script sets) ends up in the first data cell
      given:
      def t = new ScriptStateBase(CHAIN)
      def before = excelTexts(t)

      when:
      t.setScript('Text1', "value = 'export-chain-marker-7'")
      def after = excelTexts(t)

      then: 'control: the design text is exported, the marker is not'
      before.count('Text') >= 1
      !before.contains('export-chain-marker-7')

      and: 'the state shows the marker in the table and the export holds it, in place of the design text'
      t.eval('FreehandTable1.table[1][0]') == 'export-chain-marker-7'
      after.contains('export-chain-marker-7')
      after.count('export-chain-marker-7') == before.count('Text')
      !after.contains('Text')
   }

   def 'a_value_written_by_a_script_chain_is_in_the_pdf_export'() {
      given:
      def t = new ScriptStateBase(CHAIN)

      when:
      t.setScript('Text1', "value = 'export-chain-marker-8'")

      then:
      pdfText(t).contains('export-chain-marker-8')
   }

   static boolean sameValue(Object v, String text) {
      // the data tabs write a Boolean as 0 / 1
      v instanceof Boolean ? (text == v.toString() || text == (v ? '1' : '0')) : v instanceof Number ? (text ==~ /-?\d+(\.\d+)?([eE][-+]?\d+)?/ && Math.abs((text as double) - v.doubleValue()) < 1e-9) : v.toString() == text
   }

   /**
    * The rows of the table assembly (as the state shows them, first maxCols columns, empty and date cells left out) for which no row of the export holds
    * all their values; "row=values". The second element is the number of rows checked. A value that is only somewhere else in the export (another table
    * of the same viewsheet shows the same state names) does not count.
    */
   static List missingRows(ScriptStateBase t, String asm, List<List<String>> exportRows, int maxRows = Integer.MAX_VALUE, int maxCols = Integer.MAX_VALUE) {
      def rows = Math.min(t.eval("${asm}.table.length") as int, maxRows)
      def cols = Math.min(t.eval("${asm}.table[0].length") as int, maxCols)
      def missing = []
      int checked = 0
      def previous = [:]

      for(int r = 0; r < rows; r++) {
         def expected = []

         for(int c = 0; c < cols; c++) {
            def v = inetsoft.util.script.JavaScriptEngine.unwrap(t.evalRaw("${asm}.table[${r}][${c}]"))

            // a group value that repeats the one above is a merged cell in the export: only its first row holds it
            boolean repeated = r > 0 && previous[c] != null && previous[c] == v
            previous[c] = v

            if(v != null && !(v instanceof Date) && !v.toString().isEmpty() && !repeated) {
               expected << v
            }
         }

         if(expected) {
            checked++

            if(!exportRows.any { row -> expected.every { v -> row.any { sameValue(v, it) } } }) {
               missing << "${r}=${expected}".toString()
            }
         }
      }

      [missing, checked]
   }

   @Unroll
   def 'every row of the table #asm of #vs is in the excel export, on the page and in the data tab'() {
      given:
      def t = new ScriptStateBase('1^128^__NULL__^' + vs)

      when:
      // the page shows only the part of a table that fits its box (first rows and columns); the data tabs hold all of it
      def (pageMissing, pageChecked) = missingRows(t, asm, excelRows(t, '(Home)'), 5, 4)
      def (missing, checked) = missingRows(t, asm, excelRows(t))

      then:
      pageChecked > 0
      pageMissing == []
      checked >= pageChecked
      missing == []

      where:
      vs                                         | asm
      'Manual/Table/Table_Action1'               | 'FreehandTable1'
      'Manual/Table/Table_Action1'               | 'TableView1'
      'Manual/OtherScript/scope2'                | 'FreehandTable1'
      'ScriptAuto/Table/Table_Freehand1'         | 'FreehandTable1'
      'ScriptAuto/Table/Table_Freehand1'         | 'FreehandTable3'
      'ScriptAuto/SpecialScript/TableDataScript' | 'Freehand1'
      'ScriptAuto/SpecialScript/FreehandFormula' | 'Freehand1'
      'ScriptAuto/Table/FreehandSetRowHeight'    | 'TableView1'
   }

   def 'control: the check does notice values that are not in the export'() {
      // the texts of another viewsheet's export do not hold this table's values, so the check must list them
      given:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Table/Table_Action1')
      def other = excelRows(new ScriptStateBase('1^128^__NULL__^ScriptAuto/SpecialScript/FreehandFormula'))

      when:
      def (missing, checked) = missingRows(t, 'FreehandTable1', other)

      then:
      checked > 0
      missing.size() > checked / 2
   }
}

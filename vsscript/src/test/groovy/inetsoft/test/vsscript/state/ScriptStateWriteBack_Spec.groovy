package inetsoft.test.vsscript.state

import inetsoft.analytic.composition.ViewsheetService
import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.vsscript.support.RecordingDispatcher
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.core.MessageTestUtils
import inetsoft.util.ConfigurationContext
import inetsoft.report.composition.WorksheetEngine
import inetsoft.uql.asset.Worksheet
import inetsoft.util.Tool
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.viewsheet.service.VSInputService
import inetsoft.web.viewsheet.service.VSObjectService
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * The scripts that write back to the worksheet from an onClick script: appendRow, setCellValue, applyChanges and saveWorksheet
 * (manual cases appendRow and saveworksheet of docs/vsscript/VSScript-Manual-Case.xlsx). The write goes to the storage of the test home
 * (vsscript/target/test-resources/server/config-vsscript), and the home is only restored from vsscriptStorage.zip when that directory
 * is missing, so the tests compare with the state they find and put a changed cell back. Rows that appendRow adds stay in a local
 * home; delete the directory to start from the zip again (a clean checkout or CI always does).
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateWriteBack_Spec extends Specification {
   static final String APPEND = '1^128^__NULL__^Manual/OtherScript/appendRows'
   static final String SAVE = '1^128^__NULL__^Manual/OtherScript/saveworksheet'

   /** click an assembly: runs its onClick script, as the click in the browser does */
   static void click(ScriptStateBase t, String assembly) {
      def ctx = ConfigurationContext.getContext()
      def input = new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                                     ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
      def service = new OnClickService(ctx.getSpringBean(CoreLifecycleService), input, ctx.getSpringBean(ViewsheetService))
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         service.onClick(t.runtimeId, assembly, null, null, null, ScriptStateBase.BASE_URL, t.principal, rec)
      }
   }

   /** the rows the table shows, as comma separated text (the first is the header) */
   static List<String> rows(ScriptStateBase t, String table) {
      (0..<(t.eval("${table}.table.length") as int)).collect { t.eval("String(${table}.table[${it}])") as String }
   }

   /** the worksheet of appendRows as XML, before a test writes to it (appendRow and setCellValue write the embedded table NullTable and save it) */
   static String snapshot() {
      def sw = new StringWriter()
      def pw = new PrintWriter(sw)
      new ScriptStateBase(APPEND).vs.getOriginalWorksheet().writeXML(pw)
      pw.flush()
      sw.toString()
   }

   /** put the worksheet back to a snapshot: the test home is shared with the other specs and with later runs */
   static void restore(String xml) {
      def t = new ScriptStateBase(APPEND)
      def ws = new Worksheet()
      ws.parseXML(Tool.parseXML(new ByteArrayInputStream(xml.getBytes('UTF-8'))).documentElement)
      WorksheetEngine.getWorksheetService().getAssetRepository().setSheet(t.vs.getBaseEntry(), ws, t.principal, true)
   }

   def 'appendRow_and_setCellValue_in_an_onClick_script_change_the_worksheet_and_are_saved'() {
      // appendRows: Submit2 runs appendRow("NullTable", [1, "aaa"]), setCellValue("NullTable", 1, 1, "setCellValue"), saveWorksheet()
      // (the screenshots of the manual case: a row 1,aaa is added, the string of the first row is setCellValue)
      given: 'the saved data already holds setCellValue from an earlier run (the zip was taken after a manual run), so blank the cell first'
      def snap = snapshot()
      def clear = new ScriptStateBase(APPEND)
      clear.eval('setCellValue("NullTable", 1, 1, "")')
      clear.eval('saveWorksheet()')
      def t = new ScriptStateBase(APPEND)
      def before = rows(t, 'TableView1')

      expect:
      before[1] == ','

      when:
      click(t, 'Submit2')
      def after = rows(t, 'TableView1')

      then: 'one row more, it is the appended one, and the cell was set'
      after.size() == before.size() + 1
      after.last() == '1,aaa'
      after[1] == ',setCellValue'

      and: 'saveWorksheet wrote it: a viewsheet opened afterwards sees the same'
      rows(new ScriptStateBase(APPEND), 'TableView1') == after

      cleanup: 'the appended row and the cell stay in the shared test home otherwise'
      if(snap != null) {
         restore(snap)
      }
   }

   def 'a_second_click_appends_again_not_replaces'() {
      // negative control for the case above: the row count follows the number of clicks
      given:
      def snap = snapshot()
      def t = new ScriptStateBase(APPEND)
      def before = (rows(t, 'TableView1')).size()

      when:
      click(t, 'Submit2')
      click(t, 'Submit2')

      then:
      rows(t, 'TableView1').size() == before + 2

      cleanup: 'the two appended rows stay in the shared test home otherwise'
      if(snap != null) {
         restore(snap)
      }
   }

   def 'applyChanges_and_saveWorksheet_save_a_form_table_edit_and_nothing_is_saved_before_the_click'() {
      // saveworksheet: the user edits a cell of the form table (screenshot: the edited cell NY1), Submit1 runs
      // TableView1.applyChanges(), saveWorksheet() and refreshAfterSubmit = true; the new value stays after the refresh
      given:
      def t = new ScriptStateBase(SAVE)
      def original = t.eval('TableView1.table[1][2]') as String
      def changed = original == 'NY1' ? 'NY' : 'NY1'

      expect:
      t.eval('TableView1.form') == true

      when: 'the cell is edited, not submitted'
      t.eval("TableView1.setObject(1, 2, '${changed}')")

      then: 'the edit is only in this runtime viewsheet'
      t.eval('TableView1.table[1][2]') == changed
      new ScriptStateBase(SAVE).eval('TableView1.table[1][2]') == original

      when: 'Submit1 applies the changes and saves the worksheet'
      click(t, 'Submit1')

      then:
      t.eval('TableView1.table[1][2]') == changed
      new ScriptStateBase(SAVE).eval('TableView1.table[1][2]') == changed

      cleanup: 'put the cell back (the manual case says so as well)'
      if(t != null && changed != null) {
         t.eval("TableView1.setObject(1, 2, '${original}')")
         click(t, 'Submit1')
      }
   }
}

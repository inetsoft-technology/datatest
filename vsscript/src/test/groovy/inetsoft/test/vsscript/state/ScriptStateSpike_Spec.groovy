package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.core.MessageTestUtils
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.util.ConfigurationContext
import inetsoft.analytic.composition.ViewsheetService
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.viewsheet.service.ExportResponse
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Spike: one simple case for each verification approach in vsscript-upgrade-plan.md.
 * Nothing here exports a PNG for comparison.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateSpike_Spec extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/OutPut/OutPut1'

   // B. state assertion: script writes a value, read it back, no rendering
   def 'B_state_value'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      t.setScript('Text1', 'value = "hello"')

      then:
      t.eval('Text1.value') == 'hello'
   }

   // C. property round trip, cases generated from a table
   @Unroll
   def 'C_roundtrip_#prop'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      t.setScript('Text1', "${prop} = ${literal}")

      then:
      t.eval("Text1.${prop}") == expected

      where:
      prop       | literal | expected
      'autoSize' | 'true'  | true
      'autoSize' | 'false' | false
      'wrapping' | 'true'  | true
      'wrapping' | 'false' | false
   }

   // D. differential: value set by script must equal what the assembly info holds
   def 'D_script_vs_assembly_info'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      t.setScript('Text1', 'autoSize = true')
      def viaScript = t.eval('Text1.autoSize')
      def viaInfo = t.vs.getAssembly('Text1').getVSAssemblyInfo().isAutoSize()

      then:
      viaScript == viaInfo
   }

   // E. global function with an independently computed expected value
   def 'E_global_function'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      t.eval('formatNumber(1234.567, "#,##0.00", "")') == new java.text.DecimalFormat('#,##0.00').format(1234.567)
   }

   // F. click -> script alert -> MessageCommand recorded by the dispatcher
   def 'F_click_alert'() {
      given:
      def t = new ScriptStateBase(VS)
      t.setScript('Text1', 'alert("clicked", "warning")')
      def recorder = RecordingDispatcher.create(t.principal)
      def ctx = ConfigurationContext.getContext()
      // OnClickService is not a bean in the datatest Spring context; build it from the beans that are
      // VSInputService is only used for submit values (submitEvent), not for a plain click
      def inputService = null
      def service = new OnClickService(ctx.getSpringBean(CoreLifecycleService), inputService,
                                       ctx.getSpringBean(ViewsheetService))

      when:
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         service.onClick(t.runtimeId, 'Text1', null, null, null, 'http://localhost:8080/sree',
                         t.principal, recorder)
      }
      def msgs = RecordingDispatcher.messages(recorder)

      then:
      msgs.size() == 1
      msgs[0].message == 'clicked'
      msgs[0].type.toString() == 'WARNING'
   }

   // G. export: script effect must show up in the exported PDF text
   def 'G_export_pdf_text'() {
      given:
      def t = new ScriptStateBase(VS)
      t.setScript('Text1', 'value = "export-marker-123"')
      def file = File.createTempFile('vsscript-spike', '.pdf')

      when:
      file.withOutputStream { out ->
         t.res.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true, false, false, false, false,
                        ['(Home)'] as String[], false, false, null, new ExportResponse(out), t.principal)
      }
      def text = PDDocument.load(file).withCloseable { new PDFTextStripper().getText(it) }

      then:
      text.contains('export-marker-123')

      cleanup:
      file?.delete()
   }
}

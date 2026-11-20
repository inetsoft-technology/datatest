package inetsoft.test.vsscript.state

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
import inetsoft.analytic.composition.ViewsheetService
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.service.CoreLifecycleService
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Smoke cases of the state-assertion path on a real opened viewsheet: a script value read back (B), and a click that runs the script and
 * records the alert message (F). The property round trips, the assembly-info comparison, the global function and the PDF text that were here
 * are covered by Java unit tests (ScriptPropertyRoundTripTest, the Calc tests) and by ScriptStateExportContent_Spec.
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
         service.onClick(t.runtimeId, 'Text1', null, null, null, ScriptStateBase.BASE_URL,
                         t.principal, recorder)
      }
      def msgs = RecordingDispatcher.messages(recorder)

      then:
      msgs.size() == 1
      msgs[0].message == 'clicked'
      msgs[0].type.toString() == 'WARNING'
   }
}

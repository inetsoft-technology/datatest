package inetsoft.test.vsscript.state

import inetsoft.analytic.composition.ViewsheetService
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.core.MessageTestUtils
import inetsoft.util.ConfigurationContext
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.event.InputValue
import inetsoft.web.viewsheet.event.VSSubmitEvent
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.viewsheet.service.VSInputService
import inetsoft.web.viewsheet.service.VSObjectService
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Submit component: the scriptable property, and the submit flow
 * (input values sent with the click are applied, then the Submit script runs).
 * Asset: Manual/Form/Forms_Submit (Slider1, Spinner1, TextInput1, Submit1, ...).
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateSubmit_Spec extends Specification {
   static final String VS = '1^128^__NULL__^Manual/Form/Forms_Submit'

   /** OnClickService with a real VSInputService; the 7 editor-only dependencies of VSInputService are null */
   static OnClickService clickService() {
      def ctx = ConfigurationContext.getContext()
      def input = new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                                     ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
      new OnClickService(ctx.getSpringBean(CoreLifecycleService), input, ctx.getSpringBean(ViewsheetService))
   }

   static VSSubmitEvent submitEvent(Map<String, Object> values) {
      def items = values.collect { n, v -> [assemblyName: { n }, value: { v }] as InputValue } as InputValue[]
      [values: { items }] as VSSubmitEvent
   }

   /** click Submit1 with the given pending input values; returns the recorder */
   static RecordingDispatcher.Recorder submit(ScriptStateBase t, Map<String, Object> values) {
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         clickService().onClick(t.runtimeId, 'Submit1', null, null, submitEvent(values),
                                ScriptStateBase.BASE_URL, t.principal, rec)
      }
      rec
   }

   def 'Submit_refreshAfterSubmit_roundtrip'() {
      given:
      def t = new ScriptStateBase(VS)
      def before = t.eval('Submit1.refreshAfterSubmit')

      when:
      t.setScript('Submit1', "refreshAfterSubmit = ${!before}")

      then:
      before == true
      t.eval('Submit1.refreshAfterSubmit') == false
   }

   def 'Submit_applies_pending_input_values'() {
      given:
      def t = new ScriptStateBase(VS)

      expect: 'initial state of the three inputs'
      t.eval('TextInput1.value') == null
      t.eval('Spinner1.selectedObject') == 0d
      t.eval('Slider1.selectedObject') == 67d

      when:
      submit(t, [TextInput1: 'abc', Spinner1: 7, Slider1: 3])

      then:
      t.eval('TextInput1.value') == 'abc'
      t.eval('Spinner1.selectedObject') == 7d
      t.eval('Slider1.selectedObject') == 3d
   }

   def 'Submit_without_values_changes_nothing'() {
      // negative control: the assertions above must discriminate
      given:
      def t = new ScriptStateBase(VS)

      when:
      submit(t, [:])

      then:
      t.eval('TextInput1.value') == null
      t.eval('Spinner1.selectedObject') == 0d
      t.eval('Slider1.selectedObject') == 67d
   }

   def 'Submit_script_runs_after_values_are_applied'() {
      given:
      def t = new ScriptStateBase(VS)
      t.setScript('Submit1', 'alert("submitted:" + Spinner1.selectedObject, "info")')

      when:
      def rec = submit(t, [Spinner1: 7])
      def msgs = RecordingDispatcher.messages(rec).collect { it.message }
      println "SUBMIT messages=${msgs}"

      then: 'the script saw the value that was submitted with the click'
      msgs.contains('submitted:7')
   }

   def 'Submit_script_alert_is_recorded_twice'() {
      // Observed, cause not investigated (maybe once for the click and once for the refresh it triggers).
      // Pinned so that a change in the number of messages is noticed instead of passing silently.
      given:
      def t = new ScriptStateBase(VS)
      t.setScript('Submit1', 'alert("submitted:" + Spinner1.selectedObject, "info")')

      when:
      def msgs = RecordingDispatcher.messages(submit(t, [Spinner1: 7])).collect { it.message }

      then:
      msgs == ['submitted:7', 'submitted:7']
   }
}

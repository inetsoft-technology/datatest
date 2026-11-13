package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.analytic.composition.ViewsheetService
import inetsoft.test.core.MessageTestUtils
import inetsoft.util.ConfigurationContext
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.event.InputValue
import inetsoft.web.viewsheet.event.VSSubmitEvent
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.viewsheet.service.VSObjectService
import inetsoft.web.viewsheet.service.VSInputService
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Exploration for the Submit component (SubmitVSAScriptable has 0 line coverage in JaCoCo).
 * Prints "PROBE ..." lines; asserts only that the probe ran.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateSubmitProbe_Spec extends Specification {
   static void probe(String msg) { println "PROBE ${msg}" }

   static String desc(Throwable e) { e == null ? 'none' : "${e.class.simpleName}:${e.message?.take(120)}" }

   @Unroll
   def 'S1_structure_#id'() {
      when:
      def t
      try { t = new ScriptStateBase("1^128^__NULL__^${id}") }
      catch(Throwable e) { probe "S1 ${id}: OPEN FAILED ${desc(e)}"; return }
      def names = t.vs.getAssemblies().collect { "${it.name}:${it.class.simpleName.replace('VSAssembly', '')}" }
      probe "S1 ${id}: assemblies=${names}"
      t.vs.getAssemblies().findAll { it.class.simpleName == 'SubmitVSAssembly' }.each { a ->
         def info = a.getVSAssemblyInfo()
         probe "S1 ${id}: ${a.name} scriptEnabled=${info.isScriptEnabled()} script=${info.getScript()?.take(160)?.replace('\n', ' | ')}"
         try { probe "S1 ${id}: ${a.name}.refreshAfterSubmit=${t.eval(a.name + '.refreshAfterSubmit')}" }
         catch(Throwable e) { probe "S1 ${id}: read refreshAfterSubmit failed ${desc(e)}" }
      }

      then:
      true

      where:
      id << ['Manual/Form/Forms_Submit', 'Manual/OutPut/Submit_Action']
   }

   def 'S2_scriptable_property'() {
      when:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Form/Forms_Submit')
      def sub = t.vs.getAssemblies().find { it.class.simpleName == 'SubmitVSAssembly' }
      probe "S2 submit assembly=${sub?.name}"
      def before = t.eval(sub.name + '.refreshAfterSubmit')
      t.setScript(sub.name, "refreshAfterSubmit = ${!before}")
      def after = t.eval(sub.name + '.refreshAfterSubmit')
      probe "S2 refreshAfterSubmit before=${before} after=${after}"

      then:
      true
   }

   def 'S3_construct_VSInputService'() {
      when:
      def ctx = ConfigurationContext.getContext()
      def deps = ['inetsoft.web.viewsheet.service.VSObjectService',
                  'inetsoft.web.viewsheet.service.CoreLifecycleService',
                  'inetsoft.analytic.composition.ViewsheetService',
                  'inetsoft.web.viewsheet.service.VSObjectPropertyService',
                  'inetsoft.web.viewsheet.service.VSDialogService',
                  'inetsoft.web.viewsheet.service.VSTrapService',
                  'inetsoft.web.binding.service.DataRefModelFactoryService',
                  'inetsoft.web.viewsheet.handler.VSAssemblyInfoHandler',
                  'inetsoft.web.binding.handler.VSColumnHandler',
                  'inetsoft.web.viewsheet.service.QueryManagerService']
      deps.each { cn ->
         try {
            def c = Class.forName(cn)
            def bean = ctx.getSpringBean(c)
            probe "S3 ${cn.tokenize('.').last()}: bean available (${bean.class.simpleName})"
         }
         catch(ClassNotFoundException e) { probe "S3 ${cn}: CLASS NOT FOUND at this package" }
         catch(Throwable e) { probe "S3 ${cn.tokenize('.').last()}: ${desc(e)}" }
      }

      then:
      true
   }

   /** VSInputService built from the real beans the submit path uses; the 7 editor-only dependencies are null */
   static VSInputService inputService() {
      def ctx = ConfigurationContext.getContext()
      new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                         ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
   }

   static OnClickService clickService() {
      def ctx = ConfigurationContext.getContext()
      new OnClickService(ctx.getSpringBean(CoreLifecycleService), inputService(), ctx.getSpringBean(ViewsheetService))
   }

   static VSSubmitEvent submitEvent(Map<String, Object> values) {
      def items = values.collect { n, v -> [assemblyName: { n }, value: { v }] as InputValue } as InputValue[]
      [values: { items }] as VSSubmitEvent
   }

   def 'S4_submit_applies_input_values'() {
      when:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Form/Forms_Submit')
      def before = t.eval('TextInput1.selectedObject')
      def rec = RecordingDispatcher.create(t.principal)
      def err = null
      try {
         MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
            clickService().onClick(t.runtimeId, 'Submit1', null, null, submitEvent([TextInput1: 'abc']),
                                   'http://localhost:8080/sree', t.principal, rec)
         }
      }
      catch(Throwable e) { err = e }
      probe "S4 thrown=${desc(err)}"
      probe "S4 TextInput1.selectedObject before=${before} after=${t.eval('TextInput1.selectedObject')}"
      probe "S4 commands=${rec.commands.collect { it.class.simpleName }.unique()} messages=${RecordingDispatcher.messages(rec).collect { "${it.type}:${it.message}" }}"

      then:
      true
   }

   def 'S5_submit_script_sees_applied_values'() {
      when:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Form/Forms_Submit')
      t.setScript('Submit1', 'alert("submitted:" + TextInput1.selectedObject, "info")')
      def rec = RecordingDispatcher.create(t.principal)
      def err = null
      try {
         MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
            clickService().onClick(t.runtimeId, 'Submit1', null, null, submitEvent([TextInput1: 'abc']),
                                   'http://localhost:8080/sree', t.principal, rec)
         }
      }
      catch(Throwable e) { err = e }
      probe "S5 thrown=${desc(err)}"
      probe "S5 messages=${RecordingDispatcher.messages(rec).collect { "${it.type}:${it.message}" }}"
      probe "S5 TextInput1.selectedObject after=${t.eval('TextInput1.selectedObject')}"

      then:
      true
   }

   def 'S6_submit_which_property_reflects_values'() {
      when:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Form/Forms_Submit')
      def read = { n -> ['value', 'selectedObject', 'selectedLabel'].collect { p ->
         try { "${p}=${t.eval(n + '.' + p)}" } catch(Throwable e) { "${p}=ERR" } }.join(' ') }
      def inputs = [TextInput1: 'abc', Spinner1: 7, Slider1: 3]
      inputs.each { n, v -> probe "S6 before ${n}: ${read(n)}" }
      def rec = RecordingDispatcher.create(t.principal)
      def err = null
      try {
         MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
            clickService().onClick(t.runtimeId, 'Submit1', null, null, submitEvent(inputs),
                                   'http://localhost:8080/sree', t.principal, rec)
         }
      }
      catch(Throwable e) { err = e }
      probe "S6 thrown=${desc(err)}"
      inputs.each { n, v -> probe "S6 after  ${n} (sent ${v}): ${read(n)}" }

      then:
      true
   }
}

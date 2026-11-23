package inetsoft.test.vsscript.state

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
 * A script-written alpha / tip value goes back to the design value when the script that set it is removed and the viewsheet is refreshed: the class of
 * #81463 (popAlpha of Text and Image), #81577 (tipAlpha of Chart) and #81835 (tipAlpha / tipView of Table, Crosstab, CalcTable). For up to two
 * components of every type that has such a property, over all viewsheets of the test storage: read the design value, set a different one with the
 * component script, remove the script, refresh, read again.
 *
 * It fails on a property that keeps the script value and is not in KNOWN_STAYS, and on a listed one that now resets (so the list can only shrink).
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateResetSweep_Spec extends Specification {
   /** component type (class simple name) -> [script property: a script literal that differs from the design value] */
   static final Map CASES = [
      TextVSAssembly          : [popAlpha: '55'],
      ImageVSAssembly         : [popAlpha: '55', imageAlpha: '55'],
      GroupContainerVSAssembly: [imageAlpha: '55'],
      ChartVSAssembly         : [tipAlpha: '55'],
      TableVSAssembly         : [tipAlpha: '55', tipView: "'zz'"],
      CrosstabVSAssembly      : [tipAlpha: '55', tipView: "'zz'"],
      CalcTableVSAssembly     : [tipAlpha: '55', tipView: "'zz'"]
   ]

   /**
    * Properties that still keep the script value after the reset, "Type.property". Image and GroupContainer imageAlpha: ImageVSAssemblyInfo.resetRuntimeValues
    * clears alphaValue (popAlpha) but not imageAlphaValue, and GroupContainerVSAssemblyInfo has no reset for it (#82098, left open by #81835).
    */
   static final Set<String> KNOWN_STAYS = [
      'ImageVSAssembly.imageAlpha',
      'GroupContainerVSAssembly.imageAlpha'
   ] as Set

   static String read(ScriptStateBase t, String assembly, String key) {
      try {
         def v = JavaScriptEngine.unwrap(t.scope.execute("${assembly}['${key}']".toString(), assembly))
         return v == null ? 'null' : "${v}".toString()
      }
      catch(Throwable e) {
         return 'ERR ' + e.class.simpleName
      }
   }

   /** "60.0" and "60" are the same value */
   static boolean sameValue(String a, String b) {
      a == b || (a.isNumber() && b.isNumber() && (a as BigDecimal).compareTo(b as BigDecimal) == 0)
   }

   def 'a_script_written_alpha_or_tip_value_goes_back_to_the_design_value_after_the_script_is_removed'() {
      when:
      def perType = [:].withDefault { 0 }
      def stays = [] as Set
      def invalid = []
      def checked = [:].withDefault { 0 }

      ScriptStateBase.viewsheetIds().each { id ->
         def t

         try { t = new ScriptStateBase(id) } catch(Throwable e) { return }

         t.vs.getAssemblies().each { a ->
            def type = a.class.simpleName
            def props = CASES[type]

            if(props == null || a.name.contains('.') || perType[type] >= 2) {
               return
            }

            perType[type]++

            props.each { key, literal ->
               def design = read(t, a.name, key)
               t.setScript(a.name, "${a.name}['${key}'] = ${literal}".toString())
               def set = read(t, a.name, key)
               t.setScript(a.name, '')
               def after = read(t, a.name, key)

               if(sameValue(set, design)) {
                  // the script did not change the value (control invalid): nothing to say about the reset for this component
                  invalid << "${type}.${key} on ${a.name}".toString()
               }
               else {
                  checked["${type}.${key}".toString()]++

                  if(!sameValue(after, design)) {
                     stays << "${type}.${key}".toString()
                  }
               }
            }
         }
      }

      def unexpected = (stays - KNOWN_STAYS).sort()
      def gone = KNOWN_STAYS.findAll { checked[it] > 0 && !stays.contains(it) }.sort()
      def unchecked = KNOWN_STAYS.findAll { checked[it] == 0 }.sort()

      then: 'the sweep reached every listed property (else the storage has no such component)'
      unchecked == []

      and: 'it covered the fixed ones too'
      checked.size() >= 8

      and: 'no property keeps the script value except the listed ones'
      unexpected == []

      and: 'a listed property still keeps it (else remove it from KNOWN_STAYS)'
      gone == []
   }
}

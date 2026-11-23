package inetsoft.test.vsscript.state

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
 * What a script reads where nothing is: a row or cell outside a table (#81786) and a parameter that was not set (#81929), on real opened viewsheets.
 * Both have Java tests with the fixes (TableArray, VariableScriptable / ScopeProxy); these check them through the real open and the component script scope,
 * where #81627's history showed a Java fixture can pass and the real path still fail.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateScopeReads_Spec extends Specification {
   static final String TABLE_VS = '1^128^__NULL__^Manual/Table/Table_Action1'
   static final String TEXT_VS = '1^128^__NULL__^Manual/Chart/TestCase_DrillEnabled'

   def 'a_row_or_cell_outside_the_table_reads_undefined_Bug_81786'() {
      given:
      def t = new ScriptStateBase(TABLE_VS)
      def rows = t.eval('TableView1.table.length') as int

      expect:
      rows > 1
      t.eval('typeof TableView1.table[1000]') == 'undefined'
      t.eval('typeof TableView1.table[-1]') == 'undefined'
      t.eval('typeof TableView1.table[1][999]') == 'undefined'
      t.eval('typeof FreehandTable1.table[1000]') == 'undefined'

      and: 'the usual loop over the rows ends at the last row'
      (t.eval('var i = 0; while(TableView1.table[i] !== undefined && i < 100000) i++; i') as int) == rows
   }

   def 'an_unset_parameter_reads_null_in_a_component_script_Bug_81929'() {
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      t.setScript('Text1', "Text1.text = [parameter.notThere === null, typeof parameter.notThere, parameter.notThere == null].join('|')")

      then: '13.8 reads null for an unset parameter: === null is true and typeof is object'
      t.eval('Text1.text') == 'true|object|true'
   }

   def 'a_parameter_that_was_set_keeps_its_value_Bug_81929'() {
      given:
      def t = new ScriptStateBase(TEXT_VS, ['p1': ['v1'] as String[]])

      when:
      t.setScript('Text1', "Text1.text = [parameter.p1, parameter.notThere === null].join('|')")

      then:
      t.eval('Text1.text') == 'v1|true'
   }

   def 'documented_side_effect_of_81929_in_is_true_for_any_name'() {
      // PR #8552 lists it: GraalJS answers 'x' in obj / hasOwnProperty / Reflect.has from hasMember, which now says true for any name of the parameter object
      // (13.8: false for a name that is not set). Scripts have to test a parameter with != null. This pins the behaviour as documented; if development
      // changes it, change this case with it.
      given:
      def t = new ScriptStateBase(TEXT_VS)

      when:
      t.setScript('Text1', "Text1.text = [('notThere' in parameter), parameter.hasOwnProperty('zzz999'), parameter.notThere != null].join('|')")

      then:
      t.eval('Text1.text') == 'true|true|false'
   }
}

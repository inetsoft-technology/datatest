package inetsoft.test.vsscript.state

import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Spike 2: state assertions on a Crosstab and a Chart, reusing the assets of the existing PNG cases
 * (Table_Spec 'TestCase-Crosstab_Common1', ChartBindingInfo_Spec 'TestCase-BandingField').
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateChartCrosstab_Spec extends Specification {
   static final String CROSSTAB_VS = '1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1'
   static final String CHART_VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   // Crosstab: property round trip (same properties the PNG case sets on Crosstab2)
   @Unroll
   def 'Crosstab_roundtrip_#prop'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      when:
      t.setScript('Crosstab2', "${prop} = ${literal}")

      then:
      t.eval("Crosstab2.${prop}") == expected

      where:
      prop           | literal           | expected
      'titleVisible' | 'false'           | false
      'wrapping'     | 'true'            | true
      'tableStyle'   | '"ReverseHeader"' | 'ReverseHeader'
   }

   // Crosstab: script reads data that the PNG case can only show by drawing it into a Text
   def 'Crosstab_data_readable'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      when:
      def rows = t.eval('Crosstab1.data.length')
      def cell = t.eval('Crosstab1.data[1][2]')
      def head = t.eval('Crosstab1.table[1][2]')
      println "SPIKE crosstab rows=${rows} data[1][2]=${cell} table[1][2]=${head}"

      then:
      rows instanceof Number
      rows > 1
      cell != null
   }

   // Crosstab: script that writes a Text from crosstab data, checked on the Text value (no PNG)
   def 'Crosstab_script_to_text'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      when:
      t.setScript('Text1', 'Text1.value = Crosstab1.table[1][2] + ", " + Crosstab1.data[1][2] + ", " + Crosstab1.data.length')
      def value = t.eval('Text1.value')
      println "SPIKE text=${value}"

      then:
      value instanceof String
      value.split(', ').size() == 3
      value.split(', ')[2] == t.eval('Crosstab1.data.length').toString().replaceAll(/\.0$/, '')
   }

   // Chart: bindingInfo set by script, read back from the scope
   def 'Chart_binding_roundtrip'() {
      given:
      def t = new ScriptStateBase(CHART_VS)

      when:
      t.setScript('Chart1',
         'query = "Query1";\n' +
         'bindingInfo.xFields = [["Region",Chart.STRING]];\n' +
         'bindingInfo.yFields = [["Paid",Chart.NUMBER]];\n' +
         'bindingInfo.setColorField("State",Chart.STRING);')
      def xs = t.eval('Chart1.bindingInfo.xFields')
      def ys = t.eval('Chart1.bindingInfo.yFields')
      // bindingInfo only has the setter; the readable runtime field is Chart1.colorField
      def color = t.eval('Chart1.colorField')
      println "SPIKE chart x=${xs} (${xs?.class}) y=${ys} color=${color} (${color?.class})"

      then:
      xs as List == ['Region']
      ys as List == ['Paid']
      color == 'State'
   }

   // Chart: simple property
   @Unroll
   def 'Chart_roundtrip_#prop'() {
      given:
      def t = new ScriptStateBase(CHART_VS)

      when:
      t.setScript('Chart1', "${prop} = ${literal}")

      then:
      t.eval("Chart1.${prop}") == expected

      where:
      prop             | literal | expected
      'titleVisible'   | 'false' | false
      'tooltipVisible' | 'false' | false
   }
}

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
 * Regression cases for chart-script bugs that are FIXED on stylebi main, end to end (real viewsheet, real script
 * engine, real paint), where the product unit tests use mocks or hand-built descriptors. The open bugs live in
 * ScriptStateKnownBugs_Spec.
 *
 * Bug #81032 (PR #8004, community 65dc2f1c6b), three items:
 *  1. boxplot SchemaElement with setBorderColor and no color frame threw NPE in BoxPainter.paint -> chart blank
 *  2. y2Axis properties were also written to the primary measure's axis on a separated two-measure chart
 *  3. setGroupOrder(field, Chart.NONE_DATE_GROUP) was ignored -> covered in ScriptStateKnownBugs_Spec (#80711)
 *
 * Not shown: that these cases fail on a build without the fix. No pre-fix build was available; the product unit tests
 * of the same PR cover that. Use the mutation check (guide section 6) before relying on them as proof.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateChartScriptFixes_Spec extends Specification {
   static final String CHART_VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'
   /** separated chart, two measures Sum(customer_id) and Sum(product_id), secondary axis descriptor present */
   static final String TWO_MEASURE_VS = '1^128^__NULL__^ScriptAuto/Chart/MultiTextFrame'

   static final String BOX_DATA =
      'dataset = [["State","high","upper","median","lower","low"],["NJ",100,80,60,40,20],["NY",120,90,70,50,30]];\n' +
      'graph = new EGraph();\n' +
      'var box = new SchemaElement(new BoxPainter());\n' +
      'box.addDim("State");\n' +
      'box.addSchema("high","upper","median","lower","low");\n'

   // ---- #81032 item 1 (#80985): boxplot border colour without a colour frame ----

   def 'boxplot control: without a border colour the scripted boxplot paints and uses no border colour'() {
      given:
      def t = new ScriptStateBase(CHART_VS)
      t.setScript('Chart1', BOX_DATA + 'graph.addElement(box);')

      when:
      def r = t.paint('Chart1')

      then: 'it paints, so a zero magenta count below means something'
      r.failure == null
      !r.pairFailed
      r.painted > 1000
      r.magenta == 0
   }

   def 'boxplot with a border colour and no colour frame paints, using the border colour'() {
      given:
      def t = new ScriptStateBase(CHART_VS)
      t.setScript('Chart1', BOX_DATA + 'box.setBorderColor(new java.awt.Color(0xff00ff));\ngraph.addElement(box);')

      when:
      def r = t.paint('Chart1')

      then: 'before the fix every paint threw NullPointerException and the chart was blank'
      r.failure == null
      !r.pairFailed
      r.painted > 1000
      r.magenta > 100   // measured 482 on 2.1.202611170332
   }

   // ---- #81032 item 2 (#80989): y2Axis must not write to the primary measures ----

   static List axisLineVisible(ScriptStateBase t) {
      def info = t.vs.getAssembly('Chart2').getVSChartInfo()
      (info.getRTYFields() as List).findAll { it.getClass().simpleName == 'VSChartAggregateRef' }
         .collect { it.getRTAxisDescriptor().isLineVisible() }
   }

   def 'y2Axis control: yAxis still writes the axis of every measure of a separated chart'() {
      given:
      def t = new ScriptStateBase(TWO_MEASURE_VS)

      expect: 'two measures, both visible to start with'
      axisLineVisible(t) == [true, true]

      when:
      t.scope.execute('yAxis.lineVisible = false', 'Chart2')

      then: 'the "global axis" behaviour of yAxis is unchanged by the y2 fix'
      axisLineVisible(t) == [false, false]
   }

   def 'y2Axis does not write the axes of the primary measures of a separated chart'() {
      given:
      def t = new ScriptStateBase(TWO_MEASURE_VS)

      expect:
      axisLineVisible(t) == [true, true]

      when:
      t.scope.execute('y2Axis.lineVisible = false', 'Chart2')

      then: 'before the fix this hid the axis line of every measure'
      axisLineVisible(t) == [true, true]
   }

   def 'y2Axis line colour does not leak to the primary measures either'() {
      // the colour variant of the #80989 repro
      given:
      def t = new ScriptStateBase(TWO_MEASURE_VS)
      def before = (t.vs.getAssembly('Chart2').getVSChartInfo().getRTYFields() as List)
         .findAll { it.getClass().simpleName == 'VSChartAggregateRef' }.collect { it.getRTAxisDescriptor().getLineColor() }

      when:
      t.scope.execute('y2Axis.lineColor = java.awt.Color.ORANGE', 'Chart2')
      def after = (t.vs.getAssembly('Chart2').getVSChartInfo().getRTYFields() as List)
         .findAll { it.getClass().simpleName == 'VSChartAggregateRef' }.collect { it.getRTAxisDescriptor().getLineColor() }

      then:
      before.size() == 2
      after == before
   }
}

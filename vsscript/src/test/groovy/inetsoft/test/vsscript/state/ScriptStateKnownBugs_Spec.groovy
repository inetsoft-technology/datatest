package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import inetsoft.web.binding.VSScriptableController
import org.springframework.test.context.ContextConfiguration
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * Chart/Crosstab/Gauge script bugs found by reading back state. Two kinds of cases live here:
 * <ul>
 *   <li><b>Open bugs</b>, three cases each: a control on the working path of the same call (so a failure cannot come
 *       from a wrong setup); a "[bug present]" case that asserts the exact failure seen today (it turns RED when the
 *       bug is fixed, or when the failure changes to something else); and a @PendingFeature case with the expected
 *       behavior (once the bug is fixed it "passes unexpectedly", which Spock reports as a failure). A pending case
 *       passes on ANY failure, so the two other cases are what tell "bug present" from "setup broken". When a bug is
 *       fixed: delete the "[bug present]" case and remove @PendingFeature.</li>
 *   <li><b>Regressions of fixed bugs</b>: #80708/#80709 and the Crosstab getters (fixed by #81530, PR #8301) and
 *       #80711 (fixed by #81032). The #81530 cases need an inetsoft-core that contains community f0aad94be6; on an
 *       older jar they fail.</li>
 * </ul>
 * Scripts run through scope.execute because the refresh path swallows script errors.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateKnownBugs_Spec extends Specification {
   static final String VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'

   static final String CROSSTAB_VS = '1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1'
   static final String CROSSTAB = 'NullCorsstab'   // sic: the assembly name in the asset

   static Object run(ScriptStateBase t, String script, String assembly = 'Chart1') {
      JavaScriptEngine.unwrap(t.scope.execute(script, assembly))
   }

   /** the date level of the first x field as the chart info holds it (null = not set) */
   static String dateLevel(ScriptStateBase t) {
      t.vs.getAssembly('Chart1').getVSChartInfo().getXFields()[0].getDateLevelValue()
   }

   static final String TOPN_REVERSE = 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)'
   static final String GET_GROUP_ORDER = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)'
   static final String SET_NONE = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.NONE_DATE_GROUP, Chart.BINDING_FIELD)'

   // ---- #80709: bindingInfo.isTopNReverse() threw NPE for a dimension with no ranking ----
   // Fixed in stylebi by #81530 (PR #8301, community f0aad94be6). These cases are now regressions and need an
   // inetsoft-core that contains that commit; on an older jar they fail (isTopNReverse throws).
   def 'control: getTopN of an unranked dimension is 0'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.getTopN("State", Chart.BINDING_FIELD)') == 0d
   }

   def '#80709 isTopNReverse of an unranked dimension is false'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, TOPN_REVERSE) == false
   }

   // ---- #80708: bindingInfo.getGroupOrder() threw "Cannot parse null string" before setGroupOrder ----
   // Fixed in stylebi by #81530 (PR #8301); regression, needs an inetsoft-core that contains f0aad94be6.
   def 'control: getGroupOrder works once a date level is set'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD); ' +
             'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)') == 5d
   }

   def '#80708 getGroupOrder of a date field returns the default YEAR level'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, GET_GROUP_ORDER) == 5d
   }

   // ---- #80711: setGroupOrder(field, Chart.NONE_DATE_GROUP) is silently ignored ----
   def 'control: setGroupOrder YEAR and MONTH take effect'() {
      given:
      def a = new ScriptStateBase(VS)
      def b = new ScriptStateBase(VS)

      when:
      run(a, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.YEAR_DATE_GROUP, Chart.BINDING_FIELD)')
      run(b, 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.MONTH_DATE_GROUP, Chart.BINDING_FIELD)')

      then:
      dateLevel(a) == '5'
      dateLevel(b) == '3'
   }

   // Fixed in stylebi by #81032 (PR #8004, community 65dc2f1c6b); the [bug present] case went red on a build that
   // contains it and the pending case passed unexpectedly, so both were replaced by this regression case.
   def '#80711 setGroupOrder NONE_DATE_GROUP sets the date level to 0'() {
      given:
      def t = new ScriptStateBase(VS)

      when:
      run(t, SET_NONE)

      then:
      dateLevel(t) == '0'
   }

   // ---- Crosstab siblings of #80708/#80709, and #78585 item 2 (chart type 0) ----
   // #80708/#80709 were fixed on the 13.8 line only and #78585 on the wiz line only; filed for stylebi as #81530 (the
   // throwing getters; crosstab getTopN had no issue at all) and #81531 (chart isTopNReverse with type 0), both also
   // reproduced live in the Composer on 2.1.202611170332. #81530 was fixed in stylebi the same day (PR #8301, community
   // f0aad94be6): the crosstab getters and the two chart cases above are regressions now (they need an inetsoft-core
   // that contains that commit). #81531 is still open and stays below as [bug present] / pending.
   static final String CT_SETUP = 'bindingInfo.rowFields = ["state"]; bindingInfo.colFields = ["orderdate"]; bindingInfo.measureFields = ["customer_id"]; '
   static final String CT_TOPN = CT_SETUP + 'bindingInfo.getTopN("state", StyleConstant.ROW_HEADER)'
   static final String CT_TOPN_REVERSE = CT_SETUP + 'bindingInfo.isTopNReverse("state", StyleConstant.ROW_HEADER)'
   static final String CT_GROUP_ORDER = CT_SETUP + 'bindingInfo.getGroupOrder("orderdate", StyleConstant.COL_HEADER)'
   static final String CHART_TYPE0 = 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.setTopN("State", 3, Chart.BINDING_FIELD); ' +
                                     'bindingInfo.setTopNReverse("State", true, Chart.BINDING_FIELD); bindingInfo.isTopNReverse("State", 0)'

   def 'control: crosstab getTopN and isTopNReverse work once ranking is set'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)
      def set = CT_SETUP + 'bindingInfo.setTopN("state", StyleConstant.ROW_HEADER, 3); bindingInfo.setTopNReverse("state", StyleConstant.ROW_HEADER, true); '

      expect:
      run(t, set + 'bindingInfo.getTopN("state", StyleConstant.ROW_HEADER)', CROSSTAB) == 3d
      run(t, set + 'bindingInfo.isTopNReverse("state", StyleConstant.ROW_HEADER)', CROSSTAB) == true
   }

   def 'crosstab getTopN of an unranked field is 0'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_TOPN, CROSSTAB) == 0d
   }

   def 'crosstab isTopNReverse of an unranked field is false'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_TOPN_REVERSE, CROSSTAB) == false
   }

   def 'crosstab getGroupOrder of a date field with no level set returns the default YEAR level'() {
      given:
      def t = new ScriptStateBase(CROSSTAB_VS)

      expect:
      run(t, CT_GROUP_ORDER, CROSSTAB) == 5d
   }

   def 'control: chart isTopNReverse with BINDING_FIELD and getTopN with type 0 see the ranking'() {
      given:
      def t = new ScriptStateBase(VS)
      def set = 'bindingInfo.xFields = [["State", Chart.STRING]]; bindingInfo.setTopN("State", 3, Chart.BINDING_FIELD); bindingInfo.setTopNReverse("State", true, Chart.BINDING_FIELD); '

      expect:
      run(t, set + 'bindingInfo.isTopNReverse("State", Chart.BINDING_FIELD)') == true
      run(t, set + 'bindingInfo.getTopN("State", 0)') == 3d
   }

   def '#78585 [bug present] chart isTopNReverse with type 0 returns false although the chart is bottom-N'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, CHART_TYPE0) == false
   }

   @PendingFeature(reason = 'Redmine #78585 item 2: isTopNReverse(field, 0) has no fallback branch (AbstractChartBindingScriptable:1576) while setTopNReverse/getTopN resolve type 0; fixed on the wiz line only; stylebi: #81531')
   def '#78585 chart isTopNReverse with type 0 matches the BINDING_FIELD read'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, CHART_TYPE0) == true
   }

   // ---- #81033: every constant the script editor lists for a chart script must be defined ----
   // Fixed on 13.8 only (style-intelligence PR #2071 adds FIRST_FORMULA / LAST_FORMULA to ReportChartConstants); no stylebi
   // counterpart as of 2026-11-17; filed as #81534. DASH_MASK and WIDTH_MASK are undefined too and are not in #81033;
   // both are defined on StyleConstant (240, 15), and SOLID_MASK of the same interface is defined on Chart.
   // The names come from the editor's own lists (VSScriptableController), so the sweep follows the product.
   static String typeOf(ScriptStateBase t, String expr) {
      String.valueOf(JavaScriptEngine.unwrap(t.scope.execute("typeof (${expr})", 'Chart1')))
   }

   static List<String> undefinedChartConstants(ScriptStateBase t) {
      VSScriptableController.CHART_CONSTANTS.findAll { typeOf(t, "Chart['${it}']") == 'undefined' }.sort()
   }

   def 'control: the constants that work, and the ones StyleConstant has'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      typeOf(t, 'Chart.SUM_FORMULA') == 'string'
      typeOf(t, 'Chart.SOLID_MASK') == 'number'
      typeOf(t, 'StyleConstant.FIRST_FORMULA') == 'string'
      typeOf(t, 'StyleConstant.LAST_FORMULA') == 'string'
      run(t, 'StyleConstant.DASH_MASK') == 240d
      run(t, 'StyleConstant.WIDTH_MASK') == 15d
   }

   def 'every class the script editor lists for a chart script is defined'() {
      given:
      def t = new ScriptStateBase(VS)

      expect: 'a guard for the future: today all of them are defined'
      VSScriptableController.CHART_CLASSES.size() > 100
      VSScriptableController.CHART_CLASSES.findAll { typeOf(t, it) == 'undefined' } == []
   }

   def '#81033 [bug present] four editor-listed Chart constants are undefined'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      undefinedChartConstants(t) == ['DASH_MASK', 'FIRST_FORMULA', 'LAST_FORMULA', 'WIDTH_MASK']
   }

   @PendingFeature(reason = 'Redmine #81033 (13.8 only; FIRST_FORMULA, LAST_FORMULA) plus DASH_MASK and WIDTH_MASK: constants the editor lists under Chart are undefined in a chart script; stylebi: #81534')
   def '#81033 every Chart constant listed in the script editor is defined'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      undefinedChartConstants(t) == []
   }

   // ---- #81540: the xAxis getter reads the Y axis descriptor ----
   // ChartVSAScriptable builds xAxis and yAxis from the same vinfo.getRTAxisDescriptor() (since the initial commit), but
   // xAxis writes go to the descriptors of the X fields. So the writes work (painted) while xAxis.<prop> reads the Y axis.
   // yAxis/y2Axis do not read back on a SEPARATED chart either; that comes from the per-measure write fan-out of
   // bug1335152670042 and is not pinned here. Reachable workaround: axis['<field>'] reads back its own write.
   static final String NONSEP_VS = '1^128^__NULL__^ScriptAuto/Chart/PlotProperty2'   // Chart2: not separated, x field Quarter(Date)
   static final String MAGENTA_COLOR = 'java.awt.Color[r=255,g=0,b=255]'
   static final String DEFAULT_AXIS_COLOR = 'java.awt.Color[r=238,g=238,b=238]'
   static final String SET_MAGENTA = 'new java.awt.Color(0xff00ff)'

   def 'control: xAxis writes are painted, and yAxis and the per-field axis read back their own writes'() {
      given:
      def painted = new ScriptStateBase(NONSEP_VS)
      painted.setScript('Chart2', "xAxis.lineColor = ${SET_MAGENTA}")
      def y = new ScriptStateBase(NONSEP_VS)
      def f = new ScriptStateBase(NONSEP_VS)

      expect: 'the xAxis write changes what is painted (716 magenta pixels measured)'
      painted.paint('Chart2').magenta > 100
      and:
      run(y, "yAxis.lineColor = ${SET_MAGENTA}; '' + yAxis.lineColor", 'Chart2') == MAGENTA_COLOR
      run(f, "axis['Quarter(Date)'].lineColor = ${SET_MAGENTA}; '' + axis['Quarter(Date)'].lineColor", 'Chart2') == MAGENTA_COLOR
   }

   def '[bug present] xAxis reads the Y axis: it shows the yAxis write and not its own'() {
      given:
      def a = new ScriptStateBase(NONSEP_VS)
      def b = new ScriptStateBase(NONSEP_VS)

      expect: 'only yAxis was written, xAxis reports the colour'
      run(a, "yAxis.lineColor = ${SET_MAGENTA}; '' + xAxis.lineColor", 'Chart2') == MAGENTA_COLOR
      and: 'xAxis was written (and is painted), yet it reads the default'
      run(b, "xAxis.lineColor = ${SET_MAGENTA}; '' + xAxis.lineColor", 'Chart2') == DEFAULT_AXIS_COLOR
   }

   @PendingFeature(reason = 'Redmine #81540: xAxis reads the Y axis descriptor (ChartVSAScriptable:354 uses getRTAxisDescriptor() for xAxis and yAxis; xAxis writes go to the X fields)')
   def 'xAxis reads back its own write and is not changed by a yAxis write'() {
      given:
      def a = new ScriptStateBase(NONSEP_VS)
      def b = new ScriptStateBase(NONSEP_VS)

      expect:
      run(b, "xAxis.lineColor = ${SET_MAGENTA}; '' + xAxis.lineColor", 'Chart2') == MAGENTA_COLOR
      run(a, "yAxis.lineColor = ${SET_MAGENTA}; '' + xAxis.lineColor", 'Chart2') == DEFAULT_AXIS_COLOR
   }

   // ---- #81531 (note of 2026-11-17): getGroupOrder(field, 0), same asymmetry as isTopNReverse (#78585 item 2; the wiz-line fix changed both) ----
   static final String SET_MONTH = 'bindingInfo.xFields = [["Date", Chart.DATE]]; bindingInfo.setGroupOrder("Date", Chart.MONTH_DATE_GROUP, Chart.BINDING_FIELD); '

   def 'control: getGroupOrder with BINDING_FIELD returns the level that was set'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, SET_MONTH + 'bindingInfo.getGroupOrder("Date", Chart.BINDING_FIELD)') == 3d
   }

   def '[bug present] getGroupOrder with type 0 returns the default YEAR level instead of the level that was set'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, SET_MONTH + 'bindingInfo.getGroupOrder("Date", 0)') == 5d
   }

   @PendingFeature(reason = 'Redmine #81531 (getGroupOrder part): getGroupOrder(field, 0) has no fallback branch (AbstractChartBindingScriptable:1854) while setGroupOrder resolves it; fixed on the wiz line only (#78585 item 2)')
   def 'getGroupOrder with type 0 matches the BINDING_FIELD read'() {
      given:
      def t = new ScriptStateBase(VS)

      expect:
      run(t, SET_MONTH + 'bindingInfo.getGroupOrder("Date", 0)') == 3d
   }

   // ---- #81550: Gauge: a script that writes more ranges than the design has leaves placeholder "0" entries in the design getter ----
   // RangeOutputVSAssemblyInfo.setRanges grows the backing array with DynamicValue("0"); getRangeValues() returns all of
   // it, not the first rangeDesignCount entries (which is what is saved). A new Gauge has 3 design ranges (35, 70, 100).
   // The Gauge Properties dialog fills its five slots from getRangeValues(), so it shows 0 in slots 4 and 5, and OK
   // stores them (GaugePropertyDialogService, getRangeValues at ~174, setRangeValues at ~323). That dialog behaviour was
   // checked against the real service while #81550 was written; it is not pinned here, because the service constructor
   // has seven dependencies and would tie this spec to its signature. Wiz line: fixed (#78585 item 1, stylebi PR #6913).
   static final String GAUGE_VS = '1^128^__NULL__^Manual/Chart/TestCase-ChartTipView'   // Gauge1: 3 design ranges

   static List gaugeDesignRanges(ScriptStateBase t) {
      t.vs.getAssembly('Gauge1').getVSAssemblyInfo().getRangeValues().toList()
   }

   def 'control: a script that writes no more ranges than designed leaves the Gauge design ranges as designed'() {
      given:
      def t = new ScriptStateBase(GAUGE_VS)

      expect:
      gaugeDesignRanges(t) == ['35', '70', '100']

      when:
      run(t, 'ranges = [30, 60, 90]; 1', 'Gauge1')

      then:
      gaugeDesignRanges(t) == ['35', '70', '100']
   }

   def '[bug present] Gauge getRangeValues shows placeholder 0 entries after a script wrote five ranges'() {
      given:
      def t = new ScriptStateBase(GAUGE_VS)

      when:
      run(t, 'ranges = [30, 60, 90, "", ""]; 1', 'Gauge1')

      then:
      gaugeDesignRanges(t) == ['35', '70', '100', '0', '0']
   }

   @PendingFeature(reason = 'Redmine #81550: RangeOutputVSAssemblyInfo.getRangeValues returns the array grown by a script write (placeholder "0" entries) instead of the rangeDesignCount design entries; the Gauge dialog then stores the 0 on OK; fixed on the wiz line (#78585 item 1)')
   def 'Gauge getRangeValues returns only the design ranges after a script wrote five ranges'() {
      given:
      def t = new ScriptStateBase(GAUGE_VS)

      when:
      run(t, 'ranges = [30, 60, 90, "", ""]; 1', 'Gauge1')

      then:
      gaugeDesignRanges(t) == ['35', '70', '100']
   }
}

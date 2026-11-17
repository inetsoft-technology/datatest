package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.util.script.JavaScriptEngine
import inetsoft.util.script.ScriptException
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBES for hunting script bugs (not part of the suite: no _Spec suffix, they only print PROBE BH lines).
 * Each call is run directly (scope.execute) because the refresh path swallows script errors. What they found is
 * pinned in the specs (ScriptStateKnownBugs_Spec, ScriptStateChartScriptFixes_Spec); these three stay because they
 * are the way to find more:
 * <ul>
 *   <li>BH3: every name the script editor lists for a chart script must be defined in the script</li>
 *   <li>BH5: which charts in the assets have two or more measures</li>
 *   <li>BH10: write and read back every global axis object (xAxis, yAxis, y2Axis) on a separated and a plain chart</li>
 * </ul>
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateBugHuntProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 * BH3 needs -Dprobe.lists=<json file with the arrays CHART_CONSTANTS and CHART_CLASSES of VSScriptableController>.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateBugHuntProbe extends Specification {
   static final String CHART_VS = '1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo'
   static final String SEP_VS = '1^128^__NULL__^ScriptAuto/Chart/MultiTextFrame'       // separated, 2 measures
   static final String NONSEP_VS = '1^128^__NULL__^ScriptAuto/Chart/PlotProperty2'     // Chart2: not separated, 2 measures

   static void probe(String msg) { println "PROBE BH ${msg}" }

   /** run script on an assembly; return "OK <value>" or "EX <innermost message>" */
   static String call(ScriptStateBase t, String assembly, String script) {
      try {
         def v = JavaScriptEngine.unwrap(t.scope.execute(script, assembly))
         return "OK ${v} (${v?.class?.simpleName})"
      }
      catch(ScriptException e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) c = c.cause
         return "EX ${c.class.simpleName}: ${c.message}"
      }
   }


   /**
    * Port-gap sweep for #81033 / #79685: every name the script editor lists for a chart script
    * (VSScriptableController.CHART_CONSTANTS / CHART_CLASSES, parsed from origin/main into a json file passed with
    * -Dprobe.lists=<file>) must be defined in the script. Prints the undefined ones.
    */
   def 'BH3_editor_listed_names_are_defined'() {
      expect:
      def file = System.getProperty('probe.lists')
      if(!file) { probe "BH3 skipped: pass -Dprobe.lists=<editor_lists.json>" }
      def lists = file ? new groovy.json.JsonSlurper().parse(new File(file)) : [CHART_CONSTANTS: [], CHART_CLASSES: []]
      def t = new ScriptStateBase(CHART_VS)

      def typeOf = { String expr ->
         try { return String.valueOf(JavaScriptEngine.unwrap(t.scope.execute("typeof (${expr})", 'Chart1'))) }
         catch(Throwable e) { return "EX(${e.class.simpleName})" }
      }

      def constUndefined = [], constNotInStyleConstant = [], constEx = []
      lists.CHART_CONSTANTS.each { name ->
         def r = typeOf("Chart['${name}']")
         if(r == 'undefined') constUndefined << name
         else if(r.startsWith('EX')) constEx << "${name}:${r}"
         else if(typeOf("StyleConstant['${name}']") == 'undefined') constNotInStyleConstant << name
      }
      probe "BH3 CHART_CONSTANTS total=${lists.CHART_CONSTANTS.size()} undefined-in-Chart=${constUndefined.size()} ${constUndefined}"
      probe "BH3 CHART_CONSTANTS errors=${constEx}"
      probe "BH3 CHART_CONSTANTS defined in Chart but not in StyleConstant (${constNotInStyleConstant.size()}): ${constNotInStyleConstant}"

      def clsUndefined = [], clsEx = []
      lists.CHART_CLASSES.each { name ->
         def r = typeOf(name)
         if(r == 'undefined') clsUndefined << name
         else if(r.startsWith('EX')) clsEx << "${name}:${r}"
      }
      probe "BH3 CHART_CLASSES total=${lists.CHART_CLASSES.size()} undefined=${clsUndefined.size()} ${clsUndefined}"
      probe "BH3 CHART_CLASSES errors=${clsEx}"

      // direct checks for the two reports
      probe "BH3 #81033 typeof Chart.FIRST_FORMULA=${typeOf('Chart.FIRST_FORMULA')} LAST_FORMULA=${typeOf('Chart.LAST_FORMULA')} SUM_FORMULA=${typeOf('Chart.SUM_FORMULA')}"
      probe "BH3 #79685 typeof StyleConstant['MAP_TYPE_U.S.']=${typeOf("StyleConstant['MAP_TYPE_U.S.']")} Chart['MAP_TYPE_U.S.']=${typeOf("Chart['MAP_TYPE_U.S.']")}"
      true
   }

   /** which charts in the assets have >= 2 measures in the runtime y fields (needed for the y2Axis leak check)? */
   def 'BH5_find_two_measure_charts'() {
      expect:
      def found = 0
      ScriptStateBase.viewsheetIds().each { id ->
         try {
            def t = new ScriptStateBase(id)
            t.vs.getAssemblies().each { a ->
               if(a.getClass().simpleName != 'ChartVSAssembly') return
               def info = a.getVSChartInfo()
               def rty = info.getRTYFields() as List
               def meas = rty.findAll { it.getClass().simpleName == 'VSChartAggregateRef' }
               def sec = meas.count { it.isSecondaryY() }
               if(meas.size() >= 2) {
                  found++
                  probe "BH5 vs=${id} chart=${a.getName()} separated=${info.isSeparatedGraph()} measures=${meas.collect { it.getFullName() }} secondaryY=${sec} rtAxis2=${info.getRTAxisDescriptor2() != null}"
               }
            }
         }
         catch(Throwable e) { /* open failure is reported by the health probe */ }
      }
      probe "BH5 summary charts with >=2 measures: ${found}"
      true
   }

   /**
    * Axis property read-back: write and read in ONE execution, then read in the NEXT execution, for the three global
    * axis objects on a separated and on a non-separated chart. Also how a per-field axis can be reached.
    */
   def 'BH10_axis_readback_matrix'() {
      when:
      [[SEP_VS, 'separated'], [NONSEP_VS, 'non-separated']].each { vsId, label ->
         ['xAxis', 'yAxis', 'y2Axis'].each { obj ->
            ['lineVisible = false', 'lineColor = new java.awt.Color(0xff00ff)'].each { assign ->
               def prop = assign.split(' ')[0]
               def t = new ScriptStateBase(vsId)
               def same = ScriptStateBugHuntProbe.call(t, 'Chart2', "${obj}.${assign}; '' + ${obj}.${prop}")
               def next = ScriptStateBugHuntProbe.call(t, 'Chart2', "'' + ${obj}.${prop}")
               probe "BH10 ${label} ${obj}.${assign}: same-execution read=${same.replaceAll(/ \(String\)/, '')} next-execution read=${next.replaceAll(/ \(String\)/, '')}"
            }
         }
      }
      def t = new ScriptStateBase(SEP_VS)
      probe "BH10 per-field axis: typeof axis=${ScriptStateBugHuntProbe.call(t, 'Chart2', 'typeof axis')} axis['Sum(customer_id)']=${ScriptStateBugHuntProbe.call(t, 'Chart2', "typeof axis['Sum(customer_id)']")} " +
            "Chart2.axis=${ScriptStateBugHuntProbe.call(t, 'Chart2', "'' + axis")}"

      then:
      true
   }
}

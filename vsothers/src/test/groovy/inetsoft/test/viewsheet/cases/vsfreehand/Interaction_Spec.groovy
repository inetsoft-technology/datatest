package inetsoft.test.viewsheet.cases.vsfreehand

import inetsoft.report.TableLens
import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.test.modules.VSCalcTest
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

/**
 * G6: an interaction after the viewsheet is open. Brushing the chart filters the crosstab of the
 * same data source (a DataVSAssembly depends on the brushing chart), and the freehand tables bound
 * to that crosstab must follow it, and come back when the brush is cleared. No baseline files: the
 * expected values are read from the crosstab in the same state, and the state before the brush
 * must come back after it is cleared.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class Interaction_Spec extends Specification {
   static final String BRUSH = 'Customer:State^VALUE:NJ'
   static final List<String> COMPONENTS = ['Crosstab1', 'FreehandTable1', 'FreehandTable2']

   def setupSpec() {
      VSCalcTest.initHome(this.class.getName())
   }

   /** the cells of every row of the component, as text */
   static List<List<String>> dump(ViewsheetSandbox box, String name) {
      TableLens lens = box.getVSTableLens(name, false)
      lens.moreRows(Integer.MAX_VALUE)
      List<List<String>> rows = []

      for(int r = 0; r < lens.getRowCount(); r++) {
         List<String> cells = []

         for(int c = 0; c < lens.getColCount(); c++) {
            Object v = lens.getObject(r, c)
            cells << (v == null ? 'NULL' : v.toString())
         }

         rows << cells
      }

      return rows
   }

   static Map<String, List<List<String>>> dumpAll(VSCalcTest test) {
      RuntimeViewsheet rvs = test.viewsheetResource.getRuntimeViewsheet(test.principal)
      ViewsheetSandbox box = rvs.getViewsheetSandbox().get()
      Map<String, List<List<String>>> out = [:]
      COMPONENTS.each { out[it] = dump(box, it) }
      return out
   }

   static void brush(VSCalcTest test, String selected) {
      VSChartBrushEvent event = new VSChartBrushEvent()
      event.setChartName('Chart1')
      event.setRangeSelection(false)
      event.setSelected(selected)
      test.viewsheetResource.brushOnChart(event, test.principal)
   }

   /** the data before the brush, with the brush, and after the brush is cleared */
   static Map<String, Map<String, List<List<String>>>> brushRoundTrip(String vs) {
      VSCalcTest test = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/' + vs, 'brush_' + vs)
      test.initVS(null, true)
      Map<String, Map<String, List<List<String>>>> states = [:]
      states.before = dumpAll(test)
      brush(test, BRUSH)
      states.brushed = dumpAll(test)
      brush(test, null)
      states.cleared = dumpAll(test)
      return states
   }

   static boolean hasNumber(List<List<String>> rows, double value) {
      return rows.any { row ->
         row.any { cell ->
            try {
               return Double.parseDouble(cell) == value
            }
            catch(NumberFormatException ignore) {
               return false
            }
         }
      }
   }

   /** the first value column of the crosstab row of the state, in the brushed state */
   static double paidOf(List<List<String>> crosstab, String state) {
      List<String> row = crosstab.drop(2).find { it[1] == state }
      return Double.parseDouble(row[2])
   }

   /**
    * freehand bound to the crosstab, a group per region: brush NJ, the crosstab keeps one row, the
    * freehand tables show the Paid of that row and no other region, the brush cleared restores all
    */
   def 'CM11_1_1_BrushOnChart'() {
      def states = brushRoundTrip('CM11_1_1')
      def before = states.before
      def brushed = states.brushed
      def cleared = states.cleared
      double paid = paidOf(brushed.Crosstab1, 'NJ')

      expect:
      verifyAll {
         brushed.Crosstab1.drop(2).size() == 1
         brushed.Crosstab1.drop(2)[0][1] == 'NJ'
         COMPONENTS.every { before[it] != brushed[it] }
         COMPONENTS.every { cleared[it] == before[it] }

         ['FreehandTable1', 'FreehandTable2'].every { n ->
            hasNumber(brushed[n], paid) && !brushed[n].any { row -> row.contains('USA West') } &&
               before[n].any { row -> row.contains('USA West') }
         }
      }
   }

   /**
    * freehand formula script with a name group, one block per state: brush NJ, the freehand tables
    * show NJ and its Paid only, and come back when the brush is cleared
    */
   def 'CM11_1_2_BrushOnChart'() {
      def states = brushRoundTrip('CM11_1_2')
      def before = states.before
      def brushed = states.brushed
      def cleared = states.cleared
      double paid = paidOf(brushed.Crosstab1, 'NJ')

      expect:
      verifyAll {
         brushed.Crosstab1.drop(2).size() == 1
         COMPONENTS.every { before[it] != brushed[it] }
         COMPONENTS.every { cleared[it] == before[it] }

         ['FreehandTable1', 'FreehandTable2'].every { n ->
            hasNumber(brushed[n], paid) && brushed[n].any { row -> row.contains('NJ') } &&
               !brushed[n].any { row -> row.contains('CT') || row.contains('USA West') } &&
               before[n].any { row -> row.contains('CT') }
         }
      }
   }
}

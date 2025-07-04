package inetsoft.test.cases.Properties

import inetsoft.test.modules.ViewsheetTest
import spock.lang.IgnoreRest
import spock.lang.Specification

/*
  check chart properties
 */
class ChartProperties_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    1.check project forward line for date range & date part, bk check different date level
    2.day of month exist bug(Bug #51033), status is ignore
    3.don't check time type, will report a bug in v13.5(Not sure if the support is that fine)
    4.need manual check show data of project forward
   */
   def 'projectforward1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/projectforward1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check project forward line on multiple style and check show data, variable check different date level
    2.exist Bug #51059, so image is not full
   */
   def 'projectforward2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/projectforward2', caseName)
      vstest.executeVS(null, ['(Home)','variable'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    1.check special project forward apply on discontinuous & num & negative & topn), bk check drill down
    2.Bug #51045:when quarter is not continuous,formward can work, ignore
   */
   def 'projectforward3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/projectforward3', caseName)
      vstest.exportAsPNG(null, null)
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }


   /*
    1.check project forward apply when exist null value
    2.check change from previous and null valeu when exist project forward
   */
   def 'projectforward4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/projectforward4', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check stack measure on date and exist project forward & discrete & running total & value format
    2. covered stack bar & stack 3d bar
    3.brush & drill filter on the first chart
   */
   def 'stackmeasures1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasures1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush', 'drillfilter'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check stack measure on different chart style and fact chart.
    2.brush check stack chart brush_state in the first chart & drillfilter_20201 in the last chart
   */
   def 'stackmeasures2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasures2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush', 'drillfilter'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check new chart and stack chart brush relation.
    2.covered change from previous & second y & special shape.
    3.covered brush_CA and drillfilter_year on the last chart
   */
   def 'stackmeasures3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasures3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush', 'drillfilter'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check stack measure & secondary y /show points relation， exist Bug #52453
   */
   def 'stackmeasures4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasures4', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check stack measure legend(include some legend properties) display&brush&zoom
   */
   def 'stackmeasure_DimLegend'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasure_DimLegend', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'zoom1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check stack measure legend(include some legend properties) display&brush&zoom
   */
   def 'stackmeasure_MeaLgend'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasure_MeaLgend', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'zoom1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check stack measure for multistyle,struct:binding D & M for different measure
   */
   def 'stackmeasure_multistyle1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasure_multistyle1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush', 'drillfilter'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check stack measure for multistyle,struct:binding D & M for different measure
   */
   def 'stackmeasure_multistyle2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackmeasure_multistyle2', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.check fill time-series gaps on normal chart & fact chart
    2.covered chart style and running total.
    Note:chart3's  prent of subtotal col be lost in exported text exist issue, ignore.
   */
   def 'Fill Time-Series Gaps1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/Fill Time-Series Gaps1', caseName)
      vstest.executeVS(null, ['(Home)','brush'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    1.check fill time-series gaps on multiple style.
    2.covered binding col in all visual pane and binding different col in visual pane
   */
   def 'Fill Time-Series Gaps2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/Fill Time-Series Gaps2', caseName)
      vstest.executeVS(null, ['(Home)','filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    1.check fill time-series gaps on multiple style. but exist special case. eg:chart2, fill timer-series gaps as zero can't work for line, ignore the issue. please see Bug #53702
   */
   def 'Fill Time-Series Gaps3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/Fill Time-Series Gaps3', caseName)
      vstest.executeVS(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check axis properties
   */
   def 'axis properties'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/axis properties', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check stack value on different chart and relation properties
   */
   def 'stackvalue1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackvalue1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
   check stack value & show point relation
   */
   def 'stackvalue2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackvalue2', caseName)
      vstest.exportAsPNG(null,['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }


   /*
   check stack value & show point & running total relation
   */
   def 'stackvalue3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/stackvalue3', caseName)
      vstest.exportAsPNG(null,['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
   check show lines & style
   */
   def 'showlinepoint'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/showlinepoint', caseName)
      vstest.exportAsPNG(null,['(Home)', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
   check show lines & style & properties relation
   */
   def 'showlinepoint2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/showlinepoint2', caseName)
      vstest.exportAsPNG(null,['(Home)', 'bk1'] as String[])
      //vstest.exportAsPDF(null,['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check sort others last for some chart type
   */
   def 'sort others last1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/sort others last1', caseName)
      vstest.exportAsPNG(null,['(Home)', 'bk1'] as String[])
      vstest.exportAsPDF(null,['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }


   /*
   check sort others last for some specail chart type
   */
   def 'sort others last2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/sort others last2', caseName)
      vstest.exportAsPNG(null,['(Home)','brush','filter', 'sortothers'] as String[])
      //vstest.exportAsPDF(null,['(Home)','sortothers'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check sort others last for some specail chart type
   */
   def 'topnpergroup'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/topnpergroup', caseName)
      vstest.exportAsPNG(null,['(Home)','bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check some  axis color for special case
   */
   def 'special_axiscolor'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/special_axiscolor', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage(null)
   }
}
package inetsoft.test.cases.Brush

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

/*
 check Chart Relation with Chart, those action include: brush, zoom, exclude
 */
class ChartBrush_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }
   /*
    check chart relation, lcicle -> lcicle
    */

   def 'RM3_1_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2','drillfilter','exclude1', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check chart relation, lcicle -> sunburst
    */
   def 'RM3_1_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2','drillfilter', 'exclude1', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check chart relation, lcicle -> face bar chart
    */
   def 'RM3_1_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude1', 'zoom'] as String[])
       

      expect:
      vstest.compareImage(null)
   } 


   /*
    check chart relation, face bar chart -> pie chart
    */
   def 'RM3_1_4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_4', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'drillup'] as String[])
       

      expect:
      vstest.compareImage(null)
   } 

   /*
    check chart relation, face bar chart -> tree map
    */
   def 'RM3_1_5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_5', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
    check chart relation, face bar chart -> face bar chart with stack -> water fall
    */
   def 'RM3_1_6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_6', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2','drillfilter', 'exclude' ] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
   check chart relation, dount pie chart -> line chart
   Note:chart2's  prent of subtotal col be lost in exported text exist issue, ignore.
   */
   def 'RM3_1_7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_7', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'Filter','brush1', 'brush2'] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
  check chart relation, multi style chart -> radar chart
  */
   def 'RM3_1_8'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_8', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude'] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
   check chart relation, multi style chart -> sunbust chart
   */
   def 'RM3_1_9'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_9', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude', 'zoom1'] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
   check chart relation, cicle chart -> cicle chart
   */
   def 'RM3_1_10'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_10', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'drillflter'] as String[])

      expect:
      vstest.compareImage(null)
   } 

   /*
   check chart relation, cicle chart -> cicle chart
   */
   def 'RM3_1_11'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_11', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2'] as String[])

      expect:
      vstest.compareImage(null)
   }
   /*
   check chart relation, multi style chart -> mekko chart
   Note:chart3's  prent of subtotal col be lost in exported text exist issue, ignore.
   */
   def 'RM3_1_12'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_12', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'drillup'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, boxplot chart -> treemap chart
   */
   def 'RM3_1_13'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_13', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'drillfilter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, face area chart -> treemap chart  by selectiontree.drillmembers
   */
   def 'RM3_1_14'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_14', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'filter'] as String[])

      expect:
      vstest.compareImage(null)
   }
   /*
   check chart relation, radar chart -> sunburst chart
   */

   def 'RM3_1_15'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_15', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter','brush1', 'drillfilter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, tree map chart -> sunburst chart
   */
   def 'RM3_1_16'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_16', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'drillfilter','exclude'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, tree map chart -> lcicle chart by drills members
   */
   def 'RM3_1_17'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_17', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'drillfilter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, stock chart -> multi style chart
   */
   def 'RM3_1_18'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_18', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'exclude', 'filter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
  check chart relation, sunburst -> cicle chart
  */
   def 'RM3_1_19'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_19', caseName)
      vstest.exportAsPNG(null, ['(Home)','adhocfilter', 'brush1', 'brush2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, sunburst -> lcicle chart by drillmembers
   */
   def 'RM3_1_20'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_20', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'exclude', 'filter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, mekko -> sunburst chart
   */
   def 'RM3_1_21'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_21', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'brush3','drilldown'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, mekko -> boxblot chart, drillmembers
   */
   def 'RM3_1_22'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_22', caseName)
      vstest.exportAsPNG(null, ['(Home)','adhocfilter','brush1', 'brush2', 'brush3','drillfilter'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, mekko -> boxblot chart, dynimac value
   */
   def 'RM3_1_23'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_23', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2','drill'] as String[])

      expect:
      vstest.compareImage(null)
   }
   /*
   check chart relation, mekko -> boxblot chart, component binding value
   */
   def 'RM3_1_24'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_24', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2','exclude'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, region map -> lcicle chart
   */
   def 'RM3_1_25'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_25', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'drilldown','exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }
   /*
   check chart relation, lang/sta map -> face point chart
   */
   def 'RM3_1_26'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_26', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush','exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }

  def 'RM3_1_27'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_27', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1','exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
     check chart binding crosstab, grand total show, crosstab have date drill, boxplot -> circle
    */
   def 'RM3_1_28'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_28', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
     check chart binding crosstab, summary side by side as false, crosstab have custom drill, treemap -> circle
    */
   def 'RM3_1_29'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_29', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush', 'exclude'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
     check chart binding freehand, freehand have topN, lcicle -> circle
    */
   def 'RM3_1_30'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_30', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude', 'zoom'] as String[])

      expect:
      vstest.compareImage(null)
   }
   /*
     check chart binding freehand which convert by crosstab, boxplot -> sunburst
    */
   def 'RM3_1_31'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_31', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
     check chart binding freehand which create by formula script, face bar -> mekko,pie
    */
   def 'RM3_1_32'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM3_1_32', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'brush2', 'exclude'] as String[])

      expect:
      vstest.compareImage(null)
   }


   /*
   check chart relation, radar -> cloud
   */
   def 'RM_Radar1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM_Radar1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'zoom1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart relation, radar-> histogram chart
   */
   def 'RM_Radar2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Chart/RM_Radar2', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      vstest.compareImage(null)
   }
}

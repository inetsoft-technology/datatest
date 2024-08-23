package inetsoft.test.viewsheet.cases.vscalc

import inetsoft.test.modules.ViewsheetTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class VS_CalcField_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check calc field used in filter assembly
   */
   def 'filter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/filter', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field used in parent-child tree
   */
   def 'parent-child tree'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/parent-child tree', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check parameter used in calc field
   */
   def 'parameter in calc field'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/parameter in calc field', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1', 'bk2'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field used in chart binding
   */
   def 'chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field in multiple assembly
   */
   def 'calc field1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/calc field1', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field in funnel chart(x,y,visual pane)
   */
   def 'funnel chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/funnel chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field in gannt chart(start,end,vactual，visual pane)
   */
   def 'gantt chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/gantt chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field in T pane chart(T pane,visual pane)
   */
   def 'treemap chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/treemap chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check calc field in tree/network chart(source pane,target pane, visual pane)
   */
   def 'tree chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/tree chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }


   /*
    check calc field in donut chart
   */
   def 'donut chart'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/donut chart', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate is max/min/none/count for date type
   */
   def 'aggregate_date'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_date', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate is max/min/none for time type
   */
   def 'aggregate_time'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_time', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate is NthLargest/NthSmallest/NthMostFrequent/PthPercentile
   */
   def 'aggregate_nthpth'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_nthpth', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate with calcfiled
   */
   def 'aggregate_with'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_with', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate_discrete is true for calcfield
   */
   def 'aggregate_discrete'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_discrete', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check data group on calcfield
   */
   def 'calc_data group'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/calc_data group', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(['bk1'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check drill filter on calcfield
   */
   def 'calc_drillfilter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/calc_drillfilter', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check aggregate distinct count
   */
   def 'aggregate_distinct count'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_distinct count', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    /*
    check mix calc on DB2 source
   */
   def 'testDB2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/testDB2', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(['(Home)'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    /*
    check mix calc on Model
   */
   def 'testModel'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/testModel', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(['(Home)'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    /*
    check mix calc on query
   */
   def 'testQuery'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/testQuery', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(['(Home)'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    /*
    check mix calc on worksheet
   */
   def 'testWorksheet'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^vs-calcfield/testWorksheet', caseName)
      vstest.executeVS(null, ['(Home)','zoom'] as String[])
      vstest.exportAsPNG(['(Home)', 'zoom'] as String[], null, true, false)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
}
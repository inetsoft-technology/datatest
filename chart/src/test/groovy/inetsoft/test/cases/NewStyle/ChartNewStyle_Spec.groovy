package inetsoft.test.cases.NewStyle

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.Specification

/*
 check Chart new style chart
 */
class ChartNewStyle_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check tree binding
   */
   def 'ST_tree1'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_tree1', caseName)
    vstest.exportAsPNG(null, ['Home', 'brush1', 'brush2','drillfilter'] as String[])

    expect:
    vstest.compareImage(null)
   }

   /*
    check tree chart when binding date
   */
   def 'ST_tree2'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_tree2', caseName)
    vstest.exportAsPNG(null, ['Home', 'brush1', 'drillfilter'] as String[])

    expect:
    vstest.compareImage(null)
   }

   /*
    check special for tree chart
   */
   def 'ST_tree3'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_tree3', caseName)
    vstest.exportAsPNG(null, ['Home', 'brush', 'drillfilter'] as String[])

    expect:
    vstest.compareImage(null)
   }


   /*
    check binding for network
   */
   def 'ST_network1'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_network1', caseName)
    vstest.exportAsPNG(null, ['Home', 'brush1', 'brush2','drillfilter','drillfilter2'] as String[])

    expect:
    vstest.compareImage(null)
   }

   /*
    check special for tree chart
   */
   def 'ST_network2'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_network2', caseName)
    vstest.exportAsPNG(null, ['Home', 'adhocfilter', 'datagroup','drillfilter'] as String[])

    expect:
    vstest.compareImage(null)
   }

    /*
    check merge legend
   */
   def 'ST_network3'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_network3', caseName)
    vstest.exportAsPNG(null, null)

    expect:
    vstest.compareImage(null)
   }


   /*
    check binding for circularnetwork
   */
   def 'ST_Circularnetwork1'() {
    caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_Circularnetwork1', caseName)
    vstest.exportAsPNG(null, ['Home', 'brush1', 'brush2','drillfilter1'] as String[])

    expect:
    vstest.compareImage(null)
   }


   /*
    check binding for circularnetwork
   */
   def 'ST_Circularnetwork2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_Circularnetwork2', caseName)
      vstest.exportAsPNG(null, ['Home', 'brush1', 'brush2', 'drillfilter1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check Contour Map binding
   */
   def 'RM_Contour Map'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/RM_Contour Map', caseName)
      vstest.exportAsPNG(null, ['Home'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Scattered Contour binding
   */
   def 'RM_Scattered Contour'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/RM_Scattered Contour', caseName)
      vstest.exportAsPNG(null, ['Home'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check treemap chart  binding and include parent label & sort other last properties
   */
   def 'ST_treemap'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_treemap', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, ['Home', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check sunburst binding and convert legend reverse & sort others last prperties
   */
   def 'ST_sunburst'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_sunburst', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2', 'bk3'] as String[])
      vstest.exportAsPDF(null, ['Home', 'bk2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check icicle binding and convert  sort others last prperties
   */
   def 'ST_icicle'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_icicle', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, ['Home', 'bk2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check circle packing  binding and include parent label & sort other last properties
   */
   def 'ST_circle packing'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_circle packing', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check marimekko  binding and hide axis
   */
   def 'ST_marimekko'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_marimekko', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, ['Home', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check gannt  binding and format
   */
   def 'ST_gannt'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_gannt', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check funnel binding
   */
   def 'ST_funnel'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_funnel', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check funnel binding<trend and comparison>
   */
   def 'ST_funnel2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_funnel2', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }


   /*
    check stepline binding
   */
   def 'ST_stepline'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_stepline', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check jump line binding
   */
   def 'ST_jumpline'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_jumpline', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      //vstest.exportAsPDF(null, ['Home', 'bk2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check interval binding
   */
   def 'ST_interval1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_interval1', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check interval binding
   */
   def 'ST_interval2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_interval2', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check boxplot binding
   */
   def 'ST_boxplot'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_boxplot', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check scattermatrix binding
   */
   def 'ST_scattermatrix'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_scattermatrix', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check worldcloud binding
   */
   def 'ST_worldcloud'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_worldcloud', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check webmap binding
    Note:if only appear compare becakgroud failed, please make sure system date > 2021-11-30. because webmap account was registered on 2021-11-30
   */
   def 'ST_webmap'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_webmap', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check webmap binding & pan action
   */
   def 'ST_webmap2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_webmap2', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check heatmap binding
   */
   def 'ST_heatmap'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_heatmap', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    check radar binding
   */
   def 'ST_radar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/NewStyle/ST_radar', caseName)
      vstest.exportAsPNG(null, ['Home'] as String[])

      expect:
      vstest.compareImage(null)
   }
}
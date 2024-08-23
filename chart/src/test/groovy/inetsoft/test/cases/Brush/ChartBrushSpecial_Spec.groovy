package inetsoft.test.cases.Brush

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

/*
check brush, zoom, exlcude action on special binding, such as component binding, dynimac binding and so on
 */

class ChartBrushSpecial_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
   check chart brush effect dataview and output, chart use normal binding, use calcfield, namegroup,
    */
   def 'RM3_2_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/ChartWithOther/RM3_2_1', caseName)
      vstest.exportAsPNG(null, ['Home','brush'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
   check chart brush effect dataview and output, chart use dynimac binding, drillmembers
    */
   def 'RM3_2_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/ChartWithOther/RM3_2_2', caseName)
      vstest.exportAsPNG(null, ['Home','brush1','brush2'] as String[])
      vstest.exportAsPDF(null, null)
      expect:
      vstest.compareImage(null)
   }

   /*
   check chart brush effect dataview, chart use component binding
    */
   def 'RM3_2_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/ChartWithOther/RM3_2_3', caseName)
      vstest.exportAsPNG(null, ['Home','brush1','brush2'] as String[])
      vstest.exportAsPDF(null, null)
      expect:
      vstest.compareImage(null)
   }

   def 'RM3_2_4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/ChartWithOther/RM3_2_4', caseName)
      vstest.exportAsPNG(null, ['Home','brush1'] as String[])
      vstest.exportAsPDF(null, null)
      expect:
      vstest.compareImage(null)
   }
}

package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Issue
import spock.lang.Specification

/**
 * check filter to impact other assembly
 */
class Filter_Relation_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check null value filters
    */
   @Issue('bk1 Calednar selected value not right in png')
   def 'RM1_2_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_1', caseName)
      vstest.exportAsPNG(null, ['null', 'null2', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check single range slider binding chart/crosstab aggregate field, to filter data
    */
   def 'RM1_2_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_2', caseName)
      vstest.executeVS(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData()
         vstest.compareImage()
      }
   }

   /**
    * check composite range slider binding chart/crosstab aggregate field, to filter data
    */
   def 'RM1_2_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_3', caseName)
      vstest.executeVS(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData()
         vstest.compareImage()
      }
   }

   /**
    * check parent-ID filter data, no relation with other filter
    * check selected value in print layout
    */
   def 'RM1_2_4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_4', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, ['bk1', 'bk2'] as String[])

      expect:
      verifyAll{
         vstest.compareImage()
      }
   }

   /**
    * check parent-ID filter data, selected multi value, null, same value
    * check selected value in print layout
    */
   def 'RM1_2_5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_5', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2'] as String[])
      vstest.exportAsPDF(null, ['bk1', 'bk2'] as String[])

      expect:
      verifyAll{
         vstest.compareImage()
      }
   }

   /**
    * check parent-ID filter data, selected  null, same value
    * check selected value in print layout
    */
   def 'RM1_2_6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_6', caseName)
      vstest.exportAsPNG(null, ['bk1'] as String[])
      vstest.exportAsPDF(null, ['bk1'] as String[])

      expect:
      verifyAll{
         vstest.compareImage()
      }
   }

   /**
    * check embedded table can not be filter data
    */
   def 'RM1_2_7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_7', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData()
   }

   /**
    * check snapshort embedded table can be filter
    */
   def 'RM1_2_8'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_2_8', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData()
   }

   /**
    * Relation Filter and Filter, two SelectionList
    */
   def 'RM1_3_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_3_1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * Relation Filter and Filter, SelectionList and single RangeSlider
    */
   def 'RM1_3_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_3_2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * Relation Filter and Filter, SelectionList and composite RangeSlider
    */
   def 'RM1_3_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_3_3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * Relation Filter and Filter, SelectionList and SelectionTree
    */
   def 'RM1_3_4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/FilterR/RM1_3_4', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }
}

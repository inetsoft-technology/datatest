package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class InputRelation_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check input binding embedded table to change embedded table cell value
    */
   def 'CM1_2_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/Relation/CM1_2_1', caseName)
      vstest.exportAsPNG(null, ['bk1'] as String[])
      expect:
      vstest.compareImage()
   }

   /**
    * check input binding variable to filter table datas
    */
   def 'CM1_2_1_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/Relation/CM1_2_1_1', caseName)
      vstest.exportAsPNG(null,  ['(Home)', 'bk1'] as String[])
      expect:
      vstest.compareImage()
   }

   /**
    * check Slider and Spinner binding variable to filter table datas
    */
   def 'CM1_2_1_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/Relation/CM1_2_1_2', caseName)
      vstest.exportAsPNG(null,  ['bk1'] as String[])
      expect:
      vstest.compareImage()
   }
}

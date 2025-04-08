package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.VSFormImportTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class FormTable_Spec extends Specification {
   static VSFormImportTest vstest
   static String caseName

   def setupSpec() {
      VSFormImportTest.initHome(this.class.getName())
   }

   /**
    * check delete row(first,middle,last) and table header in embedded form table
    * date/time format changed if header is deleted, ignore
    */
   def 'delete row and header in embedded form table' () {
      caseName = specificationContext.currentIteration.name
      vstest = new VSFormImportTest('1^128^__NULL__^FormTable/Embedded Form', caseName)
      vstest.importXLSToVS("Embedded_Form_Delete_Row.xlsx")

      expect:
      vstest.compareImage()
   }

   /**
    * check add and modify rows in embedded form table
    */
   def 'add and modify in embedded form table' () {
      caseName = specificationContext.currentIteration.name
      vstest = new VSFormImportTest('1^128^__NULL__^FormTable/Embedded Form', caseName)
      vstest.importXLSToVS("Embedded_Form_Add_Modify_Row.xlsx")

      expect:
      vstest.compareImage()
   }

   /**
    * check highlight and condition not apply to un-submitted rows
    */
   def 'un-submitted changes' () {
      caseName = specificationContext.currentIteration.name
      vstest = new VSFormImportTest('1^128^__NULL__^FormTable/Form2', caseName)
      vstest.importXLSToVS("Embedded_Form_Add_Modify_Row.xlsx")

      expect:
      vstest.compareImage()
   }
}
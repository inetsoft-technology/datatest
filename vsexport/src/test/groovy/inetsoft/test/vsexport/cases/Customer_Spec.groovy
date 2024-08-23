package inetsoft.test.vsexport.cases

import inetsoft.test.modules.VSExportTest
import spock.lang.Specification

class Customer_Spec extends Specification {
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * ws with special formula script, which cause chart didn't show. see Bug #60967
    * such as: field['Data'].substring(0, field['Data'].indexOf(' '))
    */
   def 'Customer_SpecalScript1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^CustomerIssue/UC_Student_Prop209' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * customer case. see Bug #62709
    */
   def 'Customer_protecht' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^CustomerIssue/protecht' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * test customer case
    */
   def 'Customer_DailyP&LSummary' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^CustomerIssue/DailyP&LSummary' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }
}

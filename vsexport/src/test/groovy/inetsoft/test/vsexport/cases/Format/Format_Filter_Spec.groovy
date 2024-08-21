package inetsoft.test.vsexport.cases.Format

import inetsoft.test.viewsheet.VSExportTest
import spock.lang.Specification

class Format_Filter_Spec extends Specification {
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check Callendar diff datapath to set date format
    */
   def 'Print_Format-FCalendar1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FFilter/FCalendar1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Callendar diff datapath to set date format
    */
   def 'Print_Format-FCalendar2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FFilter/FCalendar2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'sTitle'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'sTitle'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check user format apply on filters
    */
   def 'Print_Format-FFilters' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FFilter/FFilters' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check user format apply on others
    */
   def 'Print_Format-FOthers' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FOthers' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
}
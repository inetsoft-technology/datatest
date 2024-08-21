package inetsoft.test.vsexport.cases.Component

import inetsoft.test.viewsheet.VSExportTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Com_Filter_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check Selection List and Tree show Radiobutton on HTML when single selected as true
    */
   def 'Print_Com_Filter1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CFilter/CListTree' , caseName)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.compareHTML()
      }
   }

   /**
    * check multi selections in containers, Bug #50298
    */
   def 'Print_Com_CContainer1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CFilter/CContainer1' , caseName)
      vsExportTest.testExportAsPNG(['filter'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['filter'] as String[], null)
      vsExportTest.testExportAsHtml(['filter'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check calendar set min and max value
    */
   def 'Print_Calendar_minMax' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CFilter/CCalendar1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
      }
   }
   /**
    * check 'Select First Item on Dashboard Load' options on list, tree, checkbox
    */
   def 'Print_ListTree_selectedFirst' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CFilter/CListTree2' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
}

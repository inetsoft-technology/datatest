package inetsoft.test.vsexport.cases.Format

import inetsoft.test.viewsheet.VSExportTest
import spock.lang.Specification

class Format_DataView_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check Chart Format when export as PNG and HTML
    */
   def 'Print_Format-Chart1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/Chart/Chart1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Chart Format when export as PNG and HTML
    */
   def 'Print_Format-Chart2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/Chart/Chart2' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      //vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         //vsExportTest.compareHTML()
      }
   }

   /**
    * check Chart Format when export as PNG and HTML
    */
   def 'Print_Format-Chart3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/Chart/Chart3' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Chart Format when export as PNG and HTML
    */
   def 'Print_Format-Chart4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/Chart/Chart4' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check user format apply on some special chart, such as two text
    */
   def 'Print_Format-FChart1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FChart/FChart1' , caseName)
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
    * check user format apply on some special chart, such as two text
    */
   def 'Print_Format-FChart2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FChart/FChart2' , caseName)
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
    * check user format apply on some Table
    */
   def 'Print_Format-FTable' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FTables/FTable' , caseName)
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

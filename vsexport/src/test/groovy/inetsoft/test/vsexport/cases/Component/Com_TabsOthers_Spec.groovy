package inetsoft.test.vsexport.cases.Component

import inetsoft.test.modules.VSExportTest
import spock.lang.Ignore
import spock.lang.Specification

class Com_TabsOthers_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }



   /**
    * check Tab usages:
    * 1. table in tab, table title visible as false, shink to fit as true
   */
   def 'CTab_Tab1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/Tabs/titleVisibleInTab' , caseName)
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
   *Shape As background  contaion selectioncontainer and others assembly all can display well
   */
   def 'AdhocFilter_Z-Index' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/Others/AdhocFilter_Z-Index' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
   *Shape As background embeded viewsheet  all assembly can display well
   */
   def 'Embeded_Zindex' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/Others/Embeded_Zindex' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
   *Image As background viewsheet contain tab and form assembly all assembly can display well
   */
   def 'Form_Zindex' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/Others/Form_Zindex' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
   *Shape As background viewsheet contain groupcontainer all assembly can display well
   */
   def 'GroupContainer-Zindex' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/Others/GroupContainer-Zindex' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

}

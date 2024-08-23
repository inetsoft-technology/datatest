package inetsoft.test.vsexport.cases.Component

import inetsoft.test.modules.VSExportTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class Com_Table_Spec extends Specification {
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check table with max row, crosstab with merge expand options
    * embedded table cell value changed by RB
    */
   def 'CTable_CTable1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CTable1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check table width diff format and set col width and row height by script
    */
   def 'CTable_CTable2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CTable2' , caseName)
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
    * check table form table with diff format and col width
    */
   def 'CTable_CTable3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CTable3' , caseName)
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
    * crosstab title visible as false and header row height as 0
    */
   def 'CTable_Crosstab1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CCrosstab/CrosstabTitleVisible' , caseName)
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
    * crosstab title visible as false and header row height as 0
    */
   def 'CTable_CrosstabBind1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CCrosstab/CrosstabBind1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }


   /**
    * freehand execute with run query script
    */
   def 'CTable_Freehand1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/freehandWithRunQuery' , caseName)
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
    * freehand execute with run query script
    */
   def 'CTable_Freehand2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/freehand1' , caseName)
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
    * freehand execute with calcfield, hide column, script
    */
   def 'CTable_Table2F1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Table2F1' , caseName)
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
    * freehand execute with tab
    */
   def 'CTable_Table2F2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Table2F2' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_Table2F3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Table2F3' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_Crosstab2F1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_Crosstab2F2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F2' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }


   def 'CTable_Crosstab2F3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F3' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check list:
    * 1. show detail on crosstab1
    */
   def 'CTable_Crosstab2F4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F4' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_Crosstab2F5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F5' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check all date level add on 20210727
    * pending bug, case unfinished
    */
   def 'CTable_Crosstab2F6-0' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F6-0' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_Crosstab2F6-1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F6-1' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check all date level add on 20210727
    *  说明:
    * 1. Freehand1withScript result same with studio report.
    * 2. 将Freehand1完全转为formula script之后,一些cell merge到了一起, 但是与studio report一致, 暂时Ignore
    */
   def 'CTable_Crosstab2F7' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F7' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check date, time, datetime on None level.
    */
   def 'CTable_Crosstab2F8' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F8' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check aggregate is None and Nth and so on
    */
   def 'CTable_Crosstab2F9' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F9' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check below point:
    * None(Time), range_id, TopN, highlight
    */
   def 'CTable_Crosstab2F10' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/Crosstab2F10' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * 20210727
    */
   def 'CTable_FreeBind1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/FreeBind1' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_FreeBind2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/FreeBind2' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_FreeBind3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/FreeBind3' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   def 'CTable_FreeBind4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/FreeBind4' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }


   def 'CTable_FreeBind5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CTable/CFreehand/FreeBind5' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
}

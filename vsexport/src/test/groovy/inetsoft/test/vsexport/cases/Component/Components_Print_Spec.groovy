package inetsoft.test.vsexport.cases.Component

import inetsoft.test.modules.VSExportTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.See
import spock.lang.Specification

/**
 * 1) Check some component common usage
 * 2) this part use all vs in 'PrintLayout/Print'.it covered png, HTML, and PDF with print layout
 *
 */
class Components_Print_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check component title visible: true or false
    */
   def 'Print_title' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/title' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'titleVisible is false'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'titleVisible is false'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check component some common properties, control by script
    */
   def 'Print_Properties1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/properties1' , caseName)
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
    * check component some common properties: visible by diff parameter
    */
   def 'Print_Properties2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/properties2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'Hide', 'HideOnPrint'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'Hide', 'HideOnPrint'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check component visible when have mobile and printlayout
    */
   @See('Manual open the case, 1. Match mobile layout; 2. export it as PDF, print layout show right')
   def 'Print_Properties3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/properties3' , caseName)
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
    * check Table Print: null table and title visible as false
    */
   def 'Print_ComTable1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/ComTable1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'false'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'false'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Table Print: no H scroll bar, match page weight
    */
   def 'Print_ComTable2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/ComTable2' , caseName)
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
    * check Table Print: have H scroll bar, out of page weight
    */
   def 'Print_ComTable3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/ComTable3' , caseName)
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
    * check Table Print: have H scroll bar, resize on print layout to show all column
    */
   def 'Print_ComTable4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/ComTable4' , caseName)
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
    * check Crosstab Print, resize col row,header, title, hide column
    */
   def 'Print_Crosstab1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Crosstab1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1_splitCell', 'bk2_hideColumn'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1_splitCell', 'bk2_hideColumn'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
   /**
    * check Crosstab Print, resize, drill down, with component binding
    */
   def 'Print_Crosstab2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Crosstab2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'drill1'] as String[], null, false, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'drill1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
   /**
    * check Crosstab in emvs, tab, group
    */
   def 'Print_Crosstab3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Crosstab3' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Crosstab with H,V scrollbar, reisze size < colheader size
    */
   def 'Print_Crosstab4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Crosstab4' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, true)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Freehand print, resize title, with H, V Scroll bar, set diff column header
    */
   def 'Print_Freehand1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Freehand1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Freehand print, only have V expaned and multi cell merge
    */
   def 'Print_Freehand2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Freehand2' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Freehand print,  only have V  or only have H expaned and multi cell merge
    */
   def 'Print_Freehand3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Freehand3' , caseName)
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
    * check Freehand print, H and V epxaned, and multi cell merge
    */
   def 'Print_Freehand4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Freehand4' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Crosstab Special, VS have options and action, but report no
    */
   def 'Print_Crosstab_Spec1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Crosstab_Spec1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'drillFilter', 'drilldown'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'drillFilter', 'drilldown'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Freehand Speical, Freehand have, but Report CALC No
    */
   def 'Print_Freehand_Spec1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/DataView/Freehand_Spec1' , caseName)
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
    * check Selection common properties
    */
   def 'Print_Selection1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Selection1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'titleFalse', 'Hide', 'HideOnPrint'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'titleFalse', 'Hide', 'HideOnPrint'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Selection format on print layout
    */
   def 'Print_Selection2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Selection2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, false, true)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Selection print: Single selected and parent-child selected, addition table
    * Issue #46444
    */
   def 'Print_Selection3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Selection3' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1_all'] as String[], null, false, true)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1_all'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check RangeSlider print: diff properties value, and value format on diff data type
    */
   def 'Print_RangeSlider1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/RangeSlider1' , caseName)
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
    * check rangeSlider print: composite value and show on selection conatier
    */
   def 'Print_RangeSlider2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/RangeSlider2' , caseName)
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
    * check Selection Container print: format,multi selections.
    * Issue #46466
    */
   def 'Print_SelectionContainter1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/SelectionContainer1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Selection Container print: enable is false
    * Issue #46466
    */
   def 'Print_SelectionContainter2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/SelectionContainer2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'true'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Selection print in embedded vs
    * Issue #46466
    */
   def 'Print_Selection6_em' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Selection6_em' , caseName)
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
    * check Selection print in tab
    */
   def 'Print_Selection7_tab' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Selection7_tab' , caseName)
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
    * check Calendar print value on diff mode
    */
   def 'Print_Calendar1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Calendar1' , caseName)
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
    * check Calendar print value on some speical mode
    */
   def 'Print_Calendar2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Calendar2' , caseName)
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
    * check Calendar print when select title with custom format
    */
   def 'Print_Calendar3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Selections/Calendar3' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'range', 'comparision'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'range', 'comparision'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Text print
    */
   def 'Print_Text1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Output&Shape/Text1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'Hide', 'HideOnPrint'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'Hide', 'HideOnPrint'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Gauge print
    */
   def 'Print_Gauge1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Output&Shape/Gauge1' , caseName)
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
    * check Image print
    */
   def 'Print_Image1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Output&Shape/Image1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'HideOnPrint'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'HideOnPrint'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check output in EM
    * Note: convert line to Report line, but report line unsupport arrow type. so arrow type don't support.
    */
   def 'Print_OutPut_EM' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Output&Shape/OutPut_EM' , caseName)
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
    * check output in EM
    */
   def 'Print_Shape_EM' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Output&Shape/Shape_EM' , caseName)
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
    * check all form visible and enable properties
    */
   def 'Print_Com_Form1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Form/Com_Form1' , caseName)
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
    * check Slider and Spinner print with format
    */
   def 'Print_Form1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Form/Form1' , caseName)
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
    * check Slider and Spinner print with format
    */
   def 'Print_Form2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Form/Form2' , caseName)
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
    * check Tab print with H, V Scroll bar
    */
   def 'Print_Tab1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Tab1' , caseName)
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
    * check Tab print: Group in tab
    */
   def 'Print_Tab4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Tab4' , caseName)
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
    * check Tab print: embedded vs in tab
    */
   def 'Print_Tab5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Tab5' , caseName)
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
    * check Tab print: group, selection container, embedded vs in tab
    */
   def 'Print_Tab6' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Tab6' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check Group print: with H,V scroll
    */
   def 'Print_Group1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Group1' , caseName)
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
    * check embedded vs: group container in tab
    */
   def 'Print_Embedded1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Embedded1' , caseName)
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
    * check embedded vs: embedded vs in tab
    */
   def 'Print_Embedded2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Embedded2' , caseName)
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
    * check embedded vs: selection container in tab
    */
   def 'Print_Embedded3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Embedded3' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1', 'bk2'] as String[], null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1', 'bk2'] as String[], null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check embedded vs: selection container in tab, group
    */
   def 'Print_Embedded4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Print/Special/Embedded4' , caseName)
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

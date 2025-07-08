package inetsoft.test.vsexport.cases.Component

import inetsoft.test.modules.VSExportTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

/**
 * use to test Chart when export as PNG,HTML,print layout
 */

class Com_CChart_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check Chart Print: some properties
    */
   def 'Print_CChart_Properties' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_Properties' , caseName)
      vsExportTest.testExportAsPNG(null, null, false, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
      }
   }

   /**
    * check Chart Print: pdate
    */
   def 'Print_ChartWithPdate' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/ChartWithPdate' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
      }
   }

   /**
    * check Chart Print: Chart in Group and chart with HV
    */
   def 'Print_ChartWithGroupHV' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/ChartWithGroupHV' , caseName)
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
    * check Chart Print: null chart, Chart with tab and embedded vs
    */
   def 'Print_ChartWithTab' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/ChartWithTab' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
      }
   }

   /**
    * check Radar only with 1Y + 1Break binding, export as PNG, so only check PNG and PrintLayout
    */
   def 'CChart_CRadar1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CRadar1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check Radar only with 1Y + 1Break binding, export as PNG, so only check PNG and PrintLayout
    */
   def 'CChart_CRadar2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CRadar2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk2'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk2'] as String[], null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check the word cloud chart
    */
   def 'CChart_CWord1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CWord1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check the suggest chart: x axis is range
    */
   def 'CChart_CRange1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CRange1' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check the suggest chart: x axis is range
    * check multi style: as time seiries, fill time-serias as 0
    */
   def 'CChart_CStep1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CStep1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check step and jume chart
    * check multi style: as time seiries, fill time-serias as 0, null
    */
   def 'CChart_CStep2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CStep2' , caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'fillTimeFalse', 'fillZeroTrue'] as String[], null, true, false)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'fillTimeFalse', 'fillZeroTrue'] as String[], null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check step line
    */
   def 'CChart_StepLine1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_StepLine1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style:step+jump
    * as time seires, set time series as NULl or 0
    */
   def 'CChart_CMulti1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CMulti1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style:line + step area
    */
   def 'CChart_CMulti2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CMulti2' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style:line + step area
    */
   def 'CChart_CMulti3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CMulti3' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style:line + step area
    */
   def 'CChart_CMulti4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CMulti4' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style, bar +stepline
    */
   def 'CChart_CMulti5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_CMulti3' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style, bar +stepline
    */
   def 'CChart_CMulti6' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_CMulti4' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check multi style, bar +stepline
    */
   def 'CChart_CMulti7' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_CMulti5' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check chart legend properties and position
    */
   def 'CChart_Legend1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_Legend1' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

   /**
    * check chart legend properties and position
    */
   def 'CChart_Legend2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Components/CChart/CChart_Legend2' , caseName)
      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll{
         vsExportTest.comparePNG()
      }
   }

}

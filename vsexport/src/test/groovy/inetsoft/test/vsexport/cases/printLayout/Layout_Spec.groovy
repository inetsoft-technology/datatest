package inetsoft.test.vsexport.cases.printLayout

import inetsoft.test.modules.VSExportTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Layout_Spec extends Specification {
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check normal component position in, on, out of print layout border
    */
   def 'Layout_TestCase1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/TestCase1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check tab, group, embedded vs position in, on, out of print layout border
    */
   def 'Layout_TestCase2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/TestCase2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Table resize and diff position in print layout
    */
   def 'Layout_table' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/table' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check component diff position in print layout
    */
   def 'Layout_Layout1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }


   /**
    * check selection and form  resize and diff position in print layout
    */
   def 'Layout_Layout2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check group, tab resize and diff position in print layout
    */
   def 'Layout_Layout3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout3' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check embedded vs resize and diff position in print layout
    */
   def 'Layout_Layout4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout4' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Chart and Shape image, gauge resize and diff position in print layout
    */
   def 'Layout_Layout5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout5' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Chart and Shape image, gauge resize and diff position in print layout
    */
   def 'Layout_Layout7' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Layout7' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check title scale on all component
    */
   def 'Layout_Title' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/Title' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Chart Scale
    */
   def 'Layout_scale_chart' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_chart' , caseName)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Table Scale
    */
   def 'Layout_scale_table' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_table' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Table Scale
    */
   def 'Layout_scale_table2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_table2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Crosstab Scale
    */
   def 'Layout_scale_crosstab' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_crosstab' , caseName)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1'] as String[], null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Freehand Scale
    */
   def 'Layout_scale_Freehand' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_freehand table' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Selection Scale
    */
   def 'Layout_scale_selection' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_selection' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Selection Scale
    */
   def 'Layout_scale_selectionDropdown' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_selectionDropdown' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Form Scale
    */
   def 'Layout_scale_Form' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_Form' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Tab,Group,Embedded vs Scale
    */
   def 'Layout_scale_tab' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_tab' , caseName)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'container', 'emvs'] as String[], null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check output, shape Scale
    */
   def 'Layout_scale_outputShape' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_outputShape' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font size < 10, use 10
    */
   def 'Layout_scale_special1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_special1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font size > 18, use 18
    */
   def 'Layout_scale_special3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_special3' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font < 1
    */
   def 'Layout_scale_specialcrosstab' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_specialcrosstab' , caseName)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1', 'bk2'] as String[], null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font < 1
    */
   def 'Layout_scale_specialfreehand' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_specialfreehand' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font < 1
    */
   def 'Layout_scale_tableRelation' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_tableRelation' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check resize axis, y axis,plot axis, scale font < 1
    */
   def 'Layout_scale_special2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Layout/scale_special2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }
}

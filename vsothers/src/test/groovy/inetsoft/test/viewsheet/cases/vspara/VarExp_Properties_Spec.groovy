package inetsoft.test.viewsheet.cases.vspara

import inetsoft.test.modules.VSExportTest
import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class VarExp_Properties_Spec extends Specification{
   static ViewsheetTest vstest
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check all component enable, visiable, and title which use variable or Expression.
    */
   def 'General_Properties'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/General_Properties', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'hide'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check expression and variable on chart target line,band,Statistics
    */
   def 'Chart_TargetLine1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Chart_TargetLine1', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check filter component options
    */
   def 'Filter_Options'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Filter_Options', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check use expression and variable to set all options of output component
    */
   def 'Output_Options'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Output_Options', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'image2'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check use expression and variable to set all options of form component
    */
   def 'Form_Options'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Form_Options', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check use expression and variable to set all options of form component
    */
   def 'Form_Variable'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Form_Variable', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check from component send multi value, see Bug #61643, Bug #61718
    */
   def 'Form_MultiValues'() {
      caseName = specificationContext.currentIteration.name
      def paras = new HashMap<String, String[]>()
      paras.put('testVar', 'two' as String[])
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Form_MultiValue', caseName)
      vstest.exportAsPNG(paras, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /*
    check expression and variable on shape-color
   */
   def 'Shape_Options'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Properties/Shape_Options', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check format(backgroud, forgroud, text) apply by variable
    */
   def 'Format_WithVariable'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^VarExp/Properties/Format_WithVariable', caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true,false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }

   /**
    * check format(backgroud, forgroud, text) apply by Expression
    */
   def 'Format_Expression'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^VarExp/Properties/Format_Expression', caseName)
      vsExportTest.testExportAsPNG(['(Home)', 'bk1'] as String[], null, true,false)
      vsExportTest.testExportAsHtml(null, null)

      expect:
      verifyAll {
         vsExportTest.comparePNG()
         vsExportTest.compareHTML()
      }
   }
}

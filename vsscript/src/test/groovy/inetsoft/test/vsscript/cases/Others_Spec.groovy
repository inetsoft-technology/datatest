package inetsoft.test.vsscript.cases

import inetsoft.test.vsscript.VSScriptTest
import spock.lang.IgnoreRest
import spock.lang.Specification

/**
 * test all shape and other assembly script
 */
class Others_Spec extends Specification {
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }
   /**
    * Test the shape script
    * Rejected issue, Issue #47244, Issue #47307
    */
   def 'TestCase-Shape' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullRectangle1', COMMAND: 'lineStyle = StyleConstant.DOUBLE_LINE\n' +
              'shadow = true\n' +
              'roundCorner = 60\n' +
              '//gradientColor = ']]
      def TestData2 = [[HANDLER: 'NullOval1', COMMAND: 'lineStyle = StyleConstant.THIN_LINE\n' +
              'shadow = true\n' +
              'background = "pink"\n' +
              '//gradientColor']]
      def TestData3 = [[HANDLER: 'NullLine1', COMMAND: 'lineStyle = StyleConstant.THICK_LINE\n' +
              'beginArrowStyle = StyleConstant.ARROW_LINE_1\n' +
              'endArrowStyle = StyleConstant.ARROW_LINE_2\n' +
              'foreground = "blue"']]
      def TestData4 = [[HANDLER: 'NullRectangle1', COMMAND: 'var gradient = [true,"linear",45,"red 10","yellow 20"]\n' +
              'gradientColor = gradient']]
      def TestData5 = [[HANDLER: 'NullOval1', COMMAND: 'var gradient = [true,"radial","red 10","yellow 20", "#00FF00 40"]\n' +
              'gradientColor = gradient']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Others/Shape', caseName)
      vsScriptTest.printVS('rectangle', TestData1, ['NullRectangle1'] as String[])
      vsScriptTest.printVS('oval', TestData2, ['NullOval1'] as String[])
      vsScriptTest.printVS('line', TestData3, ['NullLine1'] as String[])
      vsScriptTest.printVS('gradientLinear', TestData4, ['NullRectangle1'] as String[])
      vsScriptTest.printVS('gradientRadial', TestData5, ['NullOval1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Shape_rectangle',
                                 'TestCase-Shape_oval',
                                 'TestCase-Shape_line',
                                 'TestCase-Shape_gradientLinear',
                                 'TestCase-Shape_gradientRadial'] as String[])
   }

   /**
    * Test the tab script
    */
   def 'TestCase-Tab'() {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Tab1', COMMAND: 'labels = ["tab1", "tab2", "tab3"]\n' +
              'background = "pink"'], [
                               HANDLER: 'Text1',
                               COMMAND: 'Text1.value = "selectedObject: " + Tab1.selectedObject + ", index:" + Tab1.selectedIndex'
                       ]]
      def TestData2 = [[HANDLER: 'Tab1', COMMAND: 'selectedIndex=1']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Others/TabGroup', caseName)
      //Issue #45554
      vsScriptTest.printVS('script', TestData1, ['Tab1', 'Gauge1', 'SelectionList1', 'RadioButton1', 'Text1'] as String[])
      //Issue #49526
      vsScriptTest.printVS('selectednIdex', TestData2, ['Tab1', 'Gauge1', 'SelectionList1', 'RadioButton1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Tab_script', 'TestCase-Tab_selectednIdex'] as String[])
   }

   /**
    * Test the groupContainer script
    */
   def 'TestCase_GroupContainer' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'GroupContainer1', COMMAND: 'image = "google.png"\n' +
              'tile = true\n' +
              'imageAlpha = 50']]
      def TestData2 = [[HANDLER: 'GroupContainer1', COMMAND: 'image = "google.png"\n' +
              'scaleImage = true\n' +
              'maintainAspectRatio = true']]
      def TestData3 = [[HANDLER: 'GroupContainer1', COMMAND: 'image = "google.png"\n' +
              'scaleImage = true\n' +
              'maintainAspectRatio = false\n' +
              'scale9 = [100,200,200,200]']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Others/TabGroup', caseName)
      vsScriptTest.printVS('tile', TestData1, ['GroupContainer1', 'Gauge2', 'Text2'] as String[])
      vsScriptTest.printVS('scale1', TestData2, ['GroupContainer1', 'Gauge2', 'Text2'] as String[])
      vsScriptTest.printVS('scale2', TestData3, ['GroupContainer1', 'Gauge2', 'Text2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase_GroupContainer_tile',
                                 'TestCase_GroupContainer_scale1',
                                 'TestCase_GroupContainer_scale2'] as String[])
   }

   /**
    * Test access ws data block on vs
    */
   def 'TestCase_AccessWS' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/SpecialScript/accessWS', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_AccessWS_VS'] as String[])
   }

   /**
    * test data, table script on crosstab and vs
    * pending Issue #53490
    */
   def 'TestCase_DataTable' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/SpecialScript/TableDataScript', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_DataTable_VS'] as String[])
   }

   /**
    * test use parameter to change table header
    * pending Bug #55938
    */
   def 'TestCase_TablePara1' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/SpecialScript/TablePara1', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_TablePara1_VS'] as String[])
   }

   static VSScriptTest vsScriptTest
   String caseName
}

package inetsoft.test.vsscript.cases

import inetsoft.test.modules.VSScriptTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class OutPut_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }
   /**
    * Test Image common script
    * imageAlpha didn't work when export it, the script ignore.
    */
   def 'TestCase_Image1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Image1', COMMAND: 'shadow = true, tile = true'], [
                      HANDLER: 'Text1',
                      COMMAND: 'autoSize = true\n' +
                              'value = "Image value:" + Image1.value + "\\nhas Highlighted? " + ' +
                              'Image1.highlighted["imageHigh"]'
              ]]
      def TestData2 = [[HANDLER: 'NullImage1', COMMAND: 'image = "test.gif"\n' +
              'scaleImage = true\n' +
              'imageAlpha = 30\n' +
              'maintainAspectRatio = true']]
      def TestData3 = [[HANDLER: 'NullImage1', COMMAND: 'image = "test.gif"\n' +
              'scaleImage = true\n' +
              'maintainAspectRatio = false\n' +
              'scale9 = [100,100,100,100]']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/OutPut/OutPut1', caseName)
      vsScriptTest.printVS('script1', TestData1, ['Image1', 'Text1'] as String[])
      vsScriptTest.printVS('scaleImage', TestData2, ['NullImage1'] as String[])
      vsScriptTest.printVS('scale9', TestData3, ['NullImage1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase_Image1_script1',
                                 'TestCase_Image1_scaleImage',
                                 'TestCase_Image1_scale9'] as String[])
   }

   /**
    * Test Gauge script
    */
   def 'TestCase_Gauge1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullGauge1', COMMAND: 'query = "Query2"\n' +
              'fields = ["customer_id"]\n' +
              'formula = StyleConstant.SUM_FORMULA\n' +
              'min = 100\n' +
              'max = 2100\n' +
              'minorInc = 200\n' +
              'majorInc = 400\n' +
              'rangeColors = ["green", "yellow", "red"]\n' +
              'ranges = [500,1000,2000]']]
      def TestData2 = [[HANDLER: 'Gauge1', COMMAND: 'rangeColors = ["blue", "yellow", "red"]\n' +
              'ranges = [50,80,120]\n' +
              'rangeGradient = false\n' +
              'valueFillColor = "pink"\n' +
              'shadow = true\n' +
              'labelVisible = false'], [
                      HANDLER: 'Text1',
                      COMMAND: 'value ="Gauge value: " + Gauge1.value']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/OutPut/OutPut1', caseName)
      vsScriptTest.printVS('binding', TestData1, ['NullGauge1'] as String[])
      vsScriptTest.printVS('valueFillColor', TestData2, ['Gauge1', 'Text1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase_Gauge1_binding',
                                 'TestCase_Gauge1_valueFillColor'] as String[])
   }

   /**
    * Test Text script
    */
   def 'TestCase_Text1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullText', COMMAND: 'query = "Query2"\n' +
              'fields = ["state"]\n' +
              'formula = StyleConstant.COUNT_FORMULA\n' +
              'shadow = true\n' +
              'Text1.value = value']]
      def TestData2 = [[HANDLER: 'NullText', COMMAND: 'borders = [StyleConstant.THIN_LINE,StyleConstant.THIN_LINE,\n' +
              'StyleConstant.THIN_LINE, StyleConstant.THIN_LINE];\n' +
              'borderColors = ["blue","blue","blue","blue"]\n' +
              'text = "1)this is a long string to check wrapping\\n 2) second line to do\\n3) third line"\n' +
              'autoSize = true']]
      def TestData3 = [[HANDLER: 'NullText', COMMAND: 'borders = [StyleConstant.THIN_LINE,StyleConstant.THIN_LINE,\n' +
              'StyleConstant.THIN_LINE, StyleConstant.THIN_LINE];\n' +
              'borderColors = ["blue","blue","blue","blue"]\n' +
              'text = "1)this is a long string to check wrapping\\n 2) second line to do\\n3) third line"\n' +
              'wrapping = false']]
      def TestData4 = [[HANDLER: 'NullText', COMMAND: 'borders = [StyleConstant.THIN_LINE,StyleConstant.THIN_LINE,\n' +
              'StyleConstant.THIN_LINE, StyleConstant.THIN_LINE];\n' +
              'borderColors = ["blue","blue","blue","blue"]\n' +
              'text = "1)this is a long string to check wrapping\\n 2) second line to do\\n3) third line"']]

      def TestData5 = [[HANDLER: 'Text2', COMMAND: 'setPresenter("ButtonPresenter")'],
                       [HANDLER: 'Text1', COMMAND:'value = "Text2 as Highlight? " + Text2.highlighted[\'Text2Highlight\']']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/OutPut/OutPut1', caseName)
      vsScriptTest.printVS('script', TestData1, ['NullText', 'Text1'] as String[])
      vsScriptTest.printVS('autoSize', TestData2, ['NullText'] as String[])
      vsScriptTest.printVS('wrappingF', TestData3, ['NullText'] as String[])
      vsScriptTest.printVS('wrappingT', TestData4, ['NullText'] as String[])
      vsScriptTest.printVS('highlightPresenter', TestData5, ['Text1', 'Text2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase_Text1_script',
                                 'TestCase_Text1_autoSize',
                                 'TestCase_Text1_wrappingF',
                                 'TestCase_Text1_wrappingT',
                                 'TestCase_Text1_highlightPresenter'] as String[])
   }

   static VSScriptTest vsScriptTest
   String caseName
}

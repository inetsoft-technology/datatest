package inetsoft.test.vsscript.cases

import inetsoft.test.vsscript.VSScriptTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Form_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * Check CheckBox and Radiobutton script
    */
   def 'TestCase_RCB_binding1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'RadioButton2', COMMAND: 'selectedObject = "NJ"\n' +
              'title = "title:L- " + selectedLabel + "V-" + selectedObject\n' +
              'sortType = StyleConstant.SORT_NONE\n' +
              'embeddedDataOnBottom = true\n' +
              'var temp = "value:"\n' +
              'values.forEach(function(it) {\n' +
              '   temp += it + ", "\n' +
              '})\n' +
              'temp = temp + "\\nlabel:"\n' +
              'labels.forEach(function(it) {\n' +
              '   temp += it + ", "\n' +
              '})\n' +
              'Text1.value = temp']]
      def TestData2 = [[HANDLER: 'RadioButton1', COMMAND: 'values = ["2020-11-23", "2020-11-24", "2020-11-25"]\n' +
              'labels = ["2020/11/23", "2020/11/24", "2020/11/25"]\n' +
              'datatype = StyleConstant.DATE\n' +
              'titleVisible = false\n' +
              'selectedObject = "2020-11-25"']]
      def TestData3 = [[HANDLER: 'RadioButton1', COMMAND: 'values = [4,2,3,1]\n' +
              'labels = ["a4", "b2", "c3", "d1"]\n' +
              'datatype = StyleConstant.NUMBER\n' +
              'selectedObject = 3\n' +
              'sortByValue = true']]


      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Form/Form_RCB', caseName)
      vsScriptTest.printVS('RadioS1', TestData1, ['RadioButton2'] as String[])
      vsScriptTest.printVS('bind1', TestData2, ['RadioButton1'] as String[])
      vsScriptTest.printVS('sortByValue', TestData3, ['RadioButton1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase_RCB_binding1_RadioS1',
                                 'TestCase_RCB_binding1_bind1',
                                 'TestCase_RCB_binding1_sortByValue'] as String[])
   }

   /**
    *  Check checbox script
    */
   def 'TestCase-RCB_binding2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'CheckBox1', COMMAND: 'values = ["a", "b","c", "d"]\n' +
              'labels = ["a_1", "b_1","c_1", "d_1"]\n' +
              'datatype = StyleConstant.STRING\n' +
              'selectedObjects = ["a", "c"]\n' +
              'title = "selectObjects: " + selectedObjects[0] + "," + selectedObjects[1] + ", " + selectedLabels[0] + "," + selectedLabels[1]\n' +
              'sortType = StyleConstant.SORT_DESC']]
      def TestData2 = [[HANDLER: 'CheckBox2', COMMAND: 'sortType = StyleConstant.NONE\n' +
              'embeddedDataOnBottom = true\n' +
              'titleVisible = false\n' +
              'selectedObjects = ["2020-11-24 00:00:00", "1999-01-01 00:00:00"]']]
      def TestData3 = [[HANDLER: 'CheckBox1', COMMAND: 'values = [4,2,3,1]\n' +
              'labels = ["a4", "b2", "c3", "d1"]\n' +
              'datatype = StyleConstant.NUMBER\n' +
              'selectedObjects = [3, 4]\n' +
              'sortByValue = false']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Form/Form_RCB', caseName)
      vsScriptTest.printVS('bind1', TestData1, ['CheckBox1'] as String[])
      vsScriptTest.printVS('option1', TestData2, ['CheckBox2'] as String[])
      vsScriptTest.printVS('sortByValue', TestData3, ['CheckBox1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-RCB_binding2_bind1',
                                 'TestCase-RCB_binding2_option1',
                                 'TestCase-RCB_binding2_sortByValue'] as String[])
   }

   /**
    * Check Slider script
    */
   def 'TestCase-Slide' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Slider1', COMMAND: 'min = 2\n' +
              'max = 102\n' +
              'selectedObject = 32\n' +
              'increment = 10'], [
                      HANDLER: 'Text1',
                      COMMAND: 'value = "label: " + Slider1.selectedLabel + ", object: " + Slider1.selectedObject'
              ]]
      def TestData2 = [[HANDLER: 'Slider1', COMMAND:'currentVisible = false\n' +
              'minVisible = false\n' +
              'maxVisible = false\n' +
              'tickVisible = false']]
      def TestData3 = [[HANDLER: 'Slider1', COMMAND: 'labelVisible = false; selectedObject = 32']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Form/Form_RCB', caseName)
      vsScriptTest.printVS('script1', TestData1, ['Slider1', 'Text1'] as String[])
      vsScriptTest.printVS('script2', TestData2, ['Slider1'] as String[])
      vsScriptTest.printVS('script3', TestData3, ['Slider1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Slide_script1',
                                 'TestCase-Slide_script2',
                                 'TestCase-Slide_script3'] as String[])
   }

   /**
    * Check Spinner script
    */
   def 'TestCase-Spinner' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Spinner1', COMMAND: 'min = 10\n' +
              'max = 120\n' +
              'increment = 15'], [
                      HANDLER: 'Text1',
                      COMMAND: 'value = "min:" + Spinner1.min + ";max: " + Spinner1.max + ", increment: " + Spinner1.increment'
              ]]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Form/Form_RCB', caseName)
      vsScriptTest.printVS('script', TestData1, ['Spinner1', 'Text1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Spinner_script'] as String[])
   }

   /**
    * Check the input script
    */
   def 'TestCase-Input' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'TextInput1', COMMAND: 'value = "to test script"\n' +
              'foreground = "blue"\n' +
              'borderColors = ["green","green","green","green"]\n' +
              'borders = [StyleConstant.THIN_LINE,StyleConstant.THIN_LINE,StyleConstant.THIN_LINE,StyleConstant.THIN_LINE]']]
      def TestData2 = [[HANDLER: 'TextInput1', COMMAND: 'defaultText = "test default text"']]
      def TestData3 = [[HANDLER: 'TextInput1', COMMAND: 'placeholderText = "input number"'],[
                        HANDLER: 'Text1', COMMAND: 'value = TextInput1.placeholderText']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Form/Form_RCB', caseName)
      vsScriptTest.printVS('value', TestData1, ['TextInput1'] as String[])
      vsScriptTest.printVS('defaultValue', TestData2, ['TextInput1'] as String[])
      vsScriptTest.printVS('placeholderText', TestData3, ['TextInput1', 'Text1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Input_value', 'TestCase-Input_defaultValue', 'TestCase-Input_placeholderText'] as String[])
   }

   static VSScriptTest vsScriptTest
   String caseName
}

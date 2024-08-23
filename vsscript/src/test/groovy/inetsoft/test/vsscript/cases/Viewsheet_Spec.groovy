package inetsoft.test.vsscript.cases

import inetsoft.test.modules.VSScriptTest
import spock.lang.Specification

class Viewsheet_Spec extends Specification {
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * Test the Common Properties Script
    */
   def 'TestCase-VS_Base' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONREFRESH', COMMAND: 'Text1.background = java.awt.Color.PINK\n' +
              'Text1.foreground = \'blue\'\n' +
              'Text1.alpha = 50\n' +
              'Text1.borders = [StyleConstant.DOUBLE_LINE,StyleConstant.THIN_LINE,\n' +
              'StyleConstant.THICK_LINE, StyleConstant.NO_BORDER];\n' +
              'Text1.borderColors =[\'red\', [0,255,0], 0x444444, \'black\']\n' +
              'Text1.font = java.awt.Font(\'Verdana\',java.awt.Font.BOLD, 20)\n' +
              'Text1.format = StyleConstant.MESSAGE_FORMAT\n' +
              'Text1.formatSpec = "{0} _ test"\n' +
              'Text1.alignment = StyleConstant.H_RIGHT | StyleConstant.V_BOTTOM']]
      def TestData2 = [[HANDLER: 'Text1', COMMAND: 'enabled = false\n' +
              'size = java.awt.Dimension(100,100)\n' +
              'position = java.awt.Point(400,400)\n' +
              'value = scaledPosition'
      ]]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/VS_Self/VS_Baisc', caseName)
      vsScriptTest.printVS('format', TestData1, null)
      vsScriptTest.printVS('property', TestData2, null)
      then:
      vsScriptTest.compareImage()

   }

   /**
    * Test the viewsheet script Properties Script
    */
   def 'TestCase-VS_Self' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONREFRESH', COMMAND: 'Text1.value = thisViewsheet.viewsheetName +' +
              ' \'\\n\' + thisViewsheet.viewsheetAlias + ' +
              '\'\\n\' + thisViewsheet.viewsheetPath']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/VS_Self/VS_Baisc', caseName)
      vsScriptTest.printVS('self', TestData1, null)
      then:
      vsScriptTest.compareImage()
   }

   /**
    * Test the embedded vs script
    */
   def 'TestCase-VS_embedded' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/VS_Self/VS_embedded', caseName)
      vsScriptTest.printVS(null, null)
      then:
      vsScriptTest.compareImage()
   }

   static VSScriptTest vsScriptTest
   String caseName
}

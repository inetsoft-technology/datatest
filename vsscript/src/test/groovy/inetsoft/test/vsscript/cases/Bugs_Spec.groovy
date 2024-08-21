package inetsoft.test.vsscript.cases

import inetsoft.test.vsscript.VSScriptTest
import spock.lang.Specification

class Bugs_Spec extends Specification{
   static VSScriptTest vsScriptTest
   String caseName

   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * check some basic Egraph script on binding chart.
    * TestData1: Bug #62455, Bug #62458
    * TestData2: Bug #62459, Bug #62462
    */
   def 'TestCase-EGraphBind1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var elem = graph.getElement(0);\n' +
                                'var spec = elem.getTextSpec("Sum(Quantity Purchased)");\n' +
                                'spec.setColor(java.awt.Color(0xff0000));\n' +
                                'var logo = getImage("https://www.inetsoft.com/images/home/logo.gif");\n' +
                                'var shape = new GShape.ImageShape();\n' +
                                'shape.setImage(logo);\n' +
                                'shape.setTile(true);\n' +
                                'var frame = new StaticShapeFrame(shape);\n' +
                                'elem.setShapeFrame(frame);']]
      def TestData2 = [[HANDLER: 'Chart2',
                       COMMAND: 'var elem = graph.getElement(0);\n' +
                               'elem.setStackGroup(false);\n' +
                               'var frame = new CategoricalColorFrame();\n' +
                               'frame.setField("Region");\n' +
                               'var spec = new LegendSpec();\n' +
                               'spec.setBackground(java.awt.Color(0xff00ff));\n' +
                               'frame.setLegendSpec(spec);\n' +
                               'elem.setColorFrame(frame);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('shapeText', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setStackGroupColorFrame', TestData2, ['Chart2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-EGraphBind1_shapeText', 'TestCase-EGraphBind1_setStackGroupColorFrame'] as String[])
   }

   /**
    * check point chart one line properties. see Bug #62494
    */
   def 'TestCase-PointChart' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1', COMMAND: 'oneLine=true'],
                       [HANDLER: 'Chart2', COMMAND: 'oneLine=true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/PointChart', caseName)
      vsScriptTest.printVS('oneline', TestData1, ['Chart1','Chart2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-PointChart_oneline'] as String[])
   }

   /**
    * check setShape and set, see Bug #62567, Bug #62546, Bug #62451
    */
   def 'TestCase-SetShapeText' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var coord = graph.getCoordinate();\n' +
                                'var spec = coord.getPlotSpec();\n' +
                                'spec.setBackground(java.awt.Color.BLUE)\n' +
                                'spec.setAlpha(.3)\n' +
                                'var elem = graph.getElement(0);\n' +
                                'var shapeFranme = elem.getShapeFrame();\n' +
                                'shapeFranme.setShape(GShape.ARROW)']]
      def TestData2 =[[HANDLER: 'Chart3',
                      COMMAND: 'var elem = graph.getElement(0);\n' +
                              'var frame = elem.getSizeFrame();\n' +
                              'frame.setSmallest(20);\n' +
                              'frame.setLargest(100);\n' +
                              'frame.setMax(200);']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('setShape', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setSizeFrame', TestData2, ['Chart3'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-SetShapeText_setShape', 'TestCase-SetShapeText_setSizeFrame'] as String[])
   }

   /**
    * test update freehand data by script
    * pending Bug #63348
    */
   def 'TestCase_FreehandDataByScript' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/SpecialScript/FreehandFormula', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_FreehandDataByScript_VS'] as String[])
   }

   /**
    * test array.inclues() script,Issue #64427, Issue #64466, Bug #64335
    */
   def 'TestCase_ArrayIncludes' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/SpecialScript/ArraysIncludes', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_ArrayIncludes_VS'] as String[])
   }
}

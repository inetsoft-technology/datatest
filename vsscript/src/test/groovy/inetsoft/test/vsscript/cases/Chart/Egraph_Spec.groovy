package inetsoft.test.vsscript.cases.Chart

import inetsoft.test.modules.VSScriptTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class Egraph_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test GraphElement addDim,adVar and setOther plot property
    */
   def 'TestCase-GraphElement1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["City" , "State", "Quantity"],["NJ","Edison",2500], ["NJ","Piscataway",3000],["NY","NY City",5000],["NY","Yonkers",450]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State","Quantity");\n' +
                                 'elem.addDim("City");\n' +
                                 'elem.setHint(GraphElement.HINT_SHINE,"true");\n' +
                                 'elem.setHint(GraphElement.HINT_ALPHA, "0.5");\n' +
                                 'elem.setBorderColor(java.awt.Color(0xff00ff));\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity", "Total"],["NY",550,2500],["NJ",370,3000]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new LineElement("State","Quantity");\n' +
                                 'elem.addVar("Total");\n' +
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["NJ",100],["NJ",200], ["NY",300],["NY",450]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new PointElement("State", "Quantity");\n' +
                                 'var frame = new HeatColorFrame();\n' +
                                 'frame.setField("Quantity");\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'elem.setStackGroup(true);\n' +
                                 'elem.setCollisionModifier(GraphElement.STACK_SYMMETRIC); \n' +
                                 'graph.addElement(elem);\n' +
                                 'var frame = new DefaultTextFrame();\n' +
                                 'frame.setField("Quantity")\n' +
                                 'elem.setTextFrame(frame);\n' +
                                 'elem.setLabelPlacement(Chart.BOTTOM);\n' +
                                 'var scale = new LinearScale("Quantity");\n' +
                                 'scale.setMax(400);\n' +
                                 'graph.setScale("Quantity",scale);\n' +
                                 'elem.setInPlot(false);']]
	  when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('addDim', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('addVar', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('plotproperty', TestData3, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-GraphElement1_addDim',
                                'TestCase-GraphElement1_addVar',
                                'TestCase-GraphElement1_plotproperty'] as String[])
   }

   /**
    * test GraphElement visual pane column
    */
   def 'TestCase-GraphElement2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["CA", 200], ["NY", 300]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new StaticLineFrame();\n' +
                                 'frame.setLine(GLine.LARGE_DASH)\n' +
                                 'elem.setLineFrame(frame);\n' +
                                 'var frame1 = new DefaultTextFrame();\n' +
                                 'frame1.setField("Quantity");\n' +
                                 'var spec = new TextSpec();\n '+
                                 'spec.setColor(java.awt.Color(0xff0000));\n'+
                                 'elem.setTextFrame(frame1);\n'+
                                 'elem.setTextSpec(spec);\n'+
                                 'var frame2 = new StaticTextureFrame();\n'+
                                 'frame2.setTexture(GTexture.PATTERN_18)\n'+
                                 'elem.setTextureFrame(frame2);\n'+
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity","m1","m2", "m3"],["NJ",200,6,3,4],["NY",300,8,2,5]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var frame = new StarShapeFrame();\n'+
                                 'var elem = new PointElement("State", "Quantity");\n' +
                                 'frame.setFields(["m1", "m2", "m3"]);\n' +
                                 'elem.setShapeFrame(frame);\n'+
                                 'var frame1 = new LinearSizeFrame();\n'+
                                 'frame1.setField("m2");\n'+
                                 'frame1.setSmallest(10);\n'+
                                 'frame1.setLargest(50);\n'+
                                 'frame1.setMax(100);\n'+
                                 'elem.setSizeFrame(frame1);\n'+
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('addtexture', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('addshapesize', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-GraphElement2_addtexture',
                                'TestCase-GraphElement2_addshapesize'] as String[])
   }

   /**
    * test IntervalElement property setting with script
    */
   def 'TestCase-IntervalElement' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["Student","Bottom Score","Top Score"],["Joe",70,80], ["Eric",50,90],["Jane",90,100], ["Sue",40,45]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement();\n' +
                                 'elem.addDim("Student");\n' +
                                 'elem.addInterval("Bottom Score","Top Score") \n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["NJ",200],["NJ",300],["NY",-300],["NY",100]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'elem.setStackGroup(true);\n' +
                                 'elem.setCollisionModifier(GraphElement.STACK_SYMMETRIC);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('addinterval', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setstacknegative', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-IntervalElement_addinterval',
                                'TestCase-IntervalElement_setstacknegative'] as String[])
   }

   /**
    * test Line Element property setting with script
    */
   def 'TestCase-LineElement' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["NJ",100],["NY",300],["PA",200]];\n'+
                                 'graph = new EGraph();\n' +
                                 'elem = new LineElement("State", "Quantity");\n' +
                                 'elem.setStartArrow(true);\n' +
                                 'elem.setEndArrow(true);\n' +
                                 'elem.setClosed(true);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["Sun",100],["Mon",300],["Tue",null],["Wed",400],["Thur",600],["Fri",550],["Sat",200]];\n'+
                                 'graph = new EGraph();\n' +
                                 'elem = new LineElement("State", "Quantity") ;\n' +
                                 'elem.setIgnoreNull(false);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('setarrow', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('ignorenull', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-LineElement_setarrow',
                                'TestCase-LineElement_ignorenull'] as String[])
   }

   /**
    * Create schema Element setting with script
    */
   def 'TestCase-SchemaElement' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Hi", "Lo", "Open", "Close"],["NJ", 200, 100, 120, 150],["NY", 300, 100, 200, 120]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new SchemaElement();\n' +
                                 'elem.addDim("State")\n' +
                                 'elem.addSchema("Hi", "Close", "Lo");\n' +
                                 'elem.setPainter(new StockPainter());\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                       COMMAND: 'dataset = [["State", "Quantity"],["NY",200], ["NJ",300]];\n' +
                               'graph = new EGraph();\n' +
                               'var elem = new IntervalElement("State","Quantity");\n' +
                               'elem.setHints({shine:"true", alpha:"0.5"});\n' +
                               'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('stock', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setHints', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-SchemaElement_stock', 'TestCase-SchemaElement_setHints'] as String[])
   }

   /**
    * setting log scale with script
    */
   def 'TestCase-LogScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",12], ["NY",450]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LogScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity")\n' +
                                 'qscale.setBase(2);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('logScale', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-LogScale_logScale'] as String[])
   }

   /**
    * setting Categorical scale with script
    */
   def 'TestCase-CategoricalScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300], ["CT", 50], ["PA", 175]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new AreaElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'sscale.setFill(true);\n' +
                                 'sscale.setValues(["NY","NJ"]);\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new RectCoord(sscale, qscale);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('FillandSetvalue', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-CategoricalScale_FillandSetvalue'] as String[])
   }

   /**
    * setting liner scale with script
    */
   def 'TestCase-LinearScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n'+
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'qscale.setIncrement(25)\n' +
                                 'qscale.setMin(10);\n'+
                                 'qscale.setMax(300);\n'+
                                 'qscale.setMinorIncrement(10);\n'+
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'qscale.setReversed(true);\n'+
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'qscale.setScaleRange(new StackRange());\n' +
                                 'graph.setScale("Quantity", qscale);\n'+
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('Scale1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('Scale2', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-LinearScale_Scale1',
                                'TestCase-LinearScale_Scale2'] as String[])
   }

   /**
    * setting coordinate Scale with script coordinateRotate,coordinateReflect,coordianteTranspose
    */
   def 'TestCase-coordinateScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["Direction", "Score"], [(Math.PI/2),20],[(Math.PI/4),30], [(Math.PI),35]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new PointElement("Direction", "Score");\n' +
                                 'var xscale = new LinearScale("Direction");\n' +
                                 'var yscale = new LinearScale("Score");\n' +
                                 'yscale.setMin(0);\n'+
                                 'yscale.setMax(40);\n'+
                                 'xscale.setMin(0);\n'+
                                 'xscale.setMax(1.95*Math.PI);\n'+
                                 'xscale.setIncrement(Math.PI/8)\n'+
                                 'var rect = new RectCoord(xscale,yscale);\n'+
                                 'var polar = new PolarCoord(rect);\n'+
                                 'polar.rotate(45)\n'+
                                 'polar.setType(PolarCoord.THETA);\n'+
                                 'graph.setCoordinate(polar);\n'+
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n'+
                                 'var coord = new RectCoord(sscale,qscale);\n'+
                                 'coord.reflect(true);\n'+
                                 'graph.setCoordinate(coord);\n'+
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new RectCoord(sscale,qscale);\n'+
                                 'coord.transpose()\n'+
                                 'graph.setCoordinate(coord);\n'+
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph2', caseName)
      vsScriptTest.printVS('coordinateRotate', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('coordinateReflect', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('coordianteTranspose', TestData3, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-coordinateScale_coordinateRotate',
                                'TestCase-coordinateScale_coordinateReflect',
                                'TestCase-coordinateScale_coordianteTranspose'] as String[])
   }

   /**
    * setting coordinate SetType with script
    */
   def 'TestCase-PolarCood' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State","Quantity"], ["CA",200], ["NY",300]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("Quantity");\n' +
                                 'var yscale = new LinearScale("Quantity");\n' +
                                 'var rect = new RectCoord(null, yscale);\n' +
                                 'var polar = new PolarCoord(rect);\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'var range = new StackRange();\n' +
                                 'polar.setType(PolarCoord.THETA);\n' +
                                 'frame.setField("State");\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'elem.setCollisionModifier(GraphElement.MOVE_STACK);\n' +
                                 'yscale.setScaleRange(range);\n' +
                                 'var spec = new AxisSpec();\n' +
                                 'spec.setLabelVisible(false);\n' +
                                 'spec.setTickVisible(false);\n' +
                                 'spec.setLineVisible(false);\n' +
                                 'yscale.setAxisSpec(spec);\n' +
                                 'graph.setCoordinate(polar);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('settype', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-PolarCood_settype'] as String[])
   }

   /**
    * setting RectCoord setXScale with script
    */
   def 'TestCase-RectCoord' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State","Quantity","Total"], ["NJ",200,10000], ["NY",300,8000]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new PointElement("State", "Quantity");\n' +
                                 'var elem2 = new PointElement("State", "Total");\n' +
                                 'var cframe = new CategoricalColorFrame();\n' +
                                 'cframe.init("Quantity", "Total");\n' +
                                 'elem.setColorFrame(cframe);\n' +
                                 'elem2.setColorFrame(cframe);\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var qscale2 = new LinearScale("Total");\n' +
                                 'var coord = new RectCoord();\n' +
                                 'coord.setXScale(sscale);\n' +
                                 'coord.setYScale(qscale);\n' +
                                 'coord.setYScale2(qscale2);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);\n' +
                                 'graph.addElement(elem2);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('setXScale', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-RectCoord_setXScale'] as String[])
   }

   /**
    * setting TriCood scale  with script
    */
   def 'TestCase-TriCood' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["Quantity","Total","Returns"],[50,50,100],[75,100,25],[100,100,0]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new PointElement();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'qscale.setMin(0);\n' +
                                 'qscale.setMax(200);\n' +
                                 'var spec = new AxisSpec();\n' +
                                 'spec.setGridStyle(GraphConstants.DOT_LINE);\n' +
                                 'qscale.setAxisSpec(spec);\n' +
                                 'var coord = new TriCoord(qscale);\n' +
                                 'coord.setScale(qscale);\n' +
                                 'elem.addDim("Quantity");\n' +
                                 'elem.addDim("Total");\n' +
                                 'elem.addVar("Returns");\n' +
                                 'graph.addElement(elem);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.setScale("Total", qscale);\n' +
                                 'graph.setScale("Returns", qscale);\n' +
                                 'graph.setCoordinate(coord);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-TriCood_scale'] as String[])
   }

   /**
    * setting Rect25Coord scale  with script
    */
   def 'TestCase-Rect25Coord' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n'+
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new Rect25Coord(sscale,qscale);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-Rect25Coord_scale'] as String[])
   }

   /**
    * setting ParallelCoord scale  with script
    */
   def 'TestCase-ParallelCoord' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["Quantity","Total","Returns"], [200,800,10], [175,1000,15],[50,300,20]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new LineElement();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var tscale = new LinearScale("Total");\n' +
                                 'var rscale = new LinearScale("Returns");\n' +
                                 'var coord = new ParallelCoord();\n' +
                                 'coord.setScales([qscale,tscale,rscale]);\n' +
                                 'elem.addDim("Quantity");\n' +
                                 'elem.addDim("Total");\n' +
                                 'elem.addDim("Returns");\n' +
                                 'graph.addElement(elem);\n' +
                                 'graph.setCoordinate(coord);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-ParallelCoord_scale'] as String[])
   }

   /**
    * setting FacetCoord scale  with script
    */
   def 'TestCase-FacetCoord' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "City", "Product", "Quantity"],["NJ", "Piscataway", "P1", 200],["NJ", "Edison", "P2", 100],["NY", "NYC", "P1", 300]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("City", "Quantity");\n' +
                                 'var state = new CategoricalScale("State");\n' +
                                 'var city = new CategoricalScale("City");\n' +
                                 'var product = new CategoricalScale("Product");\n' +
                                 'var quantity = new LinearScale("Quantity");\n' +
                                 'var inner = new RectCoord(city, quantity);\n' +
                                 'var outer = new RectCoord(state, product);\n' +
                                 'var coord = new FacetCoord(outer,inner);\n' +
                                 'coord.setInnerCoordinates([inner]);\n' +
                                 'coord.setOuterCoordinate(outer);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-FacetCoord_scale'] as String[])
   }

   /**
    * setting scale function
    */
   def 'TestCase-Scale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'qscale.init(dataset);\n' +
                                 'var defaultMax = qscale.getMax();\n' +
                                 'qscale.setMax(defaultMax + defaultMax/2);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity")\n' +
                                 'var spec = new AxisSpec();\n' +
                                 'spec.setLineColor(java.awt.Color(0xff0000));\n' +
                                 'qscale.setAxisSpec(spec);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State","Quantity","Total"], ["NJ",100,0], ["NY",1500,30000]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LogScale();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'qscale.setDataFields(["Total"]);\n' +
                                 'graph.addElement(elem) \n' +
                                 'graph.setScale("Quantity", qscale);']]
      def TestData4 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]]; \n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity"); \n' +
                                 'var coord = new RectCoord(sscale,qscale);\n' +
                                 'qscale.setScaleOption(Scale.TICKS);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      def TestData5 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "City", "Product", "Quantity"],["NJ", "Piscataway", "P1", 200],["NJ", "Edison", "P2", 100],["NY", "NYC", "P1", 300]]; \n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("City", "Quantity");\n' +
                                 'var state = new CategoricalScale("State");\n' +
                                 'var city = new CategoricalScale("City");\n' +
                                 'var product = new CategoricalScale("Product");\n' +
                                 'var quantity = new LinearScale("Quantity");\n' +
                                 'quantity.setSharedRange(false)\n' +
                                 'var inner = new RectCoord(city, quantity);\n' +
                                 'var outer = new RectCoord(state, product);\n' +
                                 'var coord = new FacetCoord(outer,inner);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      def TestData6 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",-300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new DefaultTextFrame();\n' +
                                 'frame.setField("Quantity");\n' +
                                 'elem.setTextFrame(frame);\n' +
                                 'var range = new LinearRange();\n' +
                                 'range.setAbsoluteValue(true);\n' +
                                 'qscale.setScaleRange(range);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('Scaleinit', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setAxisSpec', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('setDataFields', TestData3, ['Chart1'] as String[])
      vsScriptTest.printVS('setScaleOption', TestData4, ['Chart1'] as String[])
      vsScriptTest.printVS('setSharedRange', TestData5, ['Chart1'] as String[])
      vsScriptTest.printVS('setAbsoluteValue', TestData6, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Scale_Scaleinit',
                                'TestCase-Scale_setAxisSpec',
                                'TestCase-Scale_setDataFields',
                                'TestCase-Scale_setScaleOption',
                                'TestCase-Scale_setSharedRange',
                                'TestCase-Scale_setAbsoluteValue'] as String[])
   }

   /**
    * setting Power.scale function
    */
   def 'TestCase-PowerScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new PowerScale();\n' +
                                 'qscale.setFields(["Quantity"]);\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'qscale.setExponent(0.5);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-PowerScale_scale'] as String[])
   }

   /**
    * setting Time.scale function
    */
   def 'TestCase-TimeScale' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var date1 = new Date();\n' +
                                 'var date2 = new Date();\n' +
                                 'var minDate = new Date();\n' +
                                 'var maxDate = new Date();\n' +
                                 'date1.setFullYear(2008,9,1);\n' +
                                 'date2.setFullYear(2009,2,1);\n' +
                                 'minDate.setFullYear(2005,8,1);\n' +
                                 'maxDate.setFullYear(2011,10,1);\n' +
                                 'var arr = [["Date", "Quantity"], [date1,200], [date2,300]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("Date","Quantity")\n' +
                                 'var tscale = new TimeScale("Date");\n'+
                                 'tscale.setMin(minDate);\n' +
                                 'tscale.setMax(maxDate);\n' +
                                 'tscale.setType(TimeScale.YEAR);\n' +
                                 'graph.setScale("Date", tscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('scale', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-TimeScale_scale'] as String[])
   }

   /**
    * setting stack.scale function
    */
   def 'TestCase-StackRange' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"], ["NJ",200], ["NJ",100], ["NY",400],["NY",300]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new PointElement("State", "Quantity")\n' +
                                 'range = new StackRange();\n' +
                                 'range.setGroupField("State");\n' +
                                 'qscale.setScaleRange(range);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'var arr = [["State", "Quantity"],["NJ",200],["NJ",100],["NY",-300],["NY",-400]];\n' +
                                 'dataset = new DefaultDataSet(arr);\n' +
                                 'graph = new EGraph();\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'range = new StackRange();\n' +
                                 'range.setStackNegative(false);\n' +
                                 'qscale.setScaleRange(range);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('stackRange1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('stackRange2', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-StackRange_stackRange1', 'TestCase-StackRange_stackRange2'] as String[])
   }

   /**
    * check PolarCoord apply on binding chart Bug #58887, Bug #58887
    */
   def 'TestCase_PolarCoord' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/PolarGraph', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_PolarCoord_VS'] as String[])
   }

   /**
    * check check #50059,7650, MultiTextFrame, and setBorderColor
    */
   def 'TestCase-MultiText' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/MultiTextFrame', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase-MultiText_VS'] as String[])
   }

   /**
    * check LineForm function.
    */
   def 'TestCase-LineForm' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var elem = graph.getElement(0);\n' +
                                'var form = new LineForm();\n' +
                                'form.addPoint(new java.awt.Point(0,0));\n' +
                                'form.addPoint(new java.awt.Point(200,200));\n' +
                                'form.addPoint(new java.awt.Point(200,100));\n' +
                                'form.setLine(Chart.DASH_LINE);\n' +
                                'form.setColor(java.awt.Color(0xff0000));\n' +
                                'form.setEndArrow(true)\n' +
                                'form.setXOffset(80)\n' +
                                'form.setYOffset(100)\n' +
                                'graph.addForm(form)']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'var form = new DefaultForm();\n' +
                                'form.setShape(new java.awt.geom.Rectangle2D.Double(100,100,200,200));\n' +
                                'form.setFill(true);\n' +
                                'form.setColor(java.awt.Color(0xff0000))\n' +
                                'form.setZIndex(2)\n' +
                                'graph.addForm(form);']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('setLineForm', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setDefaultForm', TestData2, ['Chart2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-LineForm_setLineForm', 'TestCase-LineForm_setDefaultForm'] as String[])
   }

   /**
    * check LabelForm,RectForm function.
    */
   def 'TestCase-LabelForm' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var elem = graph.getElement(0);\n' +
                                'var lform1 = new LabelForm();\n' +
                                'lform1.setTuple([2, 5000]);\n' +
                                'lform1.setLabel(\'check here;\');\n' +
                                'lform1.setColor(java.awt.Color(0xff0000))\n' +
                                'lform1.setAlignmentX(Chart.CENTER_ALIGNMENT);\n' +
                                'var lform2 = new LabelForm();\n' +
                                'lform2.setLabel("label2")\n' +
                                'lform2.setPoint(new java.awt.Point(310, 200))\n' +
                                'var spec = new TextSpec();\n' +
                                'spec.setColor(java.awt.Color(0xA349A4));\n' +
                                'lform2.setTextSpec(spec);\n' +
                                'graph.addForm(lform1)\n' +
                                'graph.addForm(lform2)\n']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'var rect1 = new RectForm();\n' +
                                'var rect2 = new RectForm();\n' +
                                'var rect3 = new RectForm();\n' +
                                'rect1.setTopLeftPoint(new java.awt.Point(100, 100));\n' +
                                'rect1.setBottomRightPoint(new java.awt.Point(150, 50));\n' +
                                'rect1.setColor(java.awt.Color(0xff0000));\n' +
                                'rect2.setTopLeftTuple([4,4000]);\n' +
                                'rect2.setBottomRightTuple([2,2000]);\n' +
                                'rect2.setColor(java.awt.Color(0xEF752));\n' +
                                'rect3.setTopLeftValues([\'Hardware\', 3000])\n' +
                                'rect3.setBottomRightValues([\'Personal\',1000]);\n' +
                                'rect3.setColor(java.awt.Color(0xFFF200));\n' +
                                'rect3.setFill(true)\n' +
                                'rect3.setZIndex(3)\n' +
                                'graph.addForm(rect1);\n' +
                                'graph.addForm(rect2);\n' +
                                'graph.addForm(rect3);']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('setLabelForm', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setRectForm', TestData2, ['Chart2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-LabelForm_setLabelForm', 'TestCase-LabelForm_setRectForm'] as String[])
   }

   /**
   * check ShapeForm,TagForm function.
   */
   def 'TestCase-ShapeForm' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var shape1 = new ShapeForm();\n' +
                                'var shape2 = new ShapeForm();\n' +
                                'shape1.setPoint(new java.awt.geom.Point2D.Double(100,200));\n' +
                                'shape1.setShape(GShape.FILLED_TRIANGLE);\n' +
                                'shape1.setColor(java.awt.Color(0xA349A4));\n' +
                                'shape1.setSize(new java.awt.Dimension(20,20));\n' +
                                'shape1.setAlignmentX(Chart.LEFT_ALIGNMENT);\n' +
                                'shape1.setRotation(45)\n' +
                                'shape2.setValues([\'Hardware\', 5000])\n' +
                                'shape2.setShape(GShape.STAR)\n' +
                                'shape2.setSize(new java.awt.Dimension(20,20))\n' +
                                'shape2.setColor(java.awt.Color(0xF711EF))\n' +
                                'graph.addForm(shape1);\n' +
                                'graph.addForm(shape2);']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'var form1 = new TagForm();\n' +
                                'var form2 = new TagForm();\n' +
                                'form1.setLabel("Note1:");\n' +
                                'form1.setValues([\'Hardware\', 3000]);\n' +
                                'form1.setColor(java.awt.Color(0xff0000))\n' +
                                'form2.setLabel("Note2: check those");\n' +
                                'form2.setTuple([0.5, 4000])\n' +
                                'form2.setColor(java.awt.Color(0xF514B1))\n' +
                                'graph.addForm(form1);\n' +
                                'graph.addForm(form2);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('setShapeForm', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setTagForm', TestData2, ['Chart2'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-ShapeForm_setShapeForm', 'TestCase-ShapeForm_setTagForm'] as String[])
   }

   /**
    * check setHints functions.
    */
   def 'TestCase-setProperties' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                       COMMAND: 'var elem = graph.getElement(0);\n' +
                               'elem.setHints({shine:"true", alpha:"0.5"});']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EgraphBinding', caseName)
      vsScriptTest.printVS('setHints', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-setProperties_setHints'] as String[])
   }

   /**
    * test AxisSpec property setting with script
    */
   def 'TestCase-AxisSpec' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var date1 = new Date();\n'+
                                 'var date2 = new Date();\n' +
                                 'date1.setFullYear(2008,0,1);\n' +
                                 'date2.setFullYear(2008,10,1);\n' +
                                 'dataset = [["Date", "Quantity"], [date1,200], [date2,300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("Date", "Quantity");\n' +
                                 'var tscale = new TimeScale("Date");\n' +
                                 'var aspec = new AxisSpec();\n' +
                                 'var tspec = new TextSpec();\n' +
                                 'tspec.setFormat(java.text.SimpleDateFormat("yyyy-MMM"));\n' +
                                 'aspec.setTextSpec(tspec);\n' +
                                 'aspec.setAbbreviate(true);\n' +
                                 'tscale.setAxisSpec(aspec);\n' +
                                 'aspec.setAxisStyle(AxisSpec.AXIS_DOUBLE2);\n' +
                                 'aspec.setGridColor(java.awt.Color(0xff0000));\n' +
                                 'aspec.setGridStyle(Chart.DASH_LINE);\n' +
                                 'aspec.setGridOnTop(true);\n' +
                                 'aspec.setGridAsShape(false);\n' +
                                 'graph.setScale("Date", tscale);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["NJ",20000],["NY",30000]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var aspec = new AxisSpec();\n' +
                                 'var textspec = new TextSpec();\n' +
                                 'var specx = new TitleSpec();\n' +
                                 'var specx2 = new TitleSpec();\n' +
                                 'var specy = new TitleSpec();\n' +
                                 'var specy2 = new TitleSpec();\n' +
                                 'specx.setLabel("X Title");\n' +
                                 'specx2.setLabel("X2 Title");\n' +
                                 'specy.setLabel("Y Title");\n' +
                                 'specy2.setLabel("Y2 Title");\n' +
                                 'graph.setXTitleSpec(specx);\n' +
                                 'graph.setX2TitleSpec(specx2);\n' +
                                 'graph.setYTitleSpec(specy);\n' +
                                 'graph.setY2TitleSpec(specy2);\n' +
                                 'qscale.setMax(40000);\n' +
                                 'aspec.setInPlot(true);\n' +
                                 'aspec.setLabelGap(30);\n' +
                                 'textspec.setColor(java.awt.Color(0xff0000));\n' +
                                 'aspec.setTextSpec(textspec);\n' +
                                 'aspec.setTickVisible(false);\n' +
                                 'qscale.setAxisSpec(aspec);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"],["NJ",20000],["NY",30000]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var cscale = new CategoricalScale("State");\n' +
                                 'var aspec1 = new AxisSpec();\n' +
                                 'var aspec2 = new AxisSpec();\n' +
                                 'var tframe = new DefaultTextFrame();\n' +
                                 'aspec1.setLineColor(java.awt.Color(0xff0000));\n' +
                                 'aspec2.setLineColor(java.awt.Color(0x00ff00));\n' +
                                 'aspec1.setLabelVisible(false);\n' +
                                 'aspec1.setLineVisible(true); \n' +
                                 'aspec2.setLineVisible(false);\n' +
                                 'qscale.setAxisSpec(aspec1);\n' +
                                 'cscale.setAxisSpec(aspec2);\n' +
                                 'tframe.setText("NJ","New Jersey");\n' +
                                 'tframe.setText("NY","New York");\n' +
                                 'aspec2.setTextFrame(tframe);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'graph.setScale("State", cscale);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('grid', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('axislabel', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('axisstick', TestData3, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-AxisSpec_grid',
                                'TestCase-AxisSpec_axislabel',
                                'TestCase-AxisSpec_axisstick',] as String[])
   }

   /**
    * test LegendSpec property setting with script
    */
   def 'TestCase-LegendSpec' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["New Jersey",200], ["New York",300], ["Pennsylvania",120],\
                                 ["Connecticut",450], ["New Mexico",200],["Colorado",300], ["Oregon",200],\
                                 ["Kentucky",300], ["California",100],  ["Alaska",350], ["Alabama",200],\
                                 ["Kansas",500],  ["Texas",200], ["North Dakota",300], ["Maryland",200],\
                                 ["Delaware",250],  ["Washington",200], ["Vermont",75]];\n'+
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'var spec = new LegendSpec();\n' +
                                 'frame.setField("State");\n' +
                                 'spec.setBackground(java.awt.Color(0xff00ff));\n' +
                                 'spec.setBorder(Chart.LARGE_DASH);\n' +
                                 'spec.setBorderColor(java.awt.Color(0x060f14));\n' +
                                 'spec.setPosition(java.awt.Point(80,245));\n' +
                                 'spec.setPreferredSize(java.awt.Dimension(190,100));\n' +
                                 'frame.setLegendSpec(spec);\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'graph.setLegendLayout(Chart.IN_PLACE);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'frame.setField("State");\n' +
                                 'var spec = new LegendSpec();\n' +
                                 'var tspec = new TextSpec();\n' +
                                 'var tf = new DefaultTextFrame();\n' +
                                 'tspec.setColor(java.awt.Color(0xff0000));\n' +
                                 'spec.setTitleTextSpec(tspec);\n' +
                                 'spec.setTitle("Legend1");\n' +
                                 'tf.setText("NJ","New Jersey");\n' +
                                 'tf.setText("NY","New York");\n' +
                                 'spec.setTextFrame(tf);\n' +
                                 'spec.setTextSpec(tspec);\n' +
                                 'frame.setLegendSpec(spec);\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'graph.setLegendPreferredSize(350);\n' +
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'frame.setField("State");\n' +
                                 'var spec = new LegendSpec();\n' +
                                 'var tspec = new TextSpec();\n' +
                                 'tspec.setColor(java.awt.Color(0xff0000));\n' +
                                 'spec.setTitleTextSpec(tspec);\n' +
                                 'spec.setTitle("Legend1");\n' +
                                 'spec.setTitleVisible(false);\n' +
                                 'frame.setLegendSpec(spec);\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'graph.addElement(elem);']]
      def TestData4 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'frame.setField("State");\n' +
                                 'var spec = new LegendSpec();\n' +
                                 'var tspec = new TextSpec();\n' +
                                 'tspec.setColor(java.awt.Color(0xff0000));\n' +
                                 'spec.setTitleTextSpec(tspec);\n' +
                                 'spec.setVisible(false);\n' +
                                 'frame.setLegendSpec(spec);\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('mixed', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('title', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('titlevisible', TestData3, ['Chart1'] as String[])
      vsScriptTest.printVS('visible', TestData4, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-LegendSpec_mixed',
                                'TestCase-LegendSpec_title',
                                'TestCase-LegendSpec_titlevisible',
                                'TestCase-LegendSpec_visible'] as String[])
   }

   /**
    * test PlotSpec property setting with script
    */
   def 'TestCase-PlotSpec' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'elem.setHint(GraphElement.HINT_ALPHA,.8);\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new RectCoord(sscale,qscale);\n' +
                                 'var spec = new PlotSpec();\n' +
                                 'var logo = getImage("https://www.inetsoft.com/images/home/logo.gif");\n' +
                                 'spec.setBackgroundImage(logo);\n' +
                                 'spec.setAlpha(.3);\n' +
                                 'spec.setLockAspect(true);\n' +
                                 'coord.setPlotSpec(spec);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new RectCoord(sscale,qscale);\n' +
                                 'var spec = new PlotSpec();\n' +
                                 'var form = new LabelForm();\n' +
                                 'var aspec = new AxisSpec();\n' +
                                 'var titlespec = new TitleSpec();\n' +
                                 'var tspec = new TextSpec(); \n' +
                                 'form.setLabel("label1");\n' +
                                 'form.setValues(["NY", 100]);\n' +
                                 'tspec.setBackground(java.awt.Color(0xcccccc)); \n' +
                                 'spec.setBackground(java.awt.Color(0x0eb70e));\n' +
                                 'tspec.setColor(java.awt.Color(0xff0000));\n' +
                                 'form.setTextSpec(tspec); \n' +
                                 'tspec.setFont(java.awt.Font("Verdana",java.awt.Font.BOLD, 14)); \n' +
                                 'titlespec.setLabel("X Title");\n' +
                                 'titlespec.setTextSpec(tspec); \n' +
                                 'tspec.setFormat(java.text.DecimalFormat("##,###.00"));\n' +
                                 'tspec.setRotation(45);\n' +
                                 'aspec.setTextSpec(tspec); \n' +
                                 'qscale.setAxisSpec(aspec);\n' +
                                 'coord.setPlotSpec(spec);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addForm(form);\n' +
                                 'graph.setXTitleSpec(titlespec);\n' +
                                 'graph.addElement(elem);']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["Q1","Q2"], [300,200], [500,300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new PointElement("Q1", "Q2");\n' +
                                 'elem.setHint(GraphElement.HINT_ALPHA,.8);\n' +
                                 'var sscale = new LinearScale("Q1");\n' +
                                 'var qscale = new LinearScale("Q2");\n' +
                                 'var coord = new RectCoord(sscale,qscale);\n' +
                                 'var spec = new PlotSpec();\n' +
                                 'var logo = getImage("https://www.inetsoft.com/images/home/logo.gif");\n' +
                                 'spec.setBackgroundImage(logo);\n' +
                                 'spec.setYMax(150);\n' +
                                 'spec.setYMin(100);\n' +
                                 'spec.setXMax(400);\n' +
                                 'spec.setXMin(100);\n' +
                                 'coord.setPlotSpec(spec);\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('backgroundImage1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('label', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('backgroundImage2', TestData3, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-PlotSpec_backgroundImage1',
                                'TestCase-PlotSpec_label',
                                'TestCase-PlotSpec_backgroundImage2'] as String[])
   }

   static VSScriptTest vsScriptTest
   String caseName
}

package inetsoft.test.vsscript.cases.Chart

import inetsoft.test.modules.VSScriptTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class ChartBindingInfo_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test chart script which inherit from parent，create normalchart banding all field
    */
   def 'TestCase-BandingField' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                 'bindingInfo.xFields = [["Region",Chart.STRING]];\n' +
                                 'bindingInfo.yFields  = [["Paid",Chart.NUMBER]];\n' +
                                 'bindingInfo.setColorField("State",Chart.STRING);\n' +
                                 'bindingInfo.setShapeField("Region",Chart.STRING);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                 'bindingInfo.xFields = [["Region",Chart.STRING]];\n' +
                                 'bindingInfo.yFields = [["Paid",Chart.NUMBER]];\n' +
                                 'bindingInfo.setSizeField("Date",Chart.STRING);\n' +
                                 'bindingInfo.setTextField("Discount",Chart.NUMBER);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo', caseName)
      vsScriptTest.printVS('Chart1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('Chart2', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-BandingField_Chart1',
                                'TestCase-BandingField_Chart2'] as String[])
   }

   /**
    * test create map chart
    */
   def 'TestCase-MapChart' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                 'chartStyle = Chart.CHART_MAP;\n' +
                                 'bindingInfo.geoFields = [["state",Chart.STRING]];\n' +
                                 'mapType = Chart["MAP_TYPE_U.S."];\n' +
                                 'bindingInfo.setMapLayer("state",Chart.STATE);\n' +
                                 'bindingInfo.addMapping("state","I","Idaho")']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'bindingInfo.removeMapping("state","I")']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/MapBinding', caseName)
      vsScriptTest.printVS('Chart1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('Chart2', TestData2, ['Chart2'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-MapChart_Chart1',
                                'TestCase-MapChart_Chart2'] as String[])
   }

   /**
    * test create stock and candel chart
    */
   def 'TestCase-StockingAndCandel' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'Chart1.query="Stock Prices";\n' +
                                'var data_high = ["Stock Prices.High",Chart.HIGH,Chart.AVERAGE_FORMULA];\n' +
                                'var data_low = ["Stock Prices.Low",Chart.LOW,Chart.AVERAGE_FORMULA];\n' +
                                'var data_close = ["Stock Prices.Close/Last",Chart.CLOSE,Chart.AVERAGE_FORMULA];\n' +
                                'var data_open = ["Stock Prices.Open",Chart.OPEN,Chart.AVERAGE_FORMULA]; \n' +
                                'Chart1.bindingInfo.xFields=[["Date",Chart.DATE]];\n' +
                                'Chart1.bindingInfo.setGroupOrder("Date",Chart.QUARTER_INTERVAL);\n' +
                                'Chart1.bindingInfo.setCandleBindingField(data_high);\n' +
                                'Chart1.bindingInfo.setCandleBindingField(data_low);\n'+
                                'Chart1.bindingInfo.setCandleBindingField(data_close);\n'+
                                'Chart1.bindingInfo.setCandleBindingField(data_open);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                'separatedStyle = Chart.CHART_CANDLE;\n' +
                                'var data_high = ["Discount",Chart.HIGH,Chart.SUM_FORMULA];\n' +
                                'bindingInfo.xFields = [["State",Chart.STRING]];\n' +
                                'bindingInfo.setCandleBindingField(data_high);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/StockingBanding', caseName)
      vsScriptTest.printVS('stock', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('candel', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-StockingAndCandel_stock',
                                  'TestCase-StockingAndCandel_candel'] as String[])
   }

   /**
    * test banding frame by bandinginfo existBug #45958
    */
   def 'TestCase-ChartFrame' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'bindingInfo.setColorField("Total",Chart.NUMBER);\n' +
                                 'bindingInfo.colorFrame =  new HeatColorFrame();\n' +
                                 'bindingInfo.setShapeField("State",Chart.STRING);\n' +
                                 'bindingInfo.lineFrame = new LinearLineFrame();']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'bindingInfo.textures["Sum(Total)"] = new StaticTextureFrame(GTexture.PATTERN_5);']]
      def TestData3 = [[HANDLER: 'Chart3',
                        COMMAND:'bindingInfo.setShapeField("Total",Chart.NUMBER);\n' +
                                'bindingInfo.shapeFrame = new TriangleShapeFrame();']]
      def TestData4 = [[HANDLER: 'Chart3',
                        COMMAND:'bindingInfo.setSizeField("Total",Chart.NUMBER);\n' +
                                'bindingInfo.sizeFrame = new LinearSizeFrame;\n' +
                                'bindingInfo.sizeFrame.smallest = 10;\n' +
                                'bindingInfo.sizeFrame.largest = 50;\n' +
                                'bindingInfo.sizeFrame.max = 100;']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ChartFrame', caseName)
      vsScriptTest.printVS('frame1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('frame2', TestData2, ['Chart2'] as String[])
      vsScriptTest.printVS('frame3', TestData3, ['Chart3'] as String[])
      vsScriptTest.printVS('frame4', TestData4, ['Chart3'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-ChartFrame_frame1',
                               'TestCase-ChartFrame_frame2',
                               'TestCase-ChartFrame_frame3',
                               'TestCase-ChartFrame_frame4'] as String[])
   }

   /**
    * test group info  by bandinginfo create chart
    */
   def 'TestCase-GroupInfo' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                 'separatedStyle = Chart.CHART_LINE;\n' +
                                 'bindingInfo.xFields = [["State",Chart.STRING]];\n' +
                                 'bindingInfo.yFields = [["Discount",Chart.NUMBER]];\n' +
                                 'bindingInfo.pathField = ["City",Chart.STRING];\n' +
                                 'bindingInfo.setTopN("State",3);\n' +
                                 'bindingInfo.setTopNReverse("State",true);\n' +
                                 'bindingInfo.setTopNSummaryCol("State","Sum(Discount)");']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ChartBandingInfo', caseName)
      vsScriptTest.printVS('GroupInfo1', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-GroupInfo_GroupInfo1'] as String[])
   }

   /**
    * test aggregate info  by bandinginfo create chart
    */
   def 'TestCase-AggregateInfo' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                 'bindingInfo.xFields = [["State",Chart.STRING]];\n' +
                                 'bindingInfo.yFields = [["Total",Chart.NUMBER]];\n' +
                                 'bindingInfo.setFormula("Total", Chart.MAX_FORMULA, Chart.BINDING_FIELD);\n' +
                                 'bindingInfo.setColorField("Paid",Chart.NUMBER);\n' +
                                 'bindingInfo.setFormula("Paid",Chart.COVARIANCE_FORMULA,Chart.AESTHETIC_COLOR);\n' +
                                 'bindingInfo.setSecondaryField("Paid","Discount",Chart.AESTHETIC_COLOR);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'query = "Query1";\n' +
                                'bindingInfo.xFields = [["State",Chart.STRING]];\n' +
                                'bindingInfo.yFields = [["Total",Chart.NUMBER]];\n' +
                                'bindingInfo.xFields = [["State",Chart.STRING]];\n' +
                                'bindingInfo.setPercentageType("Total", Chart.PERCENTAGE_OF_GROUP)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AggregateInfo', caseName)
      vsScriptTest.printVS('AggregateInfo1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('AggregateInfo2', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-AggregateInfo_AggregateInfo1'] as String[])
     vsScriptTest.compareImage(['TestCase-AggregateInfo_AggregateInfo2'] as String[])
   }

   /**
    * test  Discreate function when create chart by bandinginfo
    */
   def 'TestCase-Discreate' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'bindingInfo.setDiscrete("Total", true, Chart.BINDING_FIELD);\n' +
                                 'bindingInfo.setDiscrete("Paid", true, Chart.AESTHETIC_COLOR);\n' +
                                 'bindingInfo.setDiscrete("Reseller", true, Chart.AESTHETIC_SHAPE);\n' +
                                 'bindingInfo.setDiscrete("Quantity Purchased", true, Chart.AESTHETIC_SIZE);\n' +
                                 'bindingInfo.setDiscrete("Total", true, Chart.AESTHETIC_TEXT);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/SetDiscrete', caseName)
      vsScriptTest.printVS('discrete', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-Discreate_discrete'] as String[])
   }

   /**
    * test  getFunctions when create chart by bandinginfo
    */
   def 'TestCase-getFunctions' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Text1',
                        COMMAND: 'var txt1=Chart1.bindingInfo.getColumnOrder("State");\n' +
                                 'var txt2=Chart1.bindingInfo.getGroupOrder("Date",Chart.BINDING_FIELD);\n' +
                                 'var txt3=Chart1.bindingInfo.getTopN("State",Chart.BINDING_FIELD);\n' +
                                 'var txt4 = Chart1.bindingInfo.getFormula("Total",Chart.BINDING_FIELD);\n' +
                                 'var txt5 = Chart1.bindingInfo.getPercentageType("Total",Chart.BINDING_FIELD);\n' +
                                 'var txt6 = Chart1.colorField;\n' +
                                 'var txt7 = Chart1.shapeField;\n' +
                                 'var txt8 = Chart1.sizeField;\n' +
                                 'var txt9 = Chart1.textField;\n' +
                                 'var txt10 = Chart1.xFields[0];\n' +
                                 'var txt11 = Chart1.xFields[0];\n' +
                                 'Text1.text=txt1+" ;"+txt2+" ;"+txt3+" ;"+txt4+" ;"+txt5+" ;"+txt6+"; "+txt7+" ;"+txt8+" ;"+txt9+"; "+txt10+" ;"+txt11']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/getFunctions', caseName)
      vsScriptTest.printVS('getfieldinfo', TestData1, ['Chart1', 'Text1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-getFunctions_getfieldinfo'] as String[])

   }

   /**
    * test  addElement and addForm  when create chart by Egraph
    */

   def 'TestCase-EGraph1' () {
      given:
      caseName = specificationContext.currentIteration.name

      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new LineElement("State", "Quantity");\n' +
                                 'graph.addElement(elem);']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var form = new LineForm();\n' +
                                 'form.addPoint(new java.awt.Point(0,0));\n' +
                                 'form.addPoint(new java.awt.Point(100,100));\n' +
                                 'form.addPoint(new java.awt.Point(200,100)) ;\n' +
                                 'form.setFill(true);\n' +
                                 'graph.addForm(form);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('addelement', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('addform', TestData2, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-EGraph1_addelement'] as String[])
     vsScriptTest.compareImage(['TestCase-EGraph1_addform'] as String[])
   }

   /**
    * test  elemCount, getForm,formCount when create chart by Egraph
    */
   def 'TestCase-EGraph2' () {
      given:
      caseName = specificationContext.currentIteration.name

      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State","Total 1","Total 2"], ["NJ",200,500], ["NY",300,400]];\n' +
                                 'graph = new EGraph();\n' +
                                 'graph.addElement(new LineElement("State","Total 1"));\n' +
                                 'graph.addElement(new LineElement("State","Total 2"));\n'+
                                 'var elemCount = graph.getElementCount();\n'+
                                 'for (var i=0;i<elemCount;i++)\n'+
                                 '{graph.getElement(i).endArrow=true;}']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'graph.addElement(elem);\n' +
                                 'graph.addForm(new LineForm());\n' +
                                 'var form = graph.getForm(0);\n' +
                                 'form.addPoint(java.awt.Point(100,100));\n' +
                                 'form.addPoint(java.awt.Point(200,200));\n' +
                                 'form.setColor(java.awt.Color(0xff0000));']]
      def TestData3 = [[HANDLER: 'Chart1',
                         COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                  'graph = new EGraph();\n' +
                                  'var elem = new IntervalElement("State", "Quantity");\n' +
                                  'graph.addElement(elem);\n' +
                                  'graph.addForm(new LineForm());\n'+
                                  'graph.addForm(new LineForm());\n'+
                                  'graph.addForm(new LineForm());\n'+
                                  'var formCount = graph.getFormCount();\n' +
                                  'for (var i=0;i<formCount;i++) {\n' +
                                  'graph.getForm(i).addValues(["NJ",i*100]);\n' +
                                  'graph.getForm(i).addValues(["NY",300]);\n' +
                                  'graph.getForm(i).setColor(java.awt.Color(0xff0000));}']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('getelementcount', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('getform', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('getformcount', TestData3, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-EGraph2_getelementcount',
                                 'TestCase-EGraph2_getform',
                                 'TestCase-EGraph2_getformcount'] as String[])
   }

   /**
    * test getCoordinate, getscale when create chart by Egraph
    */

   def 'TestCase-EGraph3' () {
      given:
      caseName = specificationContext.currentIteration.name

      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'var coord = graph.getCoordinate(); \n' +
                                 'coord.transpose();']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'var scale = graph.getScale("Sum(Total)");\n' +
                                 'scale.setMin(600000);\n' +
                                 'scale.setMax(800000);']]
            when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph2', caseName)
      vsScriptTest.printVS('getcoordinate', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('getscale', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-EGraph3_getcoordinate',
                                 'TestCase-EGraph3_getscale'] as String[])
   }

   /**
    * test getCoordinate, getscale when create chart by Egraph
    */
   def 'TestCase-EGraph4' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ",200], ["NY",300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var frame = new CategoricalColorFrame();\n' +
                                 'frame.setField("State");\n' +
                                 'elem.setColorFrame(frame);\n' +
                                 'graph.setLegendLayout(Chart.BOTTOM);\n' +
                                 'graph.addElement(elem);\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'qscale.setMin(100);\n' +
                                 'qscale.setMax(500);\n' +
                                 'graph.setScale("Quantity", qscale);\n' +
                                 'var spec = new TitleSpec();\n' +
                                 'spec.setLabel("X Title")\n' +
                                 'graph.setXTitleSpec(spec);\n' +
                                 'var spec = new TitleSpec();\n' +
                                 'spec.setLabel("Y Title")\n' +
                                 'graph.setYTitleSpec(spec);\n']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'dataset = [["State", "Quantity"], ["NJ", 200], ["NY", 300]];\n' +
                                 'graph = new EGraph();\n' +
                                 'var elem = new IntervalElement("State", "Quantity");\n' +
                                 'var sscale = new CategoricalScale("State");\n' +
                                 'var qscale = new LinearScale("Quantity");\n' +
                                 'var coord = new RectCoord(sscale,qscale);\n' +
                                 'coord.transpose();\n' +
                                 'graph.setCoordinate(coord);\n' +
                                 'graph.addElement(elem);']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/EGraph1', caseName)
      vsScriptTest.printVS('lenlayout&titlespec', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('setCoordinate', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-EGraph4_lenlayout&titlespec',
                                 'TestCase-EGraph4_setCoordinate'] as String[])
   }



   /**
    * check set shape script on point chart  Bug #59119, Bug #59324
    */
   def 'TestCase_ShapseApply' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ShapseApply', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_ShapseApply_VS'] as String[])
   }
   static VSScriptTest vsScriptTest
   String caseName
}


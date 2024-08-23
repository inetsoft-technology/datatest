package inetsoft.test.vsscript.cases.Chart

import inetsoft.test.modules.VSScriptTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class ChartProperties_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test chart script which inherit from parent
    */
   def 'TestCase-Chart_Property' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'titleVisible=true;\n' +
                                 'title="OrderNumOfState"']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/title', caseName)
      vsScriptTest.printVS('title1', TestData1, ['Chart1'] as String[])
      then:
     vsScriptTest.compareImage(['TestCase-Chart_Property_title1'] as String[])
   }

   /**
    * Test  AxisScriptable on Axis|yAxis|y2Axis|xAxis
    */
   def 'TestCase-Chart_AxisProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'axis["State"].font = new java.awt.Font("Serif", java.awt.Font.BOLD, 12)\n' +
                                 'axis["Sum(Total)"].format = [Chart.DECIMAL_FORMAT,"#,###.00"]\n' +
                                 'axis["State"].rotation = 30\n' +
                                 'axis["Sum(Total)"].increment = 300000\n' +
                                 'axis["State"].labelColor = {r:255,g:0,b:0}\n' +
                                 'axis["Sum(Total)"].ticksVisible =true']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'axis["Sum(Paid)"].minimum = 500;;\n' +
                                'axis["Sum(Paid)"].maximum = 2000;\n' +
                                'axis["Year(Date)"].lineColor = {r:255,g:0,b:0}\n' +
                                'axis["Sum(Paid)"].increment =300']]
      def TestData3 = [[HANDLER: 'Chart3',
                        COMMAND: 'axis["Company"].labelVisible = false\n' +
                                 'axis["Company"].lineVisible= false\n' +
                                 'axis["Sum(Quantity Purchased)"].logarithmic = true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AxisProperty', caseName)
      vsScriptTest.printVS('axis1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('axis2', TestData2, ['Chart2'] as String[])
      vsScriptTest.printVS('axis3', TestData3, ['Chart3'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_AxisProperty_axis1',
                                 'TestCase-Chart_AxisProperty_axis2',
                                 'TestCase-Chart_AxisProperty_axis3'] as String[])
   }
   /**
    * Test  AxisScriptable on Axis|yAxis|y2Axis|xAxis another usage. see Bug #62163
    */
   def 'TestCase-Chart_AxisProperty2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                       COMMAND: 'xAxis.labelColor = \'red\'\n' +
                               'xAxis.lineColor = \'green\'\n' +
                               'xAxis.ticksVisible = true\n' +
                               'yAxis.labelColor = \'blue\'\n' +
                               'yAxis.lineColor = \'yellow\'\n' +
                               'yAxis.ticksVisible = true\n' +
                               'yAxis.sharedRange = false\n' +
                               'y2Axis.lineColor = \'orange\'\n' +
                               'y2Axis.labelColor = [199,133,200]\n' +
                               'y2Axis.ticksVisible = true\n' +
                               'y2Axis.minimum = -100']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'xAxis.labelVisible = false\n' +
                                'xAxis.ticksVisible = false\n' +
                                'yAxis.labelVisible = false\n' +
                                'yAxis.ticksVisible = false\n' +
                                'yAxis.sharedRange = false\n' +
                                'y2Axis.labelVisible = false\n' +
                                'y2Axis.ticksVisible = false']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AllTitleProperties', caseName)
      vsScriptTest.printVS('axis1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('axis2', TestData2, ['Chart1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Chart_AxisProperty2_axis1','TestCase-Chart_AxisProperty2_axis2'] as String[])
   }


   /**
    * Test  As TimeSeries function setting by script
    */
   def 'TestCase-TimeAxisProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'Chart1.bindingInfo.setTimeSeries("col0", true);']]
      def TestData2 = [[HANDLER: 'Text1',
                        COMMAND: 'Text1.value= Chart1.bindingInfo.isTimeSeries("col0");\n']]
     when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/TimeAxis', caseName)
      vsScriptTest.printVS('TimeAxistrue', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('IsTimeaxis', TestData2, ['Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-TimeAxisProperty_TimeAxistrue',
                                 'TestCase-TimeAxisProperty_IsTimeaxis'] as String[])
   }

   /**
   * Test SharedRange apply setting with script on factchart
   */
   def 'TestCase-SharedRange' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'yAxis.sharedRange = true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ShareRange', caseName)
      vsScriptTest.printVS('sharerange', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-SharedRange_sharerange'] as String[])
   }

     /**
    * Test ChartProcessor -> ScriptableObject on chartvalue
    */
   def 'TestCase-Chart_VauleProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'valueVisible = true;\n' +
                                 'valueColor = [255,0,0];\n' +
                                 'valueFont = new java.awt.Font("Serif", java.awt.Font.BOLD, 12);\n' +
                                 'Chart1.valueFormat = [Chart.MESSAGE_FORMAT,"{0}a"];\n' +
                                 'Chart1.valueRotation =10;']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'sortOthersLast=true;\n' +
                                 'rankPerGroup=true;']]
     when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/ValueProerty', caseName)
      vsScriptTest.printVS('value', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('option', TestData2, ['Chart2'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_VauleProperty_value',
                                 'TestCase-Chart_VauleProperty_option'] as String[])
   }

     /**
    * Test ChartProcessor -> ScriptableObject on x|x2|y|y2 title
    */
   def 'TestCase-Chart_TitleProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'xTitle.text = "StateGroup";\n' +
                                 'xTitle.color = [128,0,128];\n' +
                                 'xTitle.font = new java.awt.Font("Serif", java.awt.Font.BOLD, 15);\n' +
                                 'x2Title.text = "RegionGroup";\n' +
                                 'x2Title.color = [225,0,0];\n' +
                                 'x2Title.font = "Lucida Sans Unicode-BOLD-10";\n' +
                                 'x2Title.rotation = 360;\n' +
                                 'yTitle.text = "SummaryTotal"\n' +
                                 'yTitle.font = new java.awt.Font("Serif", java.awt.Font.BOLD, 12);\n' +
                                 'yTitle.rotation = 0;\n' +
                                 'y2Title.text="SumQuantity";\n' +
                                 'y2Title.color = [0,0,255];\n' +
                                 'y2Title.font = "Lucida Sans Unicode-BOLD-10";\n' +
                                 'y2Title.rotation =87;']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'Chart1.xTitle.visible = false;\n' +
                                'Chart1.x2Title.visible = false;\n' +
                                'Chart1.yTitle.visible = false;\n' +
                                'Chart1.y2Title.visible = false']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AllTitleProperties', caseName)
      vsScriptTest.printVS('title1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('titleinvisiable', TestData2, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_TitleProperty_title1',
                                 'TestCase-Chart_TitleProperty_titleinvisiable'] as String[])
   }

     /**
    * Test LengendProperties all properties
    */
   def 'TestCase-Chart_LengendProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'Chart1.legendPosition = Chart.LEFT;\n' +
                                 'Chart1.legendBorder = Chart.DASH_LINE;\n' +
                                 'Chart1.legendBorderColor = [255,0,0];\n' +
                                 'Chart1.legendTitleColor = [255,0,0]\n' +
                                 'Chart1.legendTitleFont = new java.awt.Font("Serif", java.awt.Font.BOLD, 12);']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'Chart2.colorLegend.title = "Paid";\n' +
                                'Chart2.colorLegend.color = {r:255,g:0,b:0}\n' +
                                'Chart2.colorLegend.font = new java.awt.Font("Serif", java.awt.Font.BOLD, 15);\n' +
                                'Chart2.sizeLegend.title = "Discount";\n' +
                                'Chart2.sizeLegend.font = "Comic Sans MS-BOLD-12";\n' +
                                'Chart2.sizeLegend.format = [Chart.DECIMAL_FORMAT,"##.00%"]']]
      def TestData3 = [[HANDLER: 'Chart3',
                        COMMAND: 'Chart3.shapeLegend.title = "Region"\n' +
                                 'Chart3.shapeLegend.color = {r:0,g:255,b:255}\n' +
                                 'Chart3.shapeLegend.font = "Comic Sans MS-BOLD-12"']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/LengendProperty', caseName)
      vsScriptTest.printVS('alllengend', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('ColorandSize', TestData2, ['Chart2'] as String[])
      vsScriptTest.printVS('Shape', TestData3, ['Chart3'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_LengendProperty_alllengend',
                                 'TestCase-Chart_LengendProperty_ColorandSize',
                                 'TestCase-Chart_LengendProperty_Shape'] as String[])
   }

     /**
    * Test LengendProperties Multi style chart
    */
   def 'TestCase-Chart_LengendsProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'colorLegends["Sum(Total)"].title ="TotalSummary";\n' +
                                 'colorLegends["Sum(Total)"].color = {r:255,g:0,b:255};\n' +
                                 'colorLegends["Sum(Paid)"].font = new java.awt.Font("Serif", java.awt.Font.BOLD, 10);\n' +
                                 'colorLegends["Sum(Paid)"].format =[Chart.DECIMAL_FORMAT,"$#,##0.00"]\n' +
                                 'shapeLegends["Sum(Total)"].title ="Test";\n' +
                                 'shapeLegends["Sum(Paid)"].color = {r:0,g:255,b:0};\n' +
                                 'shapeLegends["Sum(Total)"].font ="Comic Sans MS-BOLD-10";\n' +
                                 'shapeLegends["Sum(Paid)"].format = [Chart.DECIMAL_FORMAT,"#,###.#M"]']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND: 'sizeLegends["Sum(Total)"].title ="PaidSize"\n' +
                                'sizeLegends["Sum(Quantity Purchased)"].color = {r:0,g:255,b:255}\n' +
                                'sizeLegends["Sum(Total)"].font = "Comic Sans MS-BOLD-12"\n' +
                                'sizeLegends["Sum(Total)"].format =[Chart.DECIMAL_FORMAT,"$#,##0.00"]']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'colorLegends["Sum(Total)"].titleVisible =false;\n' +
                                'shapeLegends["Sum(Paid)"].titleVisible =false;']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/LengendsProperty', caseName)
      vsScriptTest.printVS('LengendsProperty1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('LengendsProperty2', TestData2, ['Chart2'] as String[])
      vsScriptTest.printVS('LengendsProperty3', TestData3, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_LengendsProperty_LengendsProperty1',
                                 'TestCase-Chart_LengendsProperty_LengendsProperty1',
                                 'TestCase-Chart_LengendsProperty_LengendsProperty3',] as String[])
   }

   /**
    * test ChartProcessor -> ScriptableObject Function addtarget()
    */
   def 'TestCase-Chart_Targetline1' () {
      given:
      caseName = specificationContext.currentIteration.name

      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND:
                                'var options = {fillAbove:[225,225,225],fillBelow:[225,225,225],label:["{1}: {0,number,$#,##0}","{1}:{0,number,$#,##0}"],lineColor:"red",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black"}\n' +
                                'addConfidenceIntervalTarget("Sum(Total)",0xDDAAAA,99,options)']]
      def TestData2 = [[HANDLER: 'Chart1',
                        COMMAND: 'var options = {fillAbove:[225,225,225],fillBelow:[225,225,225],label:["{1}: {0,number,$#,##0}"],lineColor:"red",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black",percentageAggregate:"Average"}\n' +
                                'addPercentageTarget("Sum(Total)","null",60,options)']]
      def TestData3 = [[HANDLER: 'Chart1',
                        COMMAND: 'var options = {fillAbove:[225,225,255],fillBelow:[225,225,225],label:["{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}","{1}:{0,number,$#,##0}"],lineColor:"red",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black"}\n' +
                                'addPercentileTarget("Sum(Total)",[0xDDAAAA,0xDDCCCC],[5,50],options)']]
      def TestData4 = [[HANDLER: 'Chart1',
                        COMMAND: 'var options = {fillAbove:[225,225,0],fillBelow:[0,225,225],label:["{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}","{1}:{0,number,$#,##0}"],lineColor:"green",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black"}\n' +
                                'addQuantileTarget("Sum(Total)",[0xDDAAAA,0xDDCCCC],3,options)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AddTarget', caseName)
      vsScriptTest.printVS('ConfidenceIntervalTarget', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('addPercentageTarget', TestData2, ['Chart1'] as String[])
      vsScriptTest.printVS('addPercentileTarget', TestData3, ['Chart1'] as String[])
      vsScriptTest.printVS('addQuantileTarget', TestData4, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_Targetline1_ConfidenceIntervalTarget',
                                'TestCase-Chart_Targetline1_addPercentageTarget',
                                'TestCase-Chart_Targetline1_addPercentileTarget',
                                'TestCase-Chart_Targetline1_addQuantileTarget'] as String[])
   }

   /**
    * test ChartProcessor -> ScriptableObject Function addtarget()
    */
   def 'TestCase-Chart_Targetline2' () {
      given:
      caseName = specificationContext.currentIteration.name

      def TestData1 = [[HANDLER: 'Chart2',
                        COMMAND:
                                'var options = {fillAbove:[225,225,225],fillBelow:[225,225,225],label:["{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}"],lineColor:"red",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black"}\n' +
                                'addStandardDeviationTarget("Sum(Total)",[0xDDCCCC,0xDDAAAA,0xDDCCCC],[-2,1,-1,2],options)']]
      def TestData2 = [[HANDLER: 'Chart3',
                        COMMAND: 'var options = {fillAbove:[225,225,225],fillBelow:[225,225,225],label:["{1}: {0,number,$#,##0}","{1}: {0,number,$#,##0}","{1}:{0,number,$#,##0}"],lineColor:"red",lineStyle:Chart.THICK_LINE,labelFont:"Lucida Sans Unicode-BOLD-11",labelColor:"black"}\n' +
                               'addTargetBand("Sum(Paid)",[0xDDAAAA,0xAAAADD],["min","avg"],options)']]
       def TestData3 = [[HANDLER: 'Chart4',
                        COMMAND: 'var options = {fillAbove:[200,200,255],fillBelow:[225,225,225],label:"Value: {0}",lineColor:"blue"}\n' +
                                'addTargetLine("Sum(Paid)",null,300,options)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/AddTarget', caseName)
      vsScriptTest.printVS('addStandardDeviationTarget', TestData1, ['Chart2'] as String[])
      vsScriptTest.printVS('addTargetBand', TestData2, ['Chart3'] as String[])
      vsScriptTest.printVS('addTargetLine', TestData3, ['Chart4'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_Targetline2_addStandardDeviationTarget',
                                'TestCase-Chart_Targetline2_addTargetBand',
                                'TestCase-Chart_Targetline2_addTargetLine'] as String[])
   }

   /**
    * test LengendAlias on chart
    */
   def 'TestCase-Chart_LengendAlias' () {
      given:
      caseName = specificationContext.currentIteration.name
       def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'setLabelAliasOfColorLegend("2017","Test");\n' +
                                 'setLabelAliasOfShapeLegend("USA East","East Region");\n' +
                                 'setLabelAliasOfSizeLegend("Personal","New Personal");']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/LengendAlias', caseName)
      vsScriptTest.printVS('AllAlias', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_LengendAlias_AllAlias'] as String[])
   }

     /**
    * test ChartProcessor -> ScriptableObject  Plot property
    */
   def 'TestCase-Chart_PlotProperty' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'xGridStyle=Chart.THICK_LINE;\n' +
                                 'yGridStyle=Chart.DOT_LINE;\n' +
                                 'xGridColor =0xFF0000;\n' +
                                 'Chart1.yGridColor ="green";\n' +
                                 'Chart1.diagonalStyle=Chart.DOT_LINE;\n' +
                                 'Chart1.diagonalColor=[255,255,0];\n' +
                                 'quadrantStyle=Chart.DOT_LINE;\n' +
                                 'xBandSize = 3;\n' +
                                 'yBandSize = 2;\n' +
                                 'Chart1.yBandColor = [220,220,255];\n' +
                                 'Chart1.xBandColor = "blue"']]
      def TestData2 = [[HANDLER: 'Chart2',
                        COMMAND:'mapDefaultColor="red";\n' +
                                'polygonColor = true']]
      def TestData3 = [[HANDLER: 'Chart3',
                        COMMAND: 'facetGrid=true;\n' +
                                 'facetGridColor="red";\n' +
                                 'plotBackground="green";\n' +
                                 'Chart1.plotAlpha =0.5;\n' +
                                 'pieExploded = true; pieRatio = 0.6']]
      def TestData4 = [[HANDLER: 'Chart4',
                        COMMAND:'inPlot=true']]
      def TestData5 = [[HANDLER: 'Chart5',
                        COMMAND:'borderColor="yellow"']]
      def TestData6 = [[HANDLER: 'Chart6',
                        COMMAND:'ApplyAestheticsToSource=true']]
     when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/PlotProperty', caseName)
      vsScriptTest.printVS('PlotProperty1', TestData1, ['Chart1'] as String[])
      vsScriptTest.printVS('PlotProperty2', TestData2, ['Chart2'] as String[])
      vsScriptTest.printVS('PlotProperty3', TestData3, ['Chart3'] as String[])
      vsScriptTest.printVS('PlotProperty4', TestData4, ['Chart4'] as String[])
      vsScriptTest.printVS('PlotProperty5', TestData5, ['Chart5'] as String[])
      vsScriptTest.printVS('PlotProperty6', TestData6, ['Chart6'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_PlotProperty_PlotProperty1',
                                 'TestCase-Chart_PlotProperty_PlotProperty2',
                                 'TestCase-Chart_PlotProperty_PlotProperty3',
                                 'TestCase-Chart_PlotProperty_PlotProperty4',
                                 'TestCase-Chart_PlotProperty_PlotProperty5',
                                 'TestCase-Chart_PlotProperty_PlotProperty6'] as String[])
   }

   /**
    * test ChartProcessor -> ScriptableObject  Plot property
    * add by bonnie on ver 13.5
    */
   def 'TestCase-Chart_PlotProperty2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def testData1 = [[HANDLER: 'Chart1',
                        COMMAND: "trendPerColor = false\n" +
                                 "trendLineColor = 'yellow'\n" +
                                 "trendLineStyle = StyleConstant.DOUBLE_LINE\n" +
                                 "projectTrendLineForward = '2'\n" +
                                 "pointLine = true"],
                       [HANDLER: 'RESULT',
                        COMMAND: "value = Chart1.trendPerColor + ', ' + Chart1.trendLineColor + ', ' + Chart1.trendLineStyle + ', ' +  Chart1.projectTrendLineForward  + ', ' + Chart1.pointLine"]]
      def testData2 = [[HANDLER: 'Chart2',
                        COMMAND: "stackMeasures = true\n" +
                                 "valueVisible = true\n" +
                                 "stackValue =true"],
                       [HANDLER: 'RESULT',
                       COMMAND: "value = 'Chart2 stackMeasures|valueVisible|stackValue is: \\n' + Chart2.stackMeasures +'|' + Chart2.valueVisible + '|' + Chart2.stackValue"]]
      def testData3 = [[HANDLER: 'Chart3',
                        COMMAND: 'contourLevels = 15\n' +
                                 'contourBandwidth = 30\n' +
                                 'contourEdgeAlpha = 0.25'],
                        [HANDLER: 'RESULT',
                         COMMAND: "value = Chart3.contourLevels + \",\" + Chart3.contourBandwidth + \", \" + Chart3.contourEdgeAlpha"]]
      def testData4 = [[HANDLER: 'Chart4',
                       COMMAND: 'includeParentLabels = true']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/PlotProperty2', caseName)
      vsScriptTest.printVS('Plot1', testData1, ['Chart1'] as String[])
      vsScriptTest.printVS('Plot2', testData2, ['Chart2'] as String[])
      vsScriptTest.printVS('Plot3', testData3, ['Chart3'] as String[])
      vsScriptTest.printVS('Plot4', testData4, ['Chart4'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_PlotProperty2_Plot1', 'TestCase-Chart_PlotProperty2_Plot2',
                                 'TestCase-Chart_PlotProperty2_Plot3', 'TestCase-Chart_PlotProperty2_Plot4'] as String[])
   }

   /**
    * test ChartProcessor -> ScriptableObject  Plot property
    * map level is only used by google map since it doesn't support setting zoom %, only pre-defined levels.
    * add by bonnie on ver 13.5
    */
   def 'TestCase-Chart_WebMap' () {
      given:
      caseName = specificationContext.currentIteration.name
      def testData1 = [[HANDLER: 'Chart1',
                       COMMAND: "webMap = true\n" +
                                "zoom = 1.2\n" +
                                "panX = 3.5\n" +
                                "panY = 4.5"]]
      def testData2 = [[HANDLER: 'Chart1',
                       COMMAND: "webMap = true\n" +
                                "webMacontourEdgeAlphap = true\n" +
                                "webMapStyle = 'Basic'\n" +
                                "zoom = 2"]]
      def testData3 = [[HANDLER: 'Chart1',
                        COMMAND: "webMap = true; webMapStyle = 'Outdoors'"]]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/webMap', caseName)
      vsScriptTest.printVS('WebMap1', testData1, ['Chart1'] as String[])
      vsScriptTest.printVS('WebMap2', testData2, ['Chart1'] as String[])
      vsScriptTest.printVS('WebMap3', testData3, ['Chart1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Chart_WebMap_WebMap1', 'TestCase-Chart_WebMap_WebMap2',
                                 'TestCase-Chart_WebMap_WebMap3'] as String[])
   }


   /**
    * on line, point chart, check fill blank, fillGapWithDash
    */
   def 'TestCase-Chart_FillBlank'() {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Chart1',
                        COMMAND: 'fillGapWithDash=true;fillTimeGap=true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Chart/LineChart', caseName)
      vsScriptTest.printVS('fillTimeGap', TestData1, ['Chart1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Chart_FillBlank_fillTimeGap'] as String[])
   }



   static VSScriptTest vsScriptTest
   String caseName
}

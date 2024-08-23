package inetsoft.test.css.cases

import inetsoft.test.modules.CSSTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Retry
import spock.lang.Specification

class VS_Component_Spec extends Specification {
   static CSSTest cssTest
   static String caseName

   def setupSpec() {
      CSSTest.initHome(this.class.getName())
   }

   def property1 = " { \n" +
          "   color: rgb(255,0,0);\n" +
          "   background-color: yellow;\n" +
          "   opacity:0.1;\n" +
          "   font-family: SansSerif;\n" +
          "   font-weight: bold;\n" +
          "   font-style: italic;\n" +
          "   font-size: 'small';\n" +
          "   border: double dotted orange;\n" +
          "   text-decoration: underline;\n" +
          "   text-align: center;\n" +
          "   vertical-align: middle;\n" +
          "   word-wrap: break-word;\n" +
          "}\n"

   @Retry(count = 3)
   def "test dataview component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Chart,Table,Crosstab,FreehandTable" + property1
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type1')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type2')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type3')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type4')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test filter component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "SelectionList,SelectionTree,RangeSlider,SelectionContainer,Calendar" + property1
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/filter')

      expect:
      cssTest.compareImage()
   }

   def "test form component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "CheckBox,RadioButton,Slider,Spinner,ComboBox,TextInput,Submit,Upload " + property1
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')

      expect:
      cssTest.compareImage()
   }

   def "test output and shape component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Gauge,Text,Image,Line,Rectangle,Oval " + property1 +
             "Rectangle {\n" +
             "   border-radius: 20px;\n" +
             "}\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/output_shape')

      expect:
      cssTest.compareImage()
   }

   def "test tab component and child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Tab" + property1 +
              "Tab[active=true] {\n" +
              "   background-color: green;\n" +
              "   opacity:1;\n" +
              "   border-top: 2px solid blue;\n" +
              "   border-radius: 5px;\n" +
              "}\n" +
             "Tab {\n" +
             "   border-radius: 20px;\n" +
             "}\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }

   def "test group component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "GroupContainer" + property1
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }

   def "test css for component title" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "ChartTitle,TableTitle,CrosstabTitle,FreehandTableTitle,SelectionListTitle,SelectionTreeTitle," +
          "SelectionContainerTitle,CalendarTitle,CheckBoxTitle,RadioButtonTitle { \n" +
          "   color: blue;\n" +
          "   background-color: #BAE3F7;\n" +
          "   opacity:0.1;\n" +
          "   font-family: SansSerif;\n" +
          "   font-weight: bold;\n" +
          "   font-style: italic;\n" +
          "   font-size: 11px;\n" +
          "   border: double dotted orange;\n" +
          "   text-decoration: line-through;\n" +
          "   word-wrap: break-word;\n" +
          "   padding-left: 30px;\n" +
          "   padding-top: 15px;\n" +
          "}"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')
      cssTest.VSCSSTest('1^128^__NULL__^vs/filter')
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')

      expect:
      cssTest.compareImage()
   }

   def "test selection list and selection tree child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "SelectionListCell {\n" +
             "   padding-top: 10px;\n" +
             "   padding-left: 20px;\n" +
             "   color: red;\n" +
             "   }\n" +
             "SelectionTreeCell {\n" +
             "   padding-top: 10px;\n" +
             "   padding-left: 20px;\n" +
             "   color: orange;\n" +
             "   }\n" +
             "SelectionTreeCell[level='0'] {\n" +
             "   color: red;\n" +
             "   text-align: right;\n" +
             "   }\n" +
             "SelectionTreeCell[level='1'] {\n" +
             "   color: blue;\n" +
             "   text-align: left;\n" +
             "   }\n" +
             "MeasureText {\n" +
             "   color: orange;\n" +
             "   }\n" +
             "MeasureText[level='0'] {\n" +
             "   color: red;\n" +
             "   text-align: right;\n" +
             "   }\n" +
             "MeasureText[level='1'] {\n" +
             "   color: blue;\n" +
             "   text-align: left;\n" +
             "   }\n" +
             "MeasureBar {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "MeasureBar[level='0'] {\n" +
             "   color: red;\n" +
             "   }\n" +
             "MeasureBar[level='1'] {\n" +
             "   color: blue;\n" +
             "   }\n" +
             "MeasureNBar {\n" +
             "   color: #BAE3F7;\n" +
             "   }\n" +
             "MeasureNBar[level='0'] {\n" +
             "   color: green;\n" +
             "   }\n" +
             "MeasureNBar[level='1'] {\n" +
             "   color: yellow;\n" +
             "   }\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection list')
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection tree')

      expect:
      cssTest.compareImage()
   }

   def "test calendar child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "CalendarHeader" + property1 +
             "CalendarMonths {\n" +
             "   color: blue;\n" +
             "   text-align: left;\n" +
             "   }\n" +
             "CalendarDays {\n" +
             "   color: green;\n" +
             "   text-decoration: line-through;\n" +
             "   text-align: right;\n" +
             "   }\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/calendar')

      expect:
      cssTest.compareImage()
   }

   def "test checkbox and radiobutton child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "CheckBoxCell,RadioButtonCell" + property1
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')

      expect:
      cssTest.compareImage()
   }

   def "test gauge child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "GaugeTickLabel {\n" +
                 "   color: blue;\n" +
                 "   font-family: Arial;\n" +
                 "   font-weight: bold;\n" +
                 "   font-style: italic;\n" +
                 "   font-size: 8px;\n" +
                "   }\n" +
                "GaugeValueLabel " + property1 +
                "GaugeValueFill {\n" +
                "   color: orange;\n" +
                "   }\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/gauge')

      expect:
      cssTest.compareImage()
   }

   //bug #59525，#59677
   @Retry(count = 3)
   def "test chart child selector1" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "ChartAxisTitle[axis=x]{\n" +
                "   color:green;\n" +
                "   background-color:#FFF0F5;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 10;\n" +
                "}\n" +
                "ChartAxisTitle[axis=x2]{\n" +
                "   color: orange;\n" +
                "   background-color: #FFE19B;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 10;\n" +
                "}\n" +
                "ChartAxisTitle[axis=y]{\n" +
                "   color: blue;\n" +
                "   background-color: #BAE3F7;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 10;\n" +
                "}\n" +
                "ChartAxisTitle[axis=y2]{\n" +
                "   color: black;\n" +
                "   background-color: #FFFAC3;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 10;\n" +
                "}\n" +
                "ChartAxisLabels[axis=x]{\n" +
                "   color: red;\n" +
                "   background-color: #ECEBE2;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_rotation: 45;\n" +
                "   label_gap: 15;\n" +
                "}\n" +
                "ChartAxisLabels[axis=x2]{\n" +
                "   color: green;\n" +
                "   background-color: #CCE6BF;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 5;\n" +
                "}\n" +
                "ChartAxisLabels[axis=y]{\n" +
                "   color: #FF00FF;\n" +
                "   background-color: pink;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_rotation: 90;\n" +
                "   label_gap: 10;\n" +
                "}\n" +
                "ChartAxisLabels[axis=y2]{ \n" +
                "   color: #00FF00;\n" +
                "   background-color: #C0DCC0;\n" +
                "   font-family: Arial;\n" +
                "   font-weight: bold;\n" +
                "   font-style: italic;\n" +
                "   font-size: 12px;\n" +
                "   text-decoration: underline;\n" +
                "   text-align: left;\n" +
                "   vertical-align: bottom;\n" +
                "   label_gap: 6;\n" +
                "}\n" +
                "ChartPlot {\n" +
                "   opacity: 0.5;\n" +
                "   background-color: #fff8de;\n" +
                "   line_x: MEDIUM_LINE;\n" +
                "   line_y: THICK_LINE;\n" +
                "   line_diagonal: DASH_LINE;\n" +
                "   line_quadrant: DOT_LINE;\n" +
                "   line_x_color: red;\n" +
                "   line_y_color: blue;\n" +
                "   line_diagonal_color: green;\n" +
                "   line_quadrant_color: orange;\n" +
                "   facet_grid_visible: true;\n" +
                "   facet_grid_color: black;\n" +
                "   pareto_line_color: red;\n" +
                "}\n" +
                "ChartLegend {\n" +
                "   border-color: red;\n" +
                "   border-style: dashed;\n" +
                "   padding: 5px;\n" +
                "   legend_gap: 20;\n" +
                "}\n"
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type1')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type2')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type3')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type4')

      expect:
      cssTest.compareImage()
   }

   //bug #59527
   @Retry(count = 3)
   def "test chart child selector2" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "ChartPlotLine[type=x] {\n" +
               "   color: magenta;\n" +
               "}\n" +
               "ChartPlotLine[type=y] {\n" +
               "   color: darkgreen;\n" +
               "}\n" +
               "ChartPlotLine[type=diagonal] {\n" +
               "   color: orange;\n" +
               "}\n" +
               "ChartPlotLine[type=quadrant] {\n" +
               "   color: red;\n" +
               "}\n" +
               "ChartPlotLabels {\n" +
               "   color: #00FFFF;\n" +
               "   background-color: #EFF4F8;\n" +
               "   font-family: Arial;\n" +
               "   font-weight: bold;\n" +
               "   font-style: italic;\n" +
               "   font-size: 12px;\n" +
               "   text-decoration: underline;\n" +
               "   text-align: left;\n" +
               "   vertical-align: bottom;\n" +
               "}\n" +
               "ChartTargetLabels {\n" +
               "   background-color: yellow;\n" +
               "   font-size: 12px;\n" +
               "   text-decoration: line-through;\n" +
               "}\n" +
               "ChartTargetLabels[index='0'] {\n" +
               "   color: pink;\n" +
               "   background-color: #EFF4F8;\n" +
               "   font-family: Arial;\n" +
               "   font-weight: bold;\n" +
               "   font-style: italic;\n" +
               "   font-size: 14px;\n" +
               "   text-decoration: underline;\n" +
               "   text-align: left;\n" +
               "   vertical-align: bottom;\n" +
               "}\n" +
               "ChartTargetLabels[index='1'] {\n" +
               "   color: blue;\n" +
               "}\n" +
               "ChartPalette[index='1'] {\n" +
               "   color: orange;\n" +
               "}\n" +
               "ChartPalette[index='2'] {\n" +
               "   color: purple;\n" +
               "}\n" +
               "ChartLegendTitle {\n" +
               "   color: #4C50A2;\n" +
               "   background-color: #EFF4F8;\n" +
               "   font-family: Arial;\n" +
               "   font-weight: bold;\n" +
               "   font-style: italic;\n" +
               "   font-size: 14px;\n" +
               "   text-decoration: underline;\n" +
               "   text-align: left;\n" +
               "   vertical-align: top;\n" +
               "}\n" +
               "ChartLegendContent {\n" +
               "   color: #009999;\n" +
               "   background-color: yellow;\n" +
               "   font-weight: bold;\n" +
               "   font-style: italic;\n" +
               "   font-size: 12px;\n" +
               "   text-decoration: line-through;\n" +
               "   text-align: right;\n" +
               "   vertical-align: bottom;\n" +
               "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type1')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type2')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type3')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type4')

      expect:
      cssTest.compareImage()
    }

   //bug #59655，#59657, #60078, #60079
   @Retry(count = 3)
   def "test type option works on chart component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def type1 = "Chart[type=VBar],Chart[type=HBar],Chart[type=Area],Chart[type=Line],Chart[type=JumpLine]," +
             "Chart[type=StepLine],Chart[type=Point],Chart[type=Waterfall],Chart[type=Pareto],Chart[type=StepArea]," +
             "Chart[type=Scatter],Chart[type=Stock],Chart[type=Candle],Chart[type=Multi],Chart[type=BoxPlot]," +
             "Chart[type=Marimekko],Chart[type=Heatmap],Chart[type=Funnel],Chart[type=ScatterContour],Chart[type=Gantt], " +
             "Chart[type=Treemap],Chart[type=Sunburst],Chart[type=CirclePacking],Chart[type=Icicle],Chart[type=Map]," +
             "Chart[type=Pie],Chart[type=Network],Chart[type=Tree],Chart[type=Radar],Chart[type=CircularNetwork]," +
             "Chart[type=ContourMap],Chart[type=Donut], Chart[type=Interval] "

      def css1 = type1 + property1

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type1')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type2')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type3')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type4')

      expect:
      cssTest.compareImage()
    }

   @Retry(count = 3)
   def "test type option works on chart child selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Chart[type=VBar] ChartTargetLabels,Chart[type=HBar] ChartTargetLabels," +
                "Chart[type=Area] ChartTargetLabels,Chart[type=Line] ChartTargetLabels," +
                "Chart[type=JumpLine] ChartTargetLabels,Chart[type=StepLine] ChartTargetLabels," +
                "Chart[type=Point] ChartTargetLabels,Chart[type=Waterfall] ChartTargetLabels," +
                "Chart[type=Pareto] ChartTargetLabels,Chart[type=StepArea] ChartTargetLabels," +
                "Chart[type=Scatter] ChartTargetLabels,Chart[type=Stock] ChartTargetLabels," +
                "Chart[type=Candle] ChartTargetLabels,Chart[type=Multi] ChartTargetLabels, " +
                "Chart[type=Interval] ChartTargetLabels {\n" +
                         "   color: red;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartAxisTitle[axis=x],Chart[type=HBar] ChartAxisTitle[axis=x]," +
                 "Chart[type=Area] ChartAxisTitle[axis=x],Chart[type=Line] ChartAxisTitle[axis=x]," +
                 "Chart[type=JumpLine] ChartAxisTitle[axis=x],Chart[type=StepLine] ChartAxisTitle[axis=x]," +
                 "Chart[type=Point] ChartAxisTitle[axis=x],Chart[type=Waterfall] ChartAxisTitle[axis=x]," +
                 "Chart[type=Pareto] ChartAxisTitle[axis=x],Chart[type=StepArea] ChartAxisTitle[axis=x]," +
                 "Chart[type=Scatter] ChartAxisTitle[axis=x],Chart[type=Stock] ChartAxisTitle[axis=x]," +
                 "Chart[type=Candle] ChartAxisTitle[axis=x],Chart[type=Multi] ChartAxisTitle[axis=x]," +
                 "Chart[type=BoxPlot] ChartAxisTitle[axis=x],Chart[type=Marimekko] ChartAxisTitle[axis=x]," +
                 "Chart[type=Heatmap] ChartAxisTitle[axis=x],Chart[type=Funnel] ChartAxisTitle[axis=x]," +
                 "Chart[type=ScatterContour] ChartAxisTitle[axis=x],Chart[type=Gantt] ChartAxisTitle[axis=x]," +
                 "Chart[type=Interval] ChartAxisTitle[axis=x] {\n" +
                         "   font-weight: bold;\n" +
                         "   color: red;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartAxisLabels[axis=y],Chart[type=HBar] ChartAxisLabels[axis=y]," +
                 "Chart[type=Area] ChartAxisLabels[axis=y],Chart[type=Line] ChartAxisLabels[axis=y]," +
                 "Chart[type=JumpLine] ChartAxisLabels[axis=y],Chart[type=StepLine] ChartAxisLabels[axis=y]," +
                 "Chart[type=Point] ChartAxisLabels[axis=y],Chart[type=Waterfall] ChartAxisLabels[axis=y]," +
                 "Chart[type=Pareto] ChartAxisLabels[axis=y],Chart[type=StepArea] ChartAxisLabels[axis=y]," +
                 "Chart[type=Scatter] ChartAxisLabels[axis=y],Chart[type=Stock] ChartAxisLabels[axis=y]," +
                 "Chart[type=Candle] ChartAxisLabels[axis=y],Chart[type=Multi] ChartAxisLabels[axis=y]," +
                 "Chart[type=BoxPlot] ChartAxisLabels[axis=y],Chart[type=Marimekko] ChartAxisLabels[axis=y]," +
                 "Chart[type=Heatmap] ChartAxisLabels[axis=y],Chart[type=Funnel] ChartAxisLabels[axis=y]," +
                 "Chart[type=ScatterContour] ChartAxisLabels[axis=y],Chart[type=Gantt] ChartAxisLabels[axis=y]," +
                 "Chart[type=Interval] ChartAxisLabels[axis=y] {\n" +
                         "   color: blue;\n" +
                         "   label_rotation: 90;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartPlot,Chart[type=HBar] ChartPlot,Chart[type=Area] ChartPlot," +
                 "Chart[type=Line] ChartPlot,Chart[type=JumpLine] ChartPlot,Chart[type=StepLine] ChartPlot," +
                 "Chart[type=Point] ChartPlot,Chart[type=Waterfall] ChartPlot,Chart[type=Pareto] ChartPlot," +
                 "Chart[type=StepArea] ChartPlot,Chart[type=Scatter] ChartPlot,Chart[type=Stock] ChartPlot," +
                 "Chart[type=Candle] ChartPlot,Chart[type=Multi] ChartPlot,Chart[type=BoxPlot] ChartPlot," +
                 "Chart[type=Marimekko] ChartPlot,Chart[type=Heatmap] ChartPlot,Chart[type=Funnel] ChartPlot," +
                 "Chart[type=ScatterContour] ChartPlot,Chart[type=Gantt] ChartPlot, Chart[type=Interval] ChartPlot," +
                 "Chart[type=Treemap] ChartPlot,Chart[type=Sunburst] ChartPlot,Chart[type=CirclePacking] ChartPlot," +
                 "Chart[type=Icicle] ChartPlot,Chart[type=Map] ChartPlot, Chart[type=Pie] ChartPlot," +
                 "Chart[type=Network] ChartPlot,Chart[type=Tree] ChartPlot,Chart[type=Radar] ChartPlot," +
                 "Chart[type=CircularNetwork] ChartPlot, Chart[type=ContourMap] ChartPlot,Chart[type=Donut] ChartPlot {\n" +
                         "   opacity: 0.5;\n" +
                         "   background-color: #fff8de;\n" +
                         "   line_x: MEDIUM_LINE;\n" +
                         "   line_y: THICK_LINE;\n" +
                         "   line_x_color: red;\n" +
                         "   line_y_color: blue;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartPlotLabels,Chart[type=HBar] ChartPlotLabels,Chart[type=Area] ChartPlotLabels," +
                 "Chart[type=Line] ChartPlotLabels,Chart[type=JumpLine] ChartPlotLabels,Chart[type=StepLine] ChartPlotLabels," +
                 "Chart[type=Point] ChartPlotLabels,Chart[type=Waterfall] ChartPlotLabels,Chart[type=Pareto] ChartPlotLabels," +
                 "Chart[type=StepArea] ChartPlotLabels,Chart[type=Scatter] ChartPlotLabels,Chart[type=Stock] ChartPlotLabels," +
                 "Chart[type=Candle] ChartPlotLabels,Chart[type=Multi] ChartPlotLabels,Chart[type=BoxPlot] ChartPlotLabels," +
                 "Chart[type=Marimekko] ChartPlotLabels,Chart[type=Heatmap] ChartPlotLabels,Chart[type=Funnel] ChartPlotLabels," +
                 "Chart[type=ScatterContour] ChartPlotLabels,Chart[type=Gantt] ChartPlotLabels, Chart[type=Interval] ChartPlotLabels," +
                 "Chart[type=Treemap] ChartPlotLabels,Chart[type=Sunburst] ChartPlotLabels,Chart[type=CirclePacking] ChartPlotLabels," +
                 "Chart[type=Icicle] ChartPlotLabels,Chart[type=Map] ChartPlotLabels, Chart[type=Pie] ChartPlotLabels," +
                 "Chart[type=Network] ChartPlotLabels,Chart[type=Tree] ChartPlotLabels,Chart[type=Radar] ChartPlotLabels," +
                 "Chart[type=CircularNetwork] ChartPlotLabels, Chart[type=ContourMap] ChartPlotLabels,Chart[type=Donut] ChartPlotLabels {\n" +
                         "   color: #00FFFF;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartLegend,Chart[type=HBar] ChartLegend,Chart[type=Area] ChartLegend," +
                 "Chart[type=Line] ChartLegend,Chart[type=JumpLine] ChartLegend,Chart[type=StepLine] ChartLegend," +
                 "Chart[type=Point] ChartLegend,Chart[type=Waterfall] ChartLegend,Chart[type=Pareto] ChartLegend," +
                 "Chart[type=StepArea] ChartLegend,Chart[type=Scatter] ChartLegend,Chart[type=Stock] ChartLegend," +
                 "Chart[type=Candle] ChartLegend,Chart[type=Multi] ChartLegend,Chart[type=BoxPlot] ChartLegend," +
                 "Chart[type=Marimekko] ChartLegend,Chart[type=Heatmap] ChartLegend,Chart[type=Funnel] ChartLegend," +
                 "Chart[type=ScatterContour] ChartLegend,Chart[type=Gantt] ChartLegend, Chart[type=Interval] ChartLegend," +
                 "Chart[type=Treemap] ChartLegend,Chart[type=Sunburst] ChartLegend,Chart[type=CirclePacking] ChartLegend," +
                 "Chart[type=Icicle] ChartLegend,Chart[type=Map] ChartLegend, Chart[type=Pie] ChartLegend," +
                 "Chart[type=Network] ChartLegend,Chart[type=Tree] ChartLegend,Chart[type=Radar] ChartLegend," +
                 "Chart[type=CircularNetwork] ChartLegend, Chart[type=ContourMap] ChartLegend,Chart[type=Donut] ChartLegend {\n" +
                         "   border-color: red;\n" +
                         "   border-style: dashed;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartLegendTitle,Chart[type=HBar] ChartLegendTitle,Chart[type=Area] ChartLegendTitle," +
                 "Chart[type=Line] ChartLegendTitle,Chart[type=JumpLine] ChartLegendTitle,Chart[type=StepLine] ChartLegendTitle," +
                 "Chart[type=Point] ChartLegendTitle,Chart[type=Waterfall] ChartLegendTitle,Chart[type=Pareto] ChartLegendTitle," +
                 "Chart[type=StepArea] ChartLegendTitle,Chart[type=Scatter] ChartLegendTitle,Chart[type=Stock] ChartLegendTitle," +
                 "Chart[type=Candle] ChartLegendTitle,Chart[type=Multi] ChartLegendTitle,Chart[type=BoxPlot] ChartLegendTitle," +
                 "Chart[type=Marimekko] ChartLegendTitle,Chart[type=Heatmap] ChartLegendTitle,Chart[type=Funnel] ChartLegendTitle," +
                 "Chart[type=ScatterContour] ChartLegendTitle,Chart[type=Gantt] ChartLegendTitle, Chart[type=Interval] ChartLegendTitle," +
                 "Chart[type=Treemap] ChartLegendTitle,Chart[type=Sunburst] ChartLegendTitle,Chart[type=CirclePacking] ChartLegendTitle," +
                 "Chart[type=Icicle] ChartLegendTitle,Chart[type=Map] ChartLegendTitle, Chart[type=Pie] ChartLegendTitle," +
                 "Chart[type=Network] ChartLegendTitle,Chart[type=Tree] ChartLegendTitle,Chart[type=Radar] ChartLegendTitle," +
                 "Chart[type=CircularNetwork] ChartLegendTitle, Chart[type=ContourMap] ChartLegendTitle,Chart[type=Donut] ChartLegendTitle {\n" +
                         "   color: #4C50A2;\n" +
                         "   color: blue;\n" +
                         "   font-weight: bold;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartLegendContent,Chart[type=HBar] ChartLegendContent,Chart[type=Area] ChartLegendContent," +
                 "Chart[type=Line] ChartLegendContent,Chart[type=JumpLine] ChartLegendContent,Chart[type=StepLine] ChartLegendContent," +
                 "Chart[type=Point] ChartLegendContent,Chart[type=Waterfall] ChartLegendContent,Chart[type=Pareto] ChartLegendContent," +
                 "Chart[type=StepArea] ChartLegendContent,Chart[type=Scatter] ChartLegendContent,Chart[type=Stock] ChartLegendContent," +
                 "Chart[type=Candle] ChartLegendContent,Chart[type=Multi] ChartLegendContent,Chart[type=BoxPlot] ChartLegendContent," +
                 "Chart[type=Marimekko] ChartLegendContent,Chart[type=Heatmap] ChartLegendContent,Chart[type=Funnel] ChartLegendContent," +
                 "Chart[type=ScatterContour] ChartLegendContent,Chart[type=Gantt] ChartLegendContent, Chart[type=Interval] ChartLegendContent," +
                 "Chart[type=Treemap] ChartLegendContent,Chart[type=Sunburst] ChartLegendContent,Chart[type=CirclePacking] ChartLegendContent," +
                 "Chart[type=Icicle] ChartLegendContent,Chart[type=Map] ChartLegendContent, Chart[type=Pie] ChartLegendContent," +
                 "Chart[type=Network] ChartLegendContent,Chart[type=Tree] ChartLegendContent,Chart[type=Radar] ChartLegendContent," +
                 "Chart[type=CircularNetwork] ChartLegendContent, Chart[type=ContourMap] ChartLegendContent,Chart[type=Donut] ChartLegendContent {\n" +
                         "   color: #009999;\n" +
                         "   text-decoration: underline;\n" +
                         "   text-align: left;\n" +
                         "}\n" +
                 "Chart[type=VBar] ChartPalette[index='1'],Chart[type=HBar] ChartPalette[index='1']," +
                 "Chart[type=Area] ChartPalette[index='1'],Chart[type=Line] ChartPalette[index='1']," +
                 "Chart[type=JumpLine] ChartPalette[index='1'],Chart[type=StepLine] ChartPalette[index='1']," +
                 "Chart[type=Point] ChartPalette[index='1'],Chart[type=Waterfall] ChartPalette[index='1']," +
                 "Chart[type=Pareto] ChartPalette[index='1'],Chart[type=StepArea] ChartPalette[index='1']," +
                 "Chart[type=Scatter] ChartPalette[index='1'],Chart[type=Stock] ChartPalette[index='1']," +
                 "Chart[type=Candle] ChartPalette[index='1'],Chart[type=Multi] ChartPalette[index='1']," +
                 "Chart[type=BoxPlot] ChartPalette[index='1'],Chart[type=Marimekko] ChartPalette[index='1']," +
                 "Chart[type=Heatmap] ChartPalette[index='1'],Chart[type=Funnel] ChartPalette[index='1']," +
                 "Chart[type=ScatterContour] ChartPalette[index='1'],Chart[type=Gantt] ChartPalette[index='1']," +
                 "Chart[type=Interval] ChartPalette[index='1'],Chart[type=Treemap] ChartPalette[index='1']," +
                 "Chart[type=Sunburst] ChartPalette[index='1'],Chart[type=CirclePacking] ChartPalette[index='1']," +
                 "Chart[type=Icicle] ChartPalette[index='1'],Chart[type=Map] ChartPalette[index='1']," +
                 "Chart[type=Pie] ChartPalette[index='1'],Chart[type=Network] ChartPalette[index='1']," +
                 "Chart[type=Tree] ChartPalette[index='1'],Chart[type=Radar] ChartPalette[index='1']," +
                 "Chart[type=CircularNetwork] ChartPalette[index='1'], Chart[type=ContourMap] ChartPalette[index='1']," +
                 "Chart[type=Donut] ChartPalette[index='1'] {\n" +
                         "   color: orange;\n" +
                         "}\n" +
                 "Chart[type=Map] ChartPlot {\n" +
                         "   map_color: green;\n" +
                         "}\n" +
                 "Chart[type=Pie] ChartPlot {\n" +
                         "   explode_pie: true;\n" +
                         "}\n" +
                 "Chart[type=Multi] ChartAxisTitle[axis=y2] {\n" +
                         "   color: orange;\n" +
                         "   font-weight: bold;\n" +
                         "   font-style: italic;\n" +
                         "}\n" +
                 "Chart[type=Multi] ChartAxisLabels[axis=y2] {\n" +
                         "   color: orange;\n" +
                         "   font-weight: bold;\n" +
                         "   font-style: italic;\n" +
                         "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type1')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type2')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type3')
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart_type4')

      expect:
      cssTest.compareImage()
    }
}
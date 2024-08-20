package inetsoft.test.css.cases

import inetsoft.test.css.CSSTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class VS_Usage_Spec extends Specification {
   static CSSTest cssTest
   static String caseName

   def setupSpec() {
      CSSTest.initHome(this.class.getName())
   }

   def property1 = " { \n" +
          "   color: blue;\n" +
          "   background-color: #BAE3F7;\n" +
          "   font-size: 12px;\n" +
          "   text-decoration: line-through;\n" +
          "}\n"
   def property2 = " { \n" +
          "   color: red;\n" +
          "   background-color: yellow;\n" +
          "   font-size: 14px;\n" +
          "   text-decoration: underline;\n" +
          "}\n"

   def "test default and component selector" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "*" + property1 + "Chart,Table,Crosstab,FreehandTable,SelectionList,SelectionTree,RangeSlider," +
            "SelectionContainer,Calendar,CheckBox,RadioButton,Slider,Spinner,ComboBox,TextInput,Submit," +
            "Upload,Gauge,Text,Image,Line,Rectangle,Oval,Tab,GroupContainer" + property2;
      
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')
      cssTest.VSCSSTest('1^128^__NULL__^vs/filter')
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')
      cssTest.VSCSSTest('1^128^__NULL__^vs/output_shape')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }

   def "test default and component child selector for chart" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "*" + property1 + "ChartTitle" + property2 +
                "ChartAxisTitle[axis=x]{\n" +
                "   color:green;\n" +
                "}\n" +
                "ChartAxisLabels[axis=y]{\n" +
                "   color: #FF00FF;\n" +
                "   background-color: pink;\n" +
                "}\n" +
               "ChartLegendTitle {\n" +
               "   color: #4C50A2;\n" +
                "}\n" +
               "ChartTargetLabels[index='1'] {\n" +
               "   color: #FF00FF;\n" +
               "}\n" +
               "ChartPlotLine[type=y] {\n" +
               "   color: darkgreen;\n" +
               "}\n"
      
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')

      expect:
      cssTest.compareImage()
   }

   def "test default and component child selector for tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "*" + property1 + "TableTitle,CrosstabTitle,FreehandTableTitle" + property2;

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test default and component child selector for filter" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "*" + property1 + "SelectionListTitle,SelectionTreeTitle," +
             "SelectionContainerTitle,CalendarTitle" + property2 +
             "SelectionListCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "SelectionTreeCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "MeasureText {\n" +
             "   color: orange;\n" +
             "   }\n" +
             "CalendarHeader {\n" +
             "   color: purple;\n" +
             "   }\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection list')
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection tree')
      cssTest.VSCSSTest('1^128^__NULL__^vs/calendar')

      expect:
      cssTest.compareImage()
   }

   def "test default and component child selector for form gauge and tab" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "*" + property1 + "CheckBoxTitle,RadioButtonTitle,GaugeValueLabel" + property2 +
              "Tab[active=true] {\n" +
              "   color: red;\n" +
              "   background-color: green;\n" +
              "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')
      cssTest.VSCSSTest('1^128^__NULL__^vs/gauge')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }

   def "test component and component child selector for chart" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Chart" + property1 + "ChartTitle" + property2 +
                "ChartAxisTitle[axis=x]{\n" +
                "   color:green;\n" +
                "}\n" +
                "ChartAxisLabels[axis=y]{\n" +
                "   color: #FF00FF;\n" +
                "   background-color: pink;\n" +
                "}\n" +
               "ChartLegendTitle {\n" +
               "   color: #4C50A2;\n" +
                "}\n" +
               "ChartTargetLabels[index='1'] {\n" +
               "   color: #FF00FF;\n" +
               "}\n" +
               "ChartPlotLine[type=y] {\n" +
               "   color: darkgreen;\n" +
               "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')

      expect:
      cssTest.compareImage()
   }

   def "test component and component child selector for tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Table,Crosstab,FreehandTable" + property1 +
                 "TableTitle,CrosstabTitle,FreehandTableTitle" + property2;

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test component and component child selector for filter" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "SelectionList,SelectionTree,SelectionContainer,Calendar" + property1 +
          "SelectionListTitle,SelectionTreeTitle,SelectionContainerTitle,CalendarTitle" + property2 +
             "SelectionListCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "SelectionTreeCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "MeasureText {\n" +
             "   color: orange;\n" +
             "   }\n" +
             "CalendarHeader {\n" +
             "   color: purple;\n" +
             "   }\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection list')
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection tree')
      cssTest.VSCSSTest('1^128^__NULL__^vs/calendar')

      expect:
      cssTest.compareImage()
   }

   def "test component and component child selector for form gauge and tab" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "CheckBox,RadioButton,Gauge,Tab" + property1 +
              "CheckBoxTitle,RadioButtonTitle,GaugeValueLabel" + property2 +
              "Tab[active=true] {\n" +
              "   color: purple;\n" +
              "   background-color: green;\n" +
              "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')
      cssTest.VSCSSTest('1^128^__NULL__^vs/gauge')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }

   def "test default selector component and component child selector for chart" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Chart" + property1 + "ChartTitle" + property2 +
                "ChartAxisTitle[axis=x]{\n" +
                "   color:green;\n" +
                "}\n" +
                "ChartAxisLabels[axis=y]{\n" +
                "   color: #FF00FF;\n" +
                "   background-color: pink;\n" +
                "}\n" +
               "ChartLegendTitle {\n" +
               "   color: #4C50A2;\n" +
                "}\n" +
               "ChartTargetLabels[index='1'] {\n" +
               "   color: #FF00FF;\n" +
               "}\n" +
               "ChartPlotLine[type=y] {\n" +
               "   color: darkgreen;\n" +
               "}\n" +
               "* { \n" +
               "   border: double dotted orange;\n" +
               "   text-align: right;\n" +
               "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')

      expect:
      cssTest.compareImage()
   }

   def "test default selector component and component child selector for tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "Table,Crosstab,FreehandTable" + property1 +
                 "TableTitle,CrosstabTitle,FreehandTableTitle" + property2 +
                 "* { \n" +
                 "   border: double dotted orange;\n" +
                 "   text-align: right;\n" +
                 "}\n";

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test default selector component and component child selector for filter" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "SelectionList,SelectionTree,SelectionContainer,Calendar" + property1 +
          "SelectionListTitle,SelectionTreeTitle,SelectionContainerTitle,CalendarTitle" + property2 +
             "SelectionListCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "SelectionTreeCell {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "MeasureText {\n" +
             "   color: orange;\n" +
             "   }\n" +
             "CalendarHeader {\n" +
             "   color: purple;\n" +
             "   }\n" +
             "* { \n" +
             "   border: double dotted orange;\n" +
             "   text-align: right;\n" +
             "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection list')
      cssTest.VSCSSTest('1^128^__NULL__^vs/selection tree')
      cssTest.VSCSSTest('1^128^__NULL__^vs/calendar')

      expect:
      cssTest.compareImage()
   }

   def "test default selector component and component child selector for form gauge and tab" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css1 = "CheckBox,RadioButton,Gauge,Tab" + property1 +
              "CheckBoxTitle,RadioButtonTitle,GaugeValueLabel" + property2 +
              "Tab[active=true] {\n" +
              "   color: purple;\n" +
              "   background-color: green;\n" +
              "}\n" +
              "* { \n" +
              "   border: double dotted orange;\n" +
              "   text-align: right;\n" +
              "}\n"

      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')
      cssTest.VSCSSTest('1^128^__NULL__^vs/gauge')
      cssTest.VSCSSTest('1^128^__NULL__^vs/tab_group')

      expect:
      cssTest.compareImage()
   }
}
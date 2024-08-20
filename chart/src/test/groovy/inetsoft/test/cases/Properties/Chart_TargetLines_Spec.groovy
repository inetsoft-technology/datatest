package inetsoft.test.cases.Properties

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.Specification

/*
 check chart targetline,include 23 case
*/
class Chart_TargetLines_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    1.Test target line of Line type apply in face Area chart
    2.check field is number type and vlaue is Enter,Average,Min,Max,Median,Sum
    3.check target line of keep element in plot
   */
   def 'Line_Area'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_Area', caseName)
      vstest.executeVS(null, ['Home', 'Average', 'Median', 'Min', 'Sum','InputMax','inplot_false'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in face Auto chart
    2.check field is date type and level is week and vlaue is Enter,Min,Max
   */
   def 'Line_Auto'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_Auto', caseName)
      vstest.executeVS(null, ['Home', 'Min', 'Input','InputMin','InputMax'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in Bar chart
    2.check field is time type and level is Hour
   */
   def 'Line_Candle'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_Candle', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in StepArea chart
    2.check field is time type and level is second and vlaue is Enter,Min,Max
   */
   def 'Line_StepArea'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_StepArea', caseName)
      vstest.executeVS(null, ['Home', 'Min', 'Input'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in JumpLine chart
    2.check field is date type and level is Quarter
   */
   def 'Line_JumpLine'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_JumpLine', caseName)
      vstest.executeVS(null, ['Home', 'TargetValue', 'TargetFormula', 'FieldName'] as String[])
      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in 3Dbar chart
    2.check field is number type and trend and comparison is Running
   */
   def 'Line_3Dbar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_3Dbar', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in BoxPlot chart
    2.check field is number type
   */
   def 'Line_BoxPlot'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_BoxPlot', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Line type apply in Stock chart
    2.check field is date type and level is year,value set trend line generate date
   */
   def 'Line_Stock'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Line_Stock', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in Line chart
    2.check field is timeinstant type and level is month
   */
   def 'Band_Line'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_Line', caseName)
      vstest.executeVS(null, ['Home','B1','B2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in JumpLine chart
    2.check field is number type
   */
   def 'Band_JumpLine'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_JumpLine', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in Bar chart
    2.check field is number type and include null value
   */
   def 'Band_Bar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_Bar', caseName)
      vstest.executeVS(null, ['Home','B1','B2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in Bar chart
    2.check field is number type and include null value
   */
   def 'Band_BoxPlot'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_Boxplot', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in Line chart
    2.check field is number type
   */
   def 'Band_StepLine'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_StepLine', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Band type apply in Area chart
    2.check field is number type and is negative
   */
   def 'Band_Area'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Band_Area', caseName)
      vstest.executeVS(null, ['Home','B1','B2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Statistics type apply in JumpLine chart
    2.check field is number type and value exist and null negative
    3.compulation is Confidence Interval
   */
   def 'Statistics_JumpLine'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Statistics_JumpLine', caseName)
      vstest.executeVS(null, ['Home','Interval is 80'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Statistics type apply in Watefall chart
    2.check field is number type
    3.compulation is Percentage
   */
   def 'Statistics_Watefall'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Statistics_Watefall', caseName)
      vstest.executeVS(null, ['Home','B1','B2','B3','B4'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Statistics type apply in Bar chart
    2.check field is number type
    3.compulation is Standard Deviation
   */
   def 'Statistics_Bar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Statistics_Bar', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Statistics type apply in Pareto chart
    2.check field is number type and value exist and null negative
    3.compulation is Percentiles
   */
   def 'Statistics_Parato'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Statistics_Parato', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line of Statistics type apply in Point chart
    2.check field is number type and value exist and null negative
    3.compulation is Quantiles
   */
   def 'Statistics_Point'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Statistics_Point', caseName)
      vstest.executeVS(null, ['Home','Quantiles is 2','Quantiles is 10'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line field from formula
    2.axis is drillMembers dynamic generate
   */
   def 'SFormula_Bar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/SFormula_Bar', caseName)
      vstest.executeVS(null, ['Home','drillMemberslevel1','drillMemberslevel2'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line field from formula
   */
   def 'SFormula_StepLine'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/SFormula_StepLine', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line field from Unpivot table
    2.axis is calendar dynamic generate
   */
   def 'SDynamicAxis_Bar'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/SDynamicAxis_Bar', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    1.Test target line field from special ws table(cross,rotate,group)
    2.axis is calendar dynamic generate
   */
   def 'Swstable'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Properties/TargetLine/Swstable', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }
}
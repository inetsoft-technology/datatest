package inetsoft.test.viewsheet.cases.vscrosstab

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Issue
import spock.lang.Specification

class Crosstab_Calculate_Spec extends Specification {
  def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
  }

   /**
    * check Percent Calculate and aggregate is Sum&Max
    * check percent of subTotal&grandTotal by rows&columns
    */
   def 'Vpecent1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Percent/Vpecent1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside','sort1'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /**
    * check exsit negative: percent of subTotal by columns/grandTotal by rows
    * col total and summarize are show
    */
   def 'Vpecent2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Percent/Vpecent2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','NamedGroup'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /**
    * check percent of grandTotal by rows&columns(date col)
    */
   @Issue('grand total by columns and rows at same times,see Bug #51147')
   def 'Vpecent3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Percent/Vpecent3',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Sliding Calculate
    *check aggregate is Min+PthPercentile and sliding of row&col inner dim
    */
   def 'Vsliding1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Sliding/Vsliding1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','filter1','properties'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check sliding of row inner dim& null if not enough values
    *check 'Fill Blank cell with Zero' in properties
    */
   def 'Vsliding2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Sliding/Vsliding2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *row&col binding calcfield and topN
    *check sliding of col inner dim and NUll&Include checkbox(exist bug)
    */
   def 'Vsliding3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Sliding/Vsliding3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','count','distinctCount','Median',
                                'product','mode','concat','Std Deviation'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check sliding of row&column inner at the same time
    */
   def 'Vsliding4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Sliding/Vsliding4', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check multiple aggregate types by radiobutton
    *check row running inner dim(diff date level)
    */
   def 'Running1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Running/Vrunningtotal1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check row running reset at(date level is minute&second)
    */
   def 'Running2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Running/Vrunningtotal2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','expand'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check aggregate is Average
    *check row running break by all rows&cols binding query
    */
   def 'Running3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Running/Vrunningtotal3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check aggregate is First and date level is * of year
    *check row running break by all rows&cols binding model
    */
   def 'Running4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Running/Vrunningtotal4', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check dynamic binding by checkbox and aggregate is calfield
    */
   def 'RunningTotalcalcfield'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Running/crosstab dynamic binding', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','morerows'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check change form previous of all rows
    */
   def 'Vchange1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange1',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','drilldown'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check change form previous of all columns
    *side by side
    */
   def 'Vchange2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange2',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check change form previous columns and rows at same times
    */
   @Issue('form previous columns and rows at same times,see Bug #51174')
   def 'Vchange3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange3',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check dynamic&calfield binding and aggregate is Nthlargest
    *check change from (previous,Next, first,last) of rows&cols
    */
   def 'Vchange4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange4',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check change from previous all Row&columns binding query
    *Rows/Columns binding 3 dims
    */
   def 'Vchange5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange5', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','adhocfilter1']
              as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check change from previous year,quarter,week
    */
   def 'Vchange6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vchange6', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','expand','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check binding by drillMembers and exsit negative value
    *check change from first of rows&cols(shrink to fit)
    */
   def 'Vnegative'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/Vnegative',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(['(Home)','filter1','nameGroup'] as String[],null,
              false,false)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check some calc for others group
    */
   def 'OthersCalc'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/Change/OthersCalc',
              caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check value of first for all rows by binding oracle physical table
    */
   def 'Vvalueof1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/ValueOf/Vvalueof1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check value of last for all rows&cols binding calfield&expression
    */
   def 'Vvalueof2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/ValueOf/Vvalueof2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','expand','cellsize'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check aggregate is count&discount
    *check value of previous for all rows&cols
    */
   def 'Vvalueof3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/ValueOf/Vvalueof3', caseName)
      vstest.executeVS(null, ['(Home)','sidebyside'] as String[])
      vstest.exportAsPNG(null, ['(Home)','sidebyside'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check value of previous year,quarter,week
    */
   def 'Vvalueof4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ValueOf/Vvalueof4', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check others group calc
    */
   def 'OthersCalc1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/OthersGroupCalc/OthersCalc1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check others group calc
    */
   def 'OthersCalc2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/OthersGroupCalc/OthersCalc2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','filterothers'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check others group calc
    */
   def 'OthersCalc3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/OthersGroupCalc/OthersCalc3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','filterdate'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check others group calc
    */
   def 'OthersCalc4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/OthersGroupCalc/OthersCalc4', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    *check Compound Growth calc
    */
   def 'growth1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/CompoundGrowth/growth1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    *check Compound Growth calc
    */
   def 'growth2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/CompoundGrowth/growth2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    *check Compound Growth calc
    */
   def 'growth3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/CompoundGrowth/growth3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Change&Value of Comparison Total
    */
   def 'Change&Value_Total1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/Change&Value_Total1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Change&Value of Comparison Total
    */
   def 'Change&Value_Total2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/Change&Value_Total2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Running&Compound growth Comparison Total
    */
   def 'Running&Compound_Total'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/Running&Compound_Total', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Sliding Comparison Total
    */
   def 'Sliding_Total'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/Sliding_Total', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Date Comparison total
    */
   def 'DC_Total1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/DC_Total1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Date Comparison total
    */
   def 'DC_Total2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/ComparisonTotal/DC_Total2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Sort Comparison in crosstab
    */
   def 'NormalSort'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/SortComparison/NormalSort', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['Home'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check Sort Comparison in DC crosstab
    */
   def 'DCSort'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest ('1^128^__NULL__^Calculate/SortComparison/DCSort', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['Home'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   static ViewsheetTest vstest
   static String caseName
}
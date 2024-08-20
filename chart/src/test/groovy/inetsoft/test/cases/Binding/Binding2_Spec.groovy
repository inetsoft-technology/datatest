package inetsoft.test.cases.Binding

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Binding2_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check use datetime, date dimension, from db or embedded
    */
   def 'CM10_1_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_1_1', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check use datetime, date dimension, from db or embedded
    */
   def 'CM10_1_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_1_2', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
      //vstest.compareImage(null)
   }

   /*
    check createPeriod dimension, level is Month
    */
   def 'CM10_2_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_1', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check createPeriod dimension, level is Year, Qutater
    */
   def 'CM10_2_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_3', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    check createPeriod dimension, diff dimension - Year, Period is Month, single select
    */
   def 'CM10_2_6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_6', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    check createPeriod dimension, diff dimension - Year, Period is Month, multi select
    */
   def 'CM10_2_7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_7', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check createPeriod dimension, diff dimension - Year|Quater|Week, Period is Month, single select
    */
   def 'CM10_2_8'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_8', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, diff dimension - Week, Period is Month, multi select, do drill
   */
   def 'CM10_2_12'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_12', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, diff dimension - Hour|Minute|Second, Period is Month, single select
   */
   def 'CM10_2_13'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_13', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, diff dimension - Hour|Minute|Second, Period is Month, single select
   */
   def 'CM10_2_16'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_16', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, dimension - Year, Period is week, single select
   */
   def 'CM10_2_18'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_18', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, dimension - Year, Period is week, multi select
   */
   def 'CM10_2_19'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_19', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, dimension - Year, Period is day, single select
   */
   def 'CM10_2_20'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_20', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check createPeriod dimension, dimension - Year, Period is day, multi select
   */
   def 'CM10_2_21'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_2_21', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check as time series, date level-year, fill time series gaps is false, true with null, true with zero
    */
   def 'CM10_3_9'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_9', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check as time series, date level-Quarter, Month, Week, Day, aggregate NthLargest, aggregate calcfield, Running total,
    percent of Grand Total
    */
   def 'CM10_3_10'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_10', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check as time series, date level-Hour,Minute, aggregate agg calcfield, Average
    */
   def 'CM10_3_11'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_11', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check disrect options on diff aggreagte type
    */
   def 'CM10_3_12'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_12', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
   /*
    different data level is of
    */
   def 'CM10_3_13'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_13', caseName)
      vstest.executeVS(null, ['Day of Month', 'Day of Week', 'Hour of Day','Minute of Hour',
                              'Month of Year', 'Quarter of Year', 'Second of  Minute', 'Week of Year', 'none'] as String[])

      expect:
      vstest.compareData(null)
   }

    /*
    timeseries is false
    */
   def 'CM10_3_14'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_3_14', caseName)
      vstest.executeVS(null,['None', 'Year', 'Quarter', 'Month', 'Week', 'Day', 'Hour',
                             'Minute', 'Second', 'Second_true'] as String[])
      expect:
      vstest.compareData(null)
   }

   /*
    check aesthetic fields and data fields binding
    */
   def 'CM10_4_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_4_1', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
      //vstest.compareImage(null)
   }

   /*
    check aesthetic fields and data fields binding
    */
   def 'CM10_4_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_4_2', caseName)
      vstest.executeVS(null, null)
      String[] files = ['Chart', 'Chart1'] as String[]

      expect:
      vstest.compareData(null)
      //vstest.compareImage(files)
   }

   /*
    check aesthetic fields and data fields binding
    */
   def 'CM10_4_5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_4_5', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
      //vstest.compareImage(null)
   }

   /*
    check aesthetic fields and data fields binding
    */
   def 'CM10_4_7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_4_7', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
      //vstest.compareImage(null)
   }

   /*
    check special chart type, stock
    */
   def 'CM10_5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_5', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
     check special chart type, candle
    */
   def 'CM10_6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Binding2/CM10_6', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
      //vstest.compareImage(null)
   }
}

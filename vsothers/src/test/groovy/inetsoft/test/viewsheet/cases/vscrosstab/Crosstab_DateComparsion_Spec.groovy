package inetsoft.test.viewsheet.cases.vscrosstab

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.Specification

class Crosstab_DateComparsion_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   /**
    * set system time is '2021-11-08' to check Today
    */
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check Years-Year&YeartoDate
    */
   def 'Interval-Year'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Interval-Year', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Quarters-Quarter/QuarterToDate/SameQuater
    */
   def 'Interval-Quarter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Interval-Quarter', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Months-Month/MonthToDate/SameMonth
    * check Quarters-MonthToDate/SameMonth
    * check action: NamedGroup
    */
   def 'Interval-Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Interval-Month', caseName)
      vstest.exportAsPNG(['(Home)', 'NamedGroup'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check interval is Weeks-week/WeekToDate
    * check Years/Quarters/Months-WeekTodate/SameWeek
    */
   def 'Interval-Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Interval-Week', caseName)
      vstest.exportAsPNG(null, null, false, false)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check interval is Days-Day
    * check ears/Quarters/Months/Weeks-SameDay
    */
   def 'Interval-Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Interval-Day', caseName)
      vstest.exportAsPNG(null, null, false, false)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check periods-interval is Year-Quarter
    */
   def 'Year-Quarter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Year-Quarter', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Year-Month
    */
   def 'Year-Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Year-Month', caseName)
      vstest.exportAsPNG(['(Home)','annotation'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Year-Week
    * diff level sort action
    */
   def 'Year-Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Year-Week', caseName)
      vstest.exportAsPNG(['(Home)','sort'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Year-Day
    */
   def 'Year-Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Year-Day', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Quarter-Month by dynamic&calcfield
    */
   def 'Quarter-Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Quarter-Month', caseName)
      vstest.exportAsPNG(['(Home)','binding1','binding2'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Quater_Week
    */
   def 'Quater_Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Quater_Week', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
          vstest.compareImage(null)
        }
   }

   /**
    * check periods-interval is Quater_Day&sort action
    */
   def 'Quater_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Quater_Day', caseName)
      vstest.exportAsPNG(['(Home)','adhoc_filter'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Month_Week
    */
   def 'Month_Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Month_Week', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Month_Day
    */
   def 'Month_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Month_Day', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check periods-interval is Week-Day
    */
   def 'Week-Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Week-Day', caseName)
      vstest.exportAsPNG(['(Home)', 'adhoc_filter'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is All
    */
   def 'All'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/All', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is Year
    */
   def 'Year'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/Year', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is Quater
    */
   def 'Quarter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/Quarter', caseName)
      vstest.exportAsPNG(['(Home)', 'aggregate_sort'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is Month
    */
   def 'Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/Month', caseName)
      vstest.exportAsPNG(['(Home)', 'adhoc_filter'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is Week
    */
   def 'Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/Week', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check Custom periods interval is Day
    */
   def 'Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Custom/Day', caseName)
      vstest.exportAsPNG(['(Home)', 'sort1'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check previous num&periods&compare data of binding variable/expression
    */
   def 'Variable1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/Variable1', caseName)
      vstest.exportAsPNG(['(Home)','Variable1','Variable2'] as String[], null, false, false)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

  /**
    * check custom dc options binding variable/expression
    */
   def 'Variable2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/Variable2', caseName)
      vstest.exportAsPNG(['(Home)','Quarter','sort'] as String[], null, false, false)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check binding element data, DC apply
    */
   def 'Element_binding'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/Element_binding', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }

   /**
    * check element by script, DC apply and share
    */
   def 'Script'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/Script', caseName)
      vstest.exportAsPNG(null, null, false, false)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check as time series apply in dc
    */
   def 'TimeSeries'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/TimeSeries', caseName)
      vstest.exportAsPNG(null, null, false, false)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check DC&shared DC apply in embedded vs
    */
   def 'EmbeddedVS'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/EmbeddedVS', caseName)
      vstest.exportAsPNG(null, null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check diff path annotation, if need check data, please check system date to 2021-11-08
    */
   def 'Annotation'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Calculate/DateComparison/Special/Annotation', caseName)
      vstest.exportAsPNG(['(Home)','annotation'] as String[], null, false, false)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareImage(null)
      }
   }
}
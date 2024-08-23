package inetsoft.test.cases.TrendComparison

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification
import spock.lang.Ignore


class DateComparison_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }


   /*check period is year and interval is quarter， chart covered highlight & format & bookmark1 covered target band & project forward*/
   def 'Year_Quarter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_Quarter', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is month & case covered highlight & format & brush*/
   def 'Year_Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_Month', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is week & check highlight & format*/
   def 'Year_Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_Week', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is day & bk1 check exculue runtime data<some data or month>*/
   def 'Year_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_Day', caseName)
      vstest.exportAsPNG(['Home','bk1'] as String[], null, false, true)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is month & check brush and hide axis in bookmark*/
   def 'Quarter_Month'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_Month', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is week & check brush in bookmark*/
   def 'Quarter_Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_Week', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is day
   bk1check hide axis and zoom*/
   def 'Quarter_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_Day', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is week.*/
   def 'Month_Week'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_Week', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is day*/
   def 'Month_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_Day', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is week and interval is day  auto summary data has entra data,Needs update auto*/
   def 'Week_Day'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Week_Day', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period and interval are same level. chart's old logic can coverd a part.we only need check peroperteies.
   chart include highlight & format & axis properties test， bookmark include project forward & target line & fillseries as zero test*/
   def 'Same Level'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Same Level', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is year to date, bookmark check brush*/
   def 'Year_YearToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_YearToDate', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1', 'bk2'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is quarter to date, case include highlight */
   def 'Year_QuarterToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_QuarterToDate', caseName)
      vstest.executeVS(null, null)


      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is month to date, bookmark check brush*/
   def 'Year_MonthToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_MonthToDate', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is week to date, case include highlight*/
   def 'Year_WeekToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_WeekToDate', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is quarter to date.bookmark check adhoc filter on dc calcfield<date part>*/
   def 'Quarter_QuarterToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_QuarterToDate', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is month to date,case include highlight */
   def 'Quarter_MonthToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_MonthToDate', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is week to date, bookmark check hide axis*/
   def 'Quarter_WeekToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_WeekToDate', caseName)
      vstest.executeVS(null, null)
      //vstest.exportAsPNG(null, null)


      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is month to date*/
   def 'Month_MonthToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_MonthToDate', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is week to date, bookmark check brush*/
   def 'Month_WeekToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_WeekToDate', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is week and interval is week to date, bookmrk check hide axis*/
   def 'Week_WeekToDate'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Week_WeekToDate', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }


   /*check period is year and interval is same quarter, bookmark check brush.*/
   def 'Year_SameQuarter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_SameQuarter', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is same month*/
   def 'Year_SameMonth'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_SameMonth', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is same day. bookmark check hide axis */
   def 'Year_SameDay'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_SameDay', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is year and interval is same week. case include format and highlight */
   def 'Year_SameWeek'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Year_SameWeek', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is same month, bookmark check brush*/
   def 'Quarter_SameMonth'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_SameMonth', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is same week*/
   def 'Quarter_SameWeek'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_SameWeek', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is quarter and interval is same day*/
   def 'Quarter_SameDay'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Quarter_SameDay', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is same week*/
   def 'Month_SameWeek'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_SameWeek', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is month and interval is same day*/
   def 'Month_SameDay'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Month_SameDay', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check period is week and interval is same day,show summary data in auto has one row entra data */
   def 'Week_SameDay'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Week_SameDay', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， granularity is year/quarter, bk1 check target line for runtime col*/
   def 'Custom_normal1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_normal1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is month/week */
   def 'Custom_normal2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_normal2', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is day */
   def 'Custom_normal3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_normal3', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is All */
   def 'Custom_GranularityAll'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_GranularityAll', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }
    
    /*fron xtodate1-xtodate7 These cases are meaningful and currently being kept to prevent the xto date option from being released in future version*/
   /*check custom period interval is year to date for each year/quarter */
   @Ignore
   def 'Custom_xtodate1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate1', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period interval is year to date granutity is month/week/day */
   @Ignore
   def 'Custom_xtodate2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate2', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is quarter to date for each year/quarter */
   @Ignore
   def 'Custom_xtodate3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is month to date for each year/quarter */
   @Ignore
   def 'Custom_xtodate4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate4', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is month to date for each month */
   @Ignore
   def 'Custom_xtodate5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate5', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is week to date for each year/quarter */
   @Ignore
   def 'Custom_xtodate6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate6', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period， interval is week to date for each month/week */
   @Ignore
   def 'Custom_xtodate7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Custom_xtodate7', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }


   /*check standard period shared dc & bk1 check change original dc then check shared from  */
   def 'Shared dc1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Shared dc1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check custom period shared dc & bk1 check change original dc then check shared from and clear dc */
   def 'Shared dc2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/Shared dc2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*check standard period use variable & expression */
   def 'dc_variable'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/dc_variable', caseName)
      vstest.exportAsPNG(null, ['Home','bk1','bk2'] as String[])

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   /*check custom period use variable & expression */
   def 'dc_variable2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/dc_variable2', caseName)
      vstest.exportAsPNG(null, ['Home','bk1','bk2'] as String[])

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   /*check date col use variable then apply dc's expression */
   def 'dc_variable3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/dc_variable3', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }


   /*check script chart apply dc, check original date level same as dc runtime col*/
   def 'dc_special'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Date Comparison/dc_special', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

}

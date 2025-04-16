package inetsoft.test.worksheet.cases

import inetsoft.test.core.TUtil
import inetsoft.test.modules.WorksheetTest
import spock.lang.IgnoreIf
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Title

//@IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("linux") })
@Title("Use to test in range condition on a fixed time(2025-02-15 00;00:00), it only be execute on Windows")
@IgnoreIf({ os.linux })
@Stepwise
class InRange_Spec extends Specification {
   static WorksheetTest wstest
   static TUtil tUtil = new TUtil()
   static String caseName

   def setupSpec() {
      tUtil.changeSystemDateToDay ('2025-02-15')
      WorksheetTest.initHome(this.class.getName())
   }

   def cleanupSpec() {
      tUtil.restoreSystemTime()
   }

   def 'test in range on YearTo, QuarterTo, MonthTo' () {
      caseName =  specificationContext.currentIteration.name
      def paras1= new HashMap<String, Object>()
      paras1.put('rangeType_datetime', 'Year to date')
      paras1.put('rangeType_date', 'Year to date last year')
      paras1.put('rangeType_datetime1', 'Yesterday')

      def paras2 = [
              rangeType_datetime: 'Quarter to date',
              rangeType_date: 'Quarter to date last quarter',
              rangeType_datetime1: 'Quarter to date last year'
      ]
      def paras3 = [
              rangeType_datetime: 'Month to date',
              rangeType_date: 'Month to date last month',
              rangeType_datetime1: 'Month to date last year'
      ]

      wstest = new WorksheetTest(caseName)

      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras1, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras2, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras3, null)

      expect:
      wstest.compareData()
   }

   def 'test in range on Today, Tomorrow, This' () {
      caseName =  specificationContext.currentIteration.name
      def paras1 = [
              rangeType_date: 'Tomorrow',
              rangeType_datetime: 'Today',
              rangeType_datetime1: 'Week before last week'
      ]
      def paras2 = [
              rangeType_date: 'This January',
              rangeType_datetime: 'This month',
              rangeType_datetime1: 'This month last year'
      ]
      def paras3 = [
              rangeType_date: 'This quarter',
              rangeType_datetime: 'This quarter last year',
              rangeType_datetime1: 'This week'
      ]

      wstest = new WorksheetTest(caseName)

      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras1, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras2, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras3, null)

      expect:
      wstest.compareData()
   }

   def 'test in range on Last N month, quarter, days' () {
      caseName =  specificationContext.currentIteration.name
      def paras1 = [
              rangeType_date: 'Last 3 months',
              rangeType_datetime: 'Last 8-14 days',
              rangeType_datetime1: 'Last 4 weeks'
      ]
      def paras2 = [
              rangeType_date: 'Last April',
              rangeType_datetime: 'Last month',
              rangeType_datetime1: 'Last month last year'
      ]
      def paras3 = [
              rangeType_date: 'Last quarter',
              rangeType_datetime: 'Last quarter last year',
              rangeType_datetime1: 'Last week'
      ]

      wstest = new WorksheetTest(caseName)

      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras1, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras2, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras3, null)

      expect:
      wstest.compareData()
   }

   def 'test in range on This, Last year, N quarter of this,last year' () {
      caseName =  specificationContext.currentIteration.name
      def paras1 = [
              rangeType_date: 'This Year',
              rangeType_datetime: 'Last Year',
              rangeType_datetime1: '1st quarter this year'
      ]
      def paras2 = [
              rangeType_date: '4th quarter last year',
              rangeType_datetime: '1st half of this year',
              rangeType_datetime1: '2nd half of last year'
      ]

      wstest = new WorksheetTest(caseName)

      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras1, null)
      wstest.executeWS('1^2^__NULL__^Common/condition_inrange', paras2, null)

      expect:
      wstest.compareData()
   }
}

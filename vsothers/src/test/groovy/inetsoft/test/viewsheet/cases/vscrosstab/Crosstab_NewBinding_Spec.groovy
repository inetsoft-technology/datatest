package inetsoft.test.viewsheet.cases.vscrosstab

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class Crosstab_NewBinding_Spec extends Specification {
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check crosstab simple structure&binding options
    */
   def 'Simple'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Simple', caseName)
      vstest.executeVS(null, ['(Home)', 'filter'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'filter'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with ws groupAggTableColumn&actions
    */
   def 'Type1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type1', caseName)
      vstest.executeVS(null, ['(Home)', 'Expand', 'adhocfilter'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'Expand', 'adhocfilter'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with ws diff type range col&NamedGroup
    */
   def 'Type2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type2', caseName)
      vstest.executeVS(null, ['(Home)', 'NamedGroup'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'NamedGroup'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with ws groupAggTableColumn in Complex structure&actions
    */
   def 'Type3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type3', caseName)
      vstest.executeVS(null, ['(Home)', 'Expand', 'adhocfilter', 'drillFilter'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'Expand', 'adhocfilter', 'drillFilter'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding query with oracle
    */
   def 'Type4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type4', caseName)
      vstest.executeVS(null, ['(Home)', 'adhocfilter', 'drillFilter'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter', 'drillFilter'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with datamodel calcfeild col&actions
    */
   def 'Type5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type5', caseName)
      vstest.executeVS(null, ['(Home)', 'adhocfilter', 'Sort'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter', 'Sort'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with vs runtime col:radiobutton&checkbox
    */
   def 'Type6_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type6_1', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with vs runtime col:expression&drillMembers
    */
   def 'Type6_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type6_2', caseName)
      vstest.executeVS(null, ['(Home)', 'adhocfilter', 'drillup'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'drillup'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check binding with vs runtime col: dynamic binding calcfield
    *check crosstab'binding variable data do expand hierarchy,sort,namedgroup *for bug49751 and Bug#50435,Bug#49798
    */
   def 'Type6_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type6_3', caseName)
      vstest.executeVS(null, ['(Home)', 'Alias', 'Expand', 'Sort'] as String[])

      expect:
         vstest.compareData(null)
         vstest.compareImage(null)
   }

   /*
    check element binding from table
    */
   def 'Type7_1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type7_1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check element binding from crosstab
    */
   def 'Type7_2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type7_2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check element binding from em, form table
    */
   def 'Type7_3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type7_3', caseName)
      vstest.executeVS(null, ['(Home)', 'Expand'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'Expand'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with physicaltable calcfeild col&actions
    */
   def 'Type8'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type8', caseName)
      vstest.executeVS(null, ['(Home)', 'adhocfilter', 'drillFilter'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter', 'drillFilter'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check binding with ws table alias&actions
    */
   def 'Type9'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Type9', caseName)
      vstest.executeVS(null, ['(Home)', 'Expand', 'sort'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'Expand', 'sort'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check CreatePeriod Dimension, crosstab have no relation with calendar
    *one calendar, period level is Month, Week
    */
   def 'Pdate1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Pdate1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check CreatePeriod Dimension, one calendar,period level is Day, range
    */
   def 'Pdate2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Pdate2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check CreatePeriod Dimension, multi calendar, period, Week
    */
   def 'Pdate3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Pdate3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check Pdate&Hierarchy relation
    */
   def 'Pdate4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Pdate4', caseName)
      vstest.executeVS(null, ['(Home)', 'expand'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'expand'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check date as measure, aggregate is Max, Last, None by Bug #50284
    */
   def 'Special1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Special1', caseName)
      vstest.executeVS(null, ['(Home)', 'Max', 'None'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'Max', 'None'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check special case: date/time/datetime is none and aggregate is none by *Bug #50678
    */
   def 'Special2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Special2', caseName)
      vstest.executeVS(null, ['(Home)', 'filter1', 'filter2'] as String[])
      vstest.exportAsPNG(null, ['(Home)', 'filter1', 'filter2'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check custom hierarchy, Region-State-City, Quarter-Month-Week-Day, QuaterOfYear-WeekOfYear-DayOfWeek
    */
   def 'Hycase1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Hycase1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    *check custom hierarchy, Hour-Second, DayOfHour-SecondOfMinute, *DayOfHour-Minute-Second
    *check date dill, *Month-DayOfMonth-HoutOfDay,HordOfDay-MinuteOfHour-SecondOfMinute
    */
   def 'Hycase2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/Hycase2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check asTime series, Year, Quater, Month, Week, Day，Hour, Minute
    */
   def 'TimeSeries1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/TimeSeries1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'crosstab_banding2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/crosstab_banding2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      String[] files = ['Crosstab1', 'Crosstab2'] as String[]

      expect:
      verifyAll{
         vstest.compareData(files)
         vstest.compareImage(null)
      }
   }

   def 'crosstab_banding3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/crosstab_banding3', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'crosstab_banding4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/crosstab_banding4', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'crosstab_drillmember'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/crosstab_drillmember', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(['(Home)'] as String[],null,false,false)

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check SortOthers
    */
   def 'SortOthers'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/SortOthers', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
   check only binding aggregate, Bug #57603
    */
   def 'AggregateNone'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/AggregateNone', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
    /*
   check aggregate percent format not apply on col when col binding number format, Bug #59405
    */
   def 'ColPercent'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Binding/ColPercent', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   static ViewsheetTest vstest
   static String caseName
}
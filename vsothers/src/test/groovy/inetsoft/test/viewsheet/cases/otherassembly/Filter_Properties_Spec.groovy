package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Issue
import spock.lang.Specification

/**
 * covered all properties and binding of Filter assembly
 */
class Filter_Properties_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check selection sort options with others actions
    */
   def 'SelectionProperties1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionProperties1', caseName)
      vstest.exportAsPNG(null, ['desc', 'asc'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check selection suppress blank options, Select First Item options(home), and filter data
    * Bug #59048 reject, if has node display issue of selection tree in exported files, check this bug.
    * first select item option can't check, because this property apply on current view, not 'Home' bookmark. see Bug #65210
    */
   def 'SelectionProperties2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionProperties2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1', 'bk2'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check Select First Item options(exist single selection node), and filter data.  Bug #60101
    */
   def 'SelectionProperties3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionProperties3', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /*
   check diff binding on Parent/ID selection tree
    */
   def 'SelectionPC1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionPC1', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check relation between TreePC and SelectionList, auto selected child is false
    */
   def 'SelectionPC3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionPC3', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check sort for parent-child Selection Tree, for feature #37415
    */
   def 'SelectionPC_LabelSort' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionPC_LabelSort', caseName)
      vstest.exportAsPNG(null, null, false, true)

      expect:
      vstest.compareImage()
   }

   /**
    * check Selection List and Tree binding Addition table with String col, one additional table and
    * multi additional table
    */
   def 'SelectionATable1' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionATable1', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check Selection List and Tree binding Addition table with date, datetime, time, boolean, float col
    */
   def 'SelectionATable2' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionATable2', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check Selection List binding Addition table with customer data -- Bug #62631
    */
   def 'SelectionATable3' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/SelectionATable3', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider single mode on diff type, year, month, day, hour, minute
    */
   def 'RangePro1' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangePro1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'labelisfalse'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider composite mode on one cols and multi cols
    */
   def 'RangePro2' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangePro2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider single value on diff value range
    */
   def 'RangePro3' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangePro3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider single value on diff value range
    */
   def 'RangePro4' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangePro4', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider assemlies binding on chart and crosstab measure
    */
   def 'RangePro5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangePro5', caseName)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check RangeSlider additional binding on single and composite mode
    */
   def 'RangeATable1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/RangeSlider/RangeATable1', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2', 'bk3'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check calendar binding date, min/max value, and diff view selected value
    */
   @Issue("for bk1 and bk2. the calendar selected value not right when export as png")
   def 'CalendarPro1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/CalendarPro1', caseName)
      vstest.exportAsPNG(null, ['bk1', 'bk2', 'bk3', 'bk4'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check calendar Min/Max property apply
    */
   def 'CalendarPro2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/CalendarPro2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'max', 'year'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check calendar filter data after set date format
    */
   @Issue('Calendar format did not apply when export as png')
   def 'DateFormat_Filter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_Filter', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check date format apply in calendar month view
    * check weekFormat script
    */
   def 'DateFormat_MonthView1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_MonthView1', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check date format apply in calendar month view
    */
   def 'DateFormat_MonthView2'() {
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_MonthView2', 'DateFormat_MonthView2')
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check date format apply in calendar month view
    */
   def 'DateFormat_MonthView3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_MonthView3', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check date format apply in calendar year view
    */
   def 'DateFormat_YearView1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_YearView1', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check date format apply in calendar year view
    */
   def 'DateFormat_YearView2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/DateFormat_YearView2', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()

   }


   /**
    * check calendar binding additional table
    */
   def 'CalendarATable1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Calendar/CalendarATable1', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check selection tree(PC)'s format with different level path
    */
   def 'PCTreeFormat1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VSFilters/Selection/PCTreeFormat1', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }
}

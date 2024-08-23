package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class Table_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check table binding component binding, then drill down filter to impact table data.
    * highlight on null value, apply row.
    */
   def 'NT1' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1-drillup', 'bk2-drilldown', 'bk3-drillfilter'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check calc field with date, datetime, time col and apply on highlight
    */
   def 'NT2' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check calc field with date, datetime, time col and apply on highlight
    */
   def 'NT3' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'filterNull'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check lm with calc, then use expression change highlight and conditions
    */
   def 'NT4' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT4', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'CA'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check element binding, script to set hide col, tab
    */
   def 'NT5' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT5', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'hideCol', 'switchTab'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check table show in embdded vs, and table binding embedded vs table
    */
   def 'NT6' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT6', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    *check ws with crosstab, and as time searies is true
    */
   def 'NT7' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/NT7', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check ws max row apply on em table, form table, em and form table.
    * check change cell value by form component
    */
   def 'EFT2' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Table/EFT2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }
}

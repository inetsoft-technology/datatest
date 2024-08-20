package inetsoft.test.viewsheet.cases.vsfreehand

import inetsoft.test.viewsheet.VSCalcTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class FreeExecution_Spec extends Specification {
   static VSCalcTest vsCalcTest
   static String caseName

   def setupSpec() {
      VSCalcTest.initHome(this.class.getName())
   }

   /**
    * test freehand name group with query
    */
   def 'FNGTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/NameGroups/FNGTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand name group with ws
    */
   def 'FNGTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/NameGroups/FNGTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand name group with variable
    */
   def 'FNGTable3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/NameGroups/FNGTable3', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand name group with pre-definedNG time
    */
   def 'FNGTable4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/NameGroups/FNGTable4', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand SortOthers
    */
   def 'SortOthersTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/SortOthersTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,datetime,time with period
    */
   def 'DateLevelTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/DateLevelTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,year|quater|month|week|day as time series is true
    */
   def 'DateLevelTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/DateLevelTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,hour|minute|second as time series is true
    */
   def 'DateLevelTable3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/DateLevelTable3', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,with table as binding sources
    */
   def 'ComBindingTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/ComBindingTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,with crosstab as binding sources
    */
   def 'ComBindingTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/ComBindingTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,with crosstab pdate as binding sources
    */
   def 'ComBindingTable3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/ComBindingTable3', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,with crosstab DC col as binding sources
    */
   def 'ComBindingTable4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/ComBindingTable4', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand date,with crosstab with dynimac as binding sources
    */
   def 'ComBindingTable5'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/ComBindingTable5', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)', 'quarter', 'noRangeID'] as String[])

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
   * test freehand normal binding with lm
   */
   def 'FreeExecute1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/FreeExecute1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand normal binding with unpivote
    */
   def 'FreeExecute2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/FreeExecute2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand normal binding with query
    */
   def 'FreeExecute3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/FreeExecute3', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand normal binding with ws, formula col, calc field
    */
   def 'FreeExecute4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/FreeExecute4', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

    /**
    * test date type manual sort
    * covered bug# 56486,# 56516
    */
   def 'DateManualSort'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/BindingOptions/DateManualSort', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * test freehand layout, detail, agg and text
    */
   def 'FreeLayout1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/Layouts/FreeLayout1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand layout, detail, agg and text, group
    */
   def 'FreeLayout2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/Layouts/FreeLayout2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand layout, detail, agg and text, group
    */
   def 'FreeLayout3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/Layouts/FreeLayout3', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand layout, agg and text, group
    */
   def 'FreeLayout4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/Layouts/FreeLayout4', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand formula functions
    */
   def 'FScript1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FScript1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand formula functions
    */
   def 'FScript2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FScript2', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

    /**
    * test col,row formula on freehand
    */
   def 'FScript3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FScript3', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test agg is none, then change to formula script rule
    */
   def 'FSNone1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FSNone1', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test agg is none, then change to formula script rule
    */
   def 'FSTopNTotal'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FSTopNTotal', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test freehand structure：RunQuery时,在一个列上竖向扩展两个column,必须在UI上指定RowGroup
    * cover normal formual and runquery formula
    */
   def 'FSRunQuery'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/FSRunQuery', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test a formula is set to read from a specific datablock. e.g. sum(datiQuery['subentry'])
    */
   def 'CellDatablockTest'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreeExecution/FormulaScript/CellDatablockTest', caseName)
      vsCalcTest.exportAsPNG(null, null)

      expect:
      vsCalcTest.compareImage(null)
   }
}

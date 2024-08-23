package inetsoft.test.viewsheet.cases.vsfreehand

import inetsoft.test.modules.VSCalcTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class Convert_Spec extends Specification {
   static VSCalcTest vsCalcTest
   static String caseName

   def setupSpec() {
      VSCalcTest.initHome(this.class.getName())
   }

   /**
    * test table convert to freehand
    */
   def 'CTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Tables/CTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test table convert to freehand
    */
   def 'CTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Tables/CTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test embedded table convert to freehand
    */
   def 'ETable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Tables/ETable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test table binding component table, then convert to freehand
    */
   def 'ComTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Tables/ComTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test table binding component table runtime col, then convert to freehand
    */
   def 'ComTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Tables/ComTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab do drill down or drill down filter, then convert to freehand
    */
   def 'DrillCroTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/DrillCroTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab with DC and trend Comparition, then convert to freehand
    */
   def 'DCPTrendTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/DCPTrendTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab with pdate and manual order on string and time, then convert to freehand
    */
   def 'PdateManulCroTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/PdateManulCroTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }


   /**
    * test crosstab with pict case, then convert to freehand
    */
   def 'CroTable1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable1', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab binding query, then convert to freehand, setRowHeight=0 to hide header
    */
   def 'CroTable2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable2', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab binding lm, then convert to freehand
    */
   def 'CroTable3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable3', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab binding group and agg table, then convert to freehand
    */
   def 'CroTable4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable4', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab binding corrstab and unpivot tabe, then convert to freehand
    */
   def 'CroTable5'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable5', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab as time series is 0
    */
   def 'CroTable6'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable6', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test component binding freehand
    * Bug #57180
    */
   def 'CroTable7'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable7', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

    /**
    * test dynamic value on crosstab
    */
   def 'CroTable8'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/CroTable8', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test percent format should not auto appy on col header when the header data type is number
    */
   def 'PercentFormat'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/Crosstabs/PercentFormat', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test crosstab in group and tab, then convert to freehand
    * bug#59405
    */
   def 'GroupAndTab'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/GroupAndTab', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }

   /**
    * test script effect
    * Bug #58711
    */
   def 'VSScriptEffect'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^ConvertToCalc/VSScriptEffect', caseName)
      vsCalcTest.exportAsPNG(null, null)
      vsCalcTest.checkConvert(null)

      expect:
      vsCalcTest.compareImage(null)
   }
}

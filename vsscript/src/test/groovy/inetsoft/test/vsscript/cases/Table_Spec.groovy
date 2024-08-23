package inetsoft.test.vsscript.cases

import inetsoft.test.modules.VSScriptTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class Table_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test table script which inherit from parent
    */
   def 'TestCase-Table_Common1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'TableView1',
                        COMMAND: 'cellFormat[1][3] = [StyleConstant.DECIMAL_FORMAT, "#,###.00"];\n' +
                                 'title = "the highlight existed? " + TableView1.highlighted.equal5;\n' +
                                 'colFormat[1] = [StyleConstant.DATE_FORMAT, "yyyy/MM/dd"];\n' +
                                 'shrink = true']]
      def TestData2 = [[HANDLER: 'TableView2',
                        COMMAND: 'titleVisible = false;\n' +
                                 'wrapping = true;\n' +
                                 'tableStyle = "ReverseHeader"'],
                       [HANDLER: 'Text1',
                        COMMAND: 'Text1.value = TableView1.table[1][2] + ", " + ' +
                                 'TableView1.data[1][2] + ", " + ' +
                                 'TableView1.data.length + "," + ' +
                                 'TableView1.data.size']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Table1', caseName)
      vsScriptTest.printVS('proRef', TestData1, ['TableView1'] as String[])
      vsScriptTest.printVS('dataRef', TestData2, ['TableView2', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Table_Common1_dataRef',
                                 'TestCase-Table_Common1_proRef'] as String[])
   }

   /**
    * test crosstab script which inherit from parent
    */
   def 'TestCase-Crosstab_Common1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Crosstab1',
                        COMMAND: 'cellFormat[2][2] = [StyleConstant.DECIMAL_FORMAT, "#,###.00"];\n' +
                                'title = "the highlight existed? " + Crosstab1.highlighted.stateNJNK;\n' +
                                'colFormat[0]=[StyleConstant.DATE_FORMAT, "yyyy/MM/dd"];\n' +
                                'shrink = true']]
      def TestData2 = [[HANDLER: 'Crosstab2',
                        COMMAND: 'titleVisible = false;\n' +
                                'wrapping = true;\n' +
                                'tableStyle = "ReverseHeader"'],
                       [HANDLER: 'Text1',
                        COMMAND: 'Text1.value = Crosstab1.table[1][2] + ", " + ' +
                                'Crosstab1.data[1][2] + ", " + ' +
                                'Crosstab1.data.length + "," + ' +
                                'Crosstab1.data.size']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1', caseName)
      vsScriptTest.printVS('proRef', TestData1, ['Crosstab1'] as String[])
      vsScriptTest.printVS('dataRef', TestData2, ['Crosstab2', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Crosstab_Common1_dataRef',
                                 'TestCase-Crosstab_Common1_proRef'] as String[])
   }

   /**
    * test freehand script which inherit from parent
    */
   def 'TestCase-Freehand_Common1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'FreehandTable1',
                        COMMAND: 'cellFormat[1][2] = [StyleConstant.DECIMAL_FORMAT, "#,###.00"];\n' +
                                'title = "the highlight existed? " + FreehandTable1.highlighted.greaterThan200;\n' +
                                'colFormat[0]=[StyleConstant.DATE_FORMAT, "yyyy/MM/dd"];\n' +
                                'shrink = true']]
      def TestData2 = [[HANDLER: 'FreehandTable2',
                        COMMAND: 'titleVisible = false;\n' +
                                'wrapping = true;\n' +
                                'tableStyle = "ReverseHeader"'],
                       [HANDLER: 'Text1',
                        COMMAND: 'Text1.value = FreehandTable1.table[1][2] + ", " + ' +
                                'FreehandTable1.data[1][2] + ", " + ' +
                                'FreehandTable1.data.length + "," + ' +
                                'FreehandTable1.data.size']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Freehand1', caseName)
      vsScriptTest.printVS('proRef', TestData1, ['FreehandTable1'] as String[])
      vsScriptTest.printVS('dataRef', TestData2, ['FreehandTable2', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Freehand_Common1_dataRef',
                                 'TestCase-Freehand_Common1_proRef'] as String[])
   }

   /**
    * test table script with some function which inherit from parent
    */
   def 'TestCase-Table_comFunction' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Form1', COMMAND: 'setPresenter(2,3,"ShadowPresenter")\n' +
              'setColumnWidth(0,50)\n' +
              'setRowHeight(0,10)\n' +
              'setObject(2,2,"FL_test")']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Table1', caseName)
      vsScriptTest.printVS('comFunc', TestData1, ['Form1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Table_comFunction_comFunc'] as String[])
   }

   /**
    * test crosstab script with some function which inherit from parent
    */
   def 'TestCase-Crosstab_comFunction' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Crosstab1', COMMAND: 'setPresenter(3,3,\'HTMLPresenter\')\n' +
              'setColumnWidth(0,50)\n' +
              'setRowHeight(0,10)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1', caseName)
      vsScriptTest.printVS('comFunc', TestData1, ['Crosstab1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Crosstab_comFunction_comFunc'] as String[])
   }

   /**
    * Test table binding script
    */
   def 'TestCase-Table_binding' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullTable', COMMAND: 'query = "Group"\n' +
              'fields = ["reseller", "state"]']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Table1', caseName)
      vsScriptTest.printVS('binding1', TestData1, ['NullTable'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Table_binding_binding1'] as String[])
   }

   /**
    * Test the crosstab self script and binding script
    */
   def 'TestCase-Crosstab-binding1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'Crosstab1', COMMAND: 'fillBlankWithZero = true\n' +
              'summarySideBySide = false\n' +
              'mergeSpan = false']]
      def TestData2 = [[HANDLER: 'NullCorsstab', COMMAND: 'query = \'Query2\';\n' +
              'bindingInfo.rowFields = ["reseller", "state"];\n' +
              'bindingInfo.colFields = ["orderdate"];\n' +
              'bindingInfo.measureFields = ["customer_id", "price"];\n' +
              'bindingInfo.setFormula("customer_id",StyleConstant.SUM_FORMULA);\n' +
              'bindingInfo.setFormula("price",StyleConstant.AVERAGE_FORMULA);\n' +
              'bindingInfo.setTopN("state", StyleConstant.ROW_HEADER, 3);\n' +
              'bindingInfo.setTopNSummaryCol("state",StyleConstant.ROW_HEADER,\'Sum(customer_id)\');\n' +
              'bindingInfo.setTopNReverse("state",StyleConstant.ROW_HEADER,true);\n' +
              'bindingInfo.setGroupOthers("state",StyleConstant.ROW_HEADER,true);\n' +
              'bindingInfo.setGroupOrder("orderdate",StyleConstant.COL_HEADER, StyleConstant.QUARTER_OF_YEAR_DATE_GROUP);\n' +
              'bindingInfo.setGroupTotal("reseller", StyleConstant.ROW_HEADER,true);']]
      def testData3 =  [[HANDLER: 'NullCorsstab', COMMAND:'query = "Query2"\n' +
              'bindingInfo.rowFields = ["orderdate"]\n' +
              'bindingInfo.measureFields = ["customer_id"]\n' +
              'bindingInfo.setGroupOrder("orderdate",StyleConstant.ROW_HEADER, StyleConstant.QUARTER_DATE_GROUP);\n' +
              'bindingInfo.setTimeSeries("orderdate", true);\n' +
              'bindingInfo.setFormula("customer_id",StyleConstant.SUM_FORMULA);\n' +
              'bindingInfo.setPercentageType(\'customer_id\',StyleConstant.PERCENTAGE_OF_COL_GRANDTOTAL);\n' +
              'bindingInfo.setColumnOrder(\'orderdate\',StyleConstant.ROW_HEADER,StyleConstant.SORT_VALUE_DESC,\'Sum(customer_id)\');\n' +
              'bindingInfo.percentageMode = StyleConstant.PERCENTAGE_BY_COL;\n' +
              'bindingInfo.showColumnTotal = true\n' +
              'bindingInfo.showRowTotal = true']]
      def TestData4 = [[HANDLER: 'Crosstab3', COMMAND: 'sortOthersLast = true']]
      def TestData5 = [[HANDLER: 'Crosstab1', COMMAND: 'computeTrendAndComparisonForTotals = true']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1', caseName)
      vsScriptTest.printVS('self', TestData1, ['Crosstab1'] as String[])
      vsScriptTest.printVS('self1', TestData4, ['Crosstab3'] as String[])
      vsScriptTest.printVS('binding1', TestData2, ['NullCorsstab'] as String[])
      vsScriptTest.printVS('binding2', testData3, ['NullCorsstab'] as String[])
      vsScriptTest.printVS('calcTotal', TestData5, ['Crosstab1'] as String[])

      then:
      vsScriptTest.compareImage(['TestCase-Crosstab-binding1_self',
                                 'TestCase-Crosstab-binding1_self1',
                                 'TestCase-Crosstab-binding1_binding1',
                                 'TestCase-Crosstab-binding1_binding2',
                                 'TestCase-Crosstab-binding1_calcTotal'] as String[])
   }

   /**
    * test freehand script with some function which inherit from parent
    */
   def 'TestCase-Freehand_comFunction' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'FreehandTable1', COMMAND: 'setPresenter(1,2,\'ShadowPresenter\')\n' +
              'setColumnWidth(0,50)\n' +
              'setRowHeight(0,10)']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Freehand1', caseName)
      vsScriptTest.printVS('comFunc', TestData1, ['FreehandTable1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Freehand_comFunction_comFunc'] as String[])
   }

   /**
    * test freehand table self script
    */
   def 'TestCase-Freehand_binding1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'FreehandTable3', COMMAND: 'fillBlankWithZero=true']]
      def TestData2 = [[HANDLER: 'NullFreehandTable', COMMAND: 'var q = runQuery("ws:global:scriptTest:Query2");\n' +
              'layoutInfo.setCellBinding(0,0,1,"aaaa")\n' +
              'layoutInfo.setCellBinding(1,0,3,"toList(q[\'orderdate\'],\'sort=asc,rounddate=year,interval=1.0\')")\n' +
              'layoutInfo.setExpansion(1,0,2)\n' +
              'layoutInfo.setCellName(1,0,"orderdate")\n' +
              'layoutInfo.setCellBinding(2,0,2,"city")\n' +
              'layoutInfo.setExpansion(2,0,2)\n' +
              'layoutInfo.setCellBinding(0,1,3,"toList(q[\'state\'],\'sort=asc\')")\n' +
              'layoutInfo.setExpansion(0,1,1)\n' +
              'layoutInfo.setCellName(0,1,"state")\n' +
              'layoutInfo.setCellBinding(1,1,3,"sum(q[\'price\'])")']]
      def TestData3 = [[HANDLER: 'FreehandTable4', COMMAND: 'sortOthersLast=false']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Freehand1', caseName)
      vsScriptTest.printVS('comFunc', TestData1, ['FreehandTable3'] as String[])
      vsScriptTest.printVS('binding1', TestData2, ['NullFreehandTable'] as String[])
      vsScriptTest.printVS('binding2', TestData3, ['FreehandTable4'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Freehand_binding1_comFunc',
                                 'TestCase-Freehand_binding1_binding1',
                                 'TestCase-Freehand_binding1_binding2'] as String[])
   }

   /**
    * Test the value, field script work on color expression, include row,col,value,field
    */
   def 'TestCase-Table_Apply1' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_Apply1', caseName)
      vsScriptTest.printVS(null, null)
      then:
      vsScriptTest.compareImage(['TestCase-Table_Apply1_VS'] as String[])
   }

  /**
    * Test script can write to table header by parameter, variable
    * Crosstab and Freehand,Table: can set any cell value, include header and other rumtime cell value
   */
   def 'TestCase-HeaderParameter' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'TableView1',
                        COMMAND: 'table[0][0]=parameter.month1\n' +
                                'table[1][0]=month1.selectedLabel\n' +
                                'table[1][1]=month2.selectedObject']]
      def TestData2 = [[HANDLER: 'Crosstab1',
                        COMMAND: 'table[0][0]=parameter.month1\n' +
                                'table[1][1]=month1.selectedLabel\n' +
                                'table[2][2]=month2.selectedObject']]
      def TestData3 = [[HANDLER: 'FreehandTable1',
                       COMMAND: 'table[0][0]=parameter.month1\n' +
                               'table[1][0]=month1.selectedLabel\n' +
                               'table[2][0]=month2.selectedObject']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/Table_HeaderParameter', caseName)
      vsScriptTest.printVS('Table', TestData1, ['TableView1'] as String[])
      vsScriptTest.printVS('Crosstab', TestData2, ['Crosstab1'] as String[])
      vsScriptTest.printVS('FreehandTable', TestData3, ['FreehandTable1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-HeaderParameter_Table','TestCase-HeaderParameter_Crosstab','TestCase-HeaderParameter_FreehandTable'] as String[])
   }

   /**
   * setRowHeight on multi row header, and setColWidth. see Bug #60497
   */
   def 'TestCase-Freehand_setRowHeight' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/FreehandSetRowHeight', caseName)
      vsScriptTest.printVS(null, null)
      then:
      vsScriptTest.compareImage(['TestCase-Freehand_setRowHeight_VS'] as String[])
   }

   /**
   * setColumnWidthAll on freehand and crosstab, see Bug #62420
   */
   def 'TestCase-Freehand_setColumnWidthAll' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Table/SetColWidthAll', caseName)
      vsScriptTest.printVS(['(Home)', 'ColSize60'] as String[], null)
      then:
      vsScriptTest.compareImage(['TestCase-Freehand_setColumnWidthAll_VS'] as String[])
   }

   static VSScriptTest vsScriptTest
   String caseName
}

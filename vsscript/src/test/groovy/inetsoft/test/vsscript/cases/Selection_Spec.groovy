package inetsoft.test.vsscript.cases

import inetsoft.test.vsscript.VSScriptTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Selection_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test the common script of selection
    */
   def 'TestCase-Selection_Com1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONREFRESH', COMMAND: 'NullList.query = "Query2"\n' +
              'NullList.fields = ["state"]\n' +
              'NullList.measure = "price"\n' +
              'NullList.formula = StyleConstant.SUM_FORMULA\n' +
              'NullList.showText = true\n' +
              'NullList.showBar = true\n' +
              'NullList.titleVisible = false']]
      def TestData2 = [[HANDLER: 'NullList', COMMAND: 'queries = ["Query2", "EM_Base"]\n' +
              'fields = ["state"]']]
      def TestData3 = [[HANDLER: 'ONREFRESH', COMMAND: 'NullTree1.query = "Query2"\n' +
              'NullTree1.fields = ["state", "city"]\n' +
              'NullTree1.measure = "customer_id"\n' +
              'NullTree1.formula = StyleConstant.MAX_FORMULA\n' +
              'NullTree1.showText = true\n' +
              'NullTree1.showBar = true\n' +
              'NullTree1.titleVisible = false']]
      def TestData4 = [[HANDLER: 'NullTree1', COMMAND: 'queries = ["Query2", "EM_Base"]\n' +
              'fields = ["state", "city"]\n' +
              'expandAll = true']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection1', caseName)
      vsScriptTest.printVS('binding1', TestData1, ['NullList'] as String[])
      vsScriptTest.printVS('binding2', TestData2, ['NullList'] as String[])
      vsScriptTest.printVS('binding3', TestData3, ['NullTree1'] as String[])
      vsScriptTest.printVS('binding4', TestData4, ['NullTree1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Selection_Com1_binding1',
                                 'TestCase-Selection_Com1_binding2',
                                 'TestCase-Selection_Com1_binding3',
                                 'TestCase-Selection_Com1_binding4'] as String[])
   }


   /**
    * test the multi selection of selectiontree
    */
   def 'TestCase-SelTree_Multi' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.selectedObjects=["AZ"]']]
      def TestData2 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.selectedObjects=["CA","Albany"]']]
      def TestData3 = [[HANDLER: 'ONINIT', COMMAND:'SelectionTree1.selectedObjects=["Denver"]']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/SelectionTree1', caseName)
      vsScriptTest.printVS('Status1', TestData1, null)
      vsScriptTest.printVS('Status2', TestData2, null)
      vsScriptTest.printVS('Status3', TestData3, null)
      then:
      vsScriptTest.compareImage(['TestCase-SelTree_Multi_Status1', 'TestCase-SelTree_Multi_Status2','TestCase-SelTree_Multi_Status3'] as String[])
   }


   /**
    * test the single selection of selectiontree
    TestData2 filter has entra value,manula is ok,not find reason
    */
   def 'TestCase-SelTree_single1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.singleSelection = true\n'+'SelectionTree1.singleSelectionLevels=["state","city"]\n' +
      'SelectionTree1.selectedObjects=["CA"]']]
      def TestData2 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.singleSelection = true\n'+'SelectionTree1.singleSelectionLevels=["state"]\n'+
      'SelectionTree1.selectedObjects=["Los Angeles","San Francisco"]']]
      def TestData3 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.singleSelection = true\n' +'SelectionTree1.singleSelectionLevels=["city"]\n'+
      'SelectionTree1.selectedObjects=["CA","AZ"]']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/SelectionTree1', caseName)
      vsScriptTest.printVS('select1', TestData1, null)
      vsScriptTest.printVS('select2', TestData2, null)
      vsScriptTest.printVS('select3', TestData3, null)
      then:
      vsScriptTest.compareImage(['TestCase-SelTree_single1_select1', 'TestCase-SelTree_single1_select2','TestCase-SelTree_single1_select3'] as String[])
   }

   /**
    * test the single selection of selectiontree
    */
   def 'TestCase-SelTree_single2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONINIT', COMMAND: 'SelectionTree1.singleSelection = true\n'+'SelectionTree1.singleSelectionLevels=["State"]\n' +
      'SelectionTree1.selectedObjects=["USA East","USA West"]']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/SelectionTree2', caseName)
      vsScriptTest.printVS('Select1', TestData1, null)
      then:
      vsScriptTest.compareImage(['TestCase-SelTree_single2_Select1'] as String[])
   }

   /**
    * test the pc selection tree and selection
    */
   def 'TestCase-PCTree' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONINIT', COMMAND: 'PCTree.selectedObjects = ["A","B"]']]
      def TestData2 = [[HANDLER: 'ONINIT', COMMAND: 'PCTree.selectedObjects = ["A1"]']]
      def TestData3 = [[HANDLER: 'ONINIT', COMMAND: 'PCTree.singleSelection=true\n' +
      'PCTree.selectedObjects=["A11"]']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/PCTree', caseName)
      vsScriptTest.printVS('Select1', TestData1, null)
      vsScriptTest.printVS('Select2', TestData2, null)
      vsScriptTest.printVS('Select3', TestData3, ['PCTree', 'TableView1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-PCTree_Select1', 'TestCase-PCTree_Select2','TestCase-PCTree_Select3'] as String[])
   }

   /**
    * test the other filter to selectiontree and other assembly filter
    */
   def 'TestCase-Calendar_Select' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'ONINIT', COMMAND: 'Calendar1.selectedObjects = [ Calendar1.min,dateAdd("m",2,Calendar1.min)]']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Relation', caseName)
      vsScriptTest.printVS('Data', TestData1, ['Chart1','Crosstab1','CurrentSelection1','Calendar1','SelectionTree1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Calendar_Select_Data'] as String[])
   }
   /**
    * test the select tree different status
    */
   def 'TestCase-Selection-self' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'SelectionList1', COMMAND: 'selectedObjects = ["false"]\n' +
              'title = "title:" + selectedObjects[0]\n' +
              'sortType = StyleConstant.SORT_DESC\n' +
              'suppressBlank = true']]
      def TestData2 = [[HANDLER: 'SelectionTree1', COMMAND: 'selectedObjects=["false", "AK"]\n' +
              'title = "Title:" + selectedObjects[0] + ":" + selectedObjects.length\n' +
              'sortType = StyleConstant.SORT_ASC\n' +
              'suppressBlank = true'], [
                      HANDLER: 'Text1',
                      COMMAND: 'value = "drillMember:" + SelectionTree1.drillMember + ";\\n drillMembers:" + ' +
                              'SelectionTree1.drillMembers[0] + "-" + SelectionTree1.drillMembers[1]'
              ]]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection2', caseName)
      vsScriptTest.printVS('script1', TestData1, ['SelectionList1'] as String[])
      vsScriptTest.printVS('script2', TestData2, ['SelectionTree1', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Selection-self_script1', 'TestCase-Selection-self_script2'] as String[])
   }
   /**
    * test the script of range slider and rangeSlider
    */
   def 'TestCase-Range_Bind1' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'query = "Query2"\n' +
              'rangeType = StyleConstant.NUMBER\n' +
              'fields = ["customer_id"]']]
      def TestData2 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'queries = ["Query2", "EM_Base"]\n' +
              'rangeType = StyleConstant.NUMBER\n' +
              'fields = ["customer_id"]']]
      def TestData3 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = true\n' +
              'queries = ["Query2"]\n' +
              'fields = ["reseller","state"]']]
      def TestData4 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = true\n' +
              'queries = ["Query2", "EM_Base"]\n' +
              'fields = ["state", "city"]']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection1', caseName)
      vsScriptTest.printVS('singleBinding1', TestData1, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('singleBinding2', TestData2, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('compositeBinding1', TestData3, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('compositeBinding2', TestData4, ['NullRangeSlider1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Range_Bind1_singleBinding1',
                                 'TestCase-Range_Bind1_singleBinding2',
                                 'TestCase-Range_Bind1_compositeBinding1',
                                 'TestCase-Range_Bind1_compositeBinding2'] as String[])
   }

   /**
    * test the script of range slider to binding date column
    */
   def 'TestCase-Range_Bind2' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'query = "Query2"\n' +
              'fields = ["orderdate"]\n' +
              'rangeType=StyleConstant.YEAR\n' +
              'maxRangeSize = 2\n' +
              'tickVisible = false\n' +
              'min = new Date("1996-01-01")\n' +
              'max = new Date("2002-12-31")']]
      def TestData2 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'query = "Query2"\n' +
              'fields = ["orderdate"]\n' +
              'rangeType=StyleConstant.MONTH\n' +
              'maxRangeSize = 6\n' +
              'tickVisible = false\n' +
              'min = new Date("1996-10-01")\n' +
              'max = new Date("2002-10-31")']]
      def TestData3 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'query = "Query2"\n' +
              'fields = ["orderdate"]\n' +
              'rangeType=StyleConstant.DAY\n' +
              'maxRangeSize = 8\n' +
              'tickVisible = false\n' +
              'min = new Date("1997-10-01")\n' +
              'max = new Date("1997-10-31")']]
      def TestData4 = [[HANDLER: 'NullRangeSlider1', COMMAND: 'composite = false\n' +
              'query = "EM_Base"\n' +
              'fields = ["orderdate"]\n' +
              'rangeType = StyleConstant.HOUR\n' +
              'maxRangeSize =5\n' +
              'tickVisible = false\n' +
              'format = StyleConstant.DATE_FORMAT\n' +
              'formatSpec = "HH"']]
      def TestData5 = [[HANDLER: 'NullRangeSlider1', COMMAND:'composite = false\n' +
              'query = "EM_Base"\n' +
              'fields = ["orderdate"]\n' +
              'rangeType = StyleConstant.MINUTE\n' +
              'maxRangeSize = 20\n' +
              'tickVisible = false\n' +
              'format = StyleConstant.DATE_FORMAT\n' +
              'formatSpec = "hh:mm"']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection1', caseName)
      vsScriptTest.printVS('yearBind', TestData1, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('monthBind', TestData2, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('dayBind', TestData3, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('hourBind', TestData4, ['NullRangeSlider1'] as String[])
      vsScriptTest.printVS('minuteBind', TestData5, ['NullRangeSlider1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Range_Bind2_yearBind',
                                 'TestCase-Range_Bind2_monthBind',
                                 'TestCase-Range_Bind2_dayBind',
                                 'TestCase-Range_Bind2_hourBind',
                                 'TestCase-Range_Bind2_minuteBind'] as String[])
   }

   /**
    * test the range slider other script
    */
   def 'TestCase-Range-Self' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'RangeSlider1', COMMAND: 'minVisible = false\n' +
              'maxVisible = false\n' +
              'tickVisible = false\n' +
              'currentVisible = false']]
      def TestData2 = [[HANDLER: 'ONINIT', COMMAND: 'RangeSlider1.rangeMin = 2\n' +
              'RangeSlider1.rangeMax = 4']]
      def TestData3 = [[HANDLER: 'CurrentSelection1', COMMAND: 'RangeSlider2.title ="range slider length:" + ' +
              'RangeSlider2.length']]
      def TestData4 =[[HANDLER: 'RangeSlider1', COMMAND: 'logScale = true\n' +
              'upperInclusive = true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection2', caseName)
      vsScriptTest.printVS('visible', TestData1, ['RangeSlider1'] as String[])
      vsScriptTest.printVS('rangeMinMax', TestData2, ['RangeSlider1', 'TableView1'] as String[])
      vsScriptTest.printVS('titleLength', TestData3, ['CurrentSelection1', 'RangeSlider2'] as String[])
      vsScriptTest.printVS('logScale', TestData4, ['RangeSlider1', 'TableView1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Range-Self_visible',
                                 'TestCase-Range-Self_rangeMinMax',
                                 'TestCase-Range-Self_titleLength',
                                 'TestCase-Range-Self_logScale'] as String[])
   }

   /**
    * test the range slider script on composite binding
    */
   def 'TestCase-Range-Composite' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'RangeSlider3', COMMAND: 'min = ["CA", "Los Angles"]\n' +
              'max = ["I", "Rome"]']]
      def TestData2 = [[HANDLER: 'ONINIT', COMMAND: 'RangeSlider3.rangeMin = ["CA", "Los Angles"]\n' +
              'RangeSlider3.rangeMax = ["I", "Rome"]'], [
                      HANDLER: 'Text1',
                      COMMAND: 'var arrs = RangeSlider3.selectedObjects\n' +
                              'value = ""\n' +
                              'for(var i = 0; i< arrs.length; i++) {\n' +
                              '   value += arrs[i] + ", "\n' +
                              '}'
              ]]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection2', caseName)
      vsScriptTest.printVS('minMax', TestData1, ['RangeSlider3'] as String[])
      vsScriptTest.printVS('rangeMinMax', TestData2, ['RangeSlider3', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Range-Composite_minMax',
                                 'TestCase-Range-Composite_rangeMinMax'] as String[])
   }

   /**
    * test the calendar script
    */
   def 'TestCase-Calendar-binding' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData2 = [[HANDLER: 'NullCalendar1', COMMAND: 'queries = ["Query2", "EM_Base"]\n' +
              'fields = ["orderdate"]\n' +
              'titleVisible = false\n' +
              'selectedObjects = ["w2002-10-3"]\n' +
              'var startDate = formatDate(NullCalendar1.min,\'MM/d/yy\')\n' +
              'var endDate = formatDate(NullCalendar1.max,\'MM/d/yy\');\n' +
              'Text1.value =  NullCalendar1.selectedObjects[0] + " ," +  startDate + ",  " + endDate;\n' +
              'weekFormat="E"']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection1', caseName)
      vsScriptTest.printVS('binding2', TestData2, ['NullCalendar1', 'Text1'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Calendar-binding_binding2'] as String[])
   }

   /**
    * test the selection container script
    */
   def 'TestCase-Container-script' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 = [[HANDLER: 'CurrentSelection1', COMMAND: 'titleVisible = false\n' +
              'showCurrentSelection = false']]
      def TestData2 = [[HANDLER: 'CurrentSelection1', COMMAND: 'title = "adhoc is enable:" + adhocEnabled\n' +
              'showCurrentSelection = true']]
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection2', caseName)
      vsScriptTest.printVS('titleVisible', TestData1, ['CurrentSelection1', 'RangeSlider2'] as String[])
      vsScriptTest.printVS('showSelection', TestData2, ['CurrentSelection1', 'RangeSlider2'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Container-script_titleVisible',
                                 'TestCase-Container-script_showSelection'] as String[])
   }

   /**
    * test parseDate on Calendar1.selectedObject  Bug #58056
    */
   def 'TestCase_Calendar_ParseDate' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Calendar_ParseDate', caseName)
      vsScriptTest.printVS(['(Home)', 'bk1'] as String[], null)

      then:
      vsScriptTest.compareImage(['TestCase_Calendar_ParseDate_VS'] as String[])
   }

   /**
    * test calendar min and max
    */
   def 'TestCase_Calendar_MinMax' () {
      given:
      caseName = specificationContext.currentIteration.name
      def TestData1 =  [[HANDLER: 'ONREFRESH', COMMAND: 'Calendar1.min="2023-5-1"; Calendar1.max="2023-5-31"\n' +
              'Calendar2.min=new Date(2000,4,10);Calendar2.max=new Date(2000,4,28)']]

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Calendar1', caseName)
      vsScriptTest.printVS('minmax', TestData1, null)

      then:
      vsScriptTest.compareImage(['TestCase_Calendar_MinMax_minmax'] as String[])
   }

   /**
    * test selection and checkbox selectFirstItemOnLoad Script
    * the script execute on first open, so must set script on vs.
    */
   def 'TestCase_Selection_SelectedFirstItem' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/Selection3', caseName)
      vsScriptTest.printVS(null, null)

      then:
      vsScriptTest.compareImage(['TestCase_Selection_SelectedFirstItem_VS'] as String[])
   }

   /**
    * test Selection container empty Script
    */
   def 'TestCase_Selection_SelectionContainer' () {
      given:
      caseName = specificationContext.currentIteration.name

      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/Selection/SelectionContainer', caseName)
      vsScriptTest.printVS(['(Home)', 'bk1'] as String[], null)

      then:
      vsScriptTest.compareImage(['TestCase_Selection_SelectionContainer_VS'] as String[])
   }


   static VSScriptTest vsScriptTest
   String caseName
}

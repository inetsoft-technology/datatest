package inetsoft.test.cases.Actions

import spock.lang.Specification

class Actions_Spec extends Specification{
   static VSActionsTest vsactionsTest
   static String caseName

   def setup() {
      VSActionsTest.initHome(this.class.getName())
   }

   //test brush on multiple style chart
   def 'Brush_Case1'() {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/Brush_Case1', caseName)
      def objs = [[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Medal^VALUE:Bronze'
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Country^VALUE:ROC'
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: null
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Sum(Measure2)^INDEX:3'
        ]]

      vsactionsTest.testBrushOnChart(null, objs)

      expect:
      	vsactionsTest.compareImage(null)
   }

   //test brush on hybrid table chart
   def 'Brush_Case2'() {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/Brush_Case2', caseName)
      def objs = [[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Product:Category^VALUE:Business^INDEX:0'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Customer:State^VALUE:MA'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: null
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Sum(Product:Total)^INDEX:4``Sum(Product:Total)^INDEX:2``Sum(Product:Total)^INDEX:1'
        ]]

      vsactionsTest.testBrushOnChart(null, objs)

      expect:
      	vsactionsTest.compareImage(null)
   }

   //test brush on dc chart
   def 'Brush_Case3'() {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/Brush_Case3', caseName)
      def objs = [[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Year(Order Date)^VALUE:2021-01-01 00:00:00'
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Product Category^VALUE:Furniture'
        ]]

      vsactionsTest.testBrushOnChart(null, objs)

      expect:
      	vsactionsTest.compareImage(null)
   }

   //test brush on sepcial chart & worldcloud
   def 'Brush_Case4'() {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/Brush_Case4', caseName)
      def objs = [[
        chartName: 'Chart1',
        isRange: false,
        selected: 'state^VALUE:NJ'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'ColorGroup(state)^VALUE:CA'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'reseller^VALUE:true^INDEX:1'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: null
        ],[
        chartName: 'Chart3',
        isRange: false,
        selected: 'product_name^INDEX:2'
        ]]

      vsactionsTest.testBrushOnChart(null, objs)

      expect:
      	vsactionsTest.compareImage(null)
   }


   //test brush on sepcial chart & worldcloud
   def 'Brush_Case5'() {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/Brush_Case5', caseName)
      def objs = [[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Reseller^VALUE:1'
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: 'Category^VALUE:Graphics'
        ],[
        chartName: 'Chart1',
        isRange: false,
        selected: null
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Running Sum: Sum(Quantity Purchased)^INDEX:2'
        ],[
        chartName: 'Chart2',
        isRange: false,
        selected: 'Year(Date)^VALUE:2020-01-01 00:00:00^INDEX:2'
        ]]

      vsactionsTest.testBrushOnChart(null, objs)

      expect:
     	vsactionsTest.compareImage(null)
   }

   //check show detials on multiple styles and special chart
   def 'ShowDetail_Case1' () {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/ShowDetail_Case1', caseName)
      def objs = [[
         chartName: 'Chart1',
         colIdx: 0,
         isRange: false,
         selected: 'state^VALUE:WA^INDEX:13'
         ],[
         chartName: 'Chart1',
         colIdx: 0,
         isRange: true,
         selected: 'Sum(customer_id)^INDEX:8'
         ],[
         chartName: 'Chart2',
         colIdx: 0,
         isRange: true,
         selected: 'DataGroup(state)^VALUE:C_state^INDEX:1'
         ],[
         chartName: 'Chart3',
         colIdx: 0,
         isRange: true,
         selected: 'Max_M^INDEX:13'
         ],[
         chartName: 'Chart4',
         colIdx: 0,
         isRange: true,
         selected: 'Sum(M)^INDEX:1'
         ]]

      vsactionsTest.testShowDetailOnChart(null, objs)

      expect:
      	vsactionsTest.compareData(null)
   }

   //check show detials on dc chart
   def 'ShowDetail_Case2' () {
      caseName = specificationContext.currentIteration.name
      vsactionsTest = new VSActionsTest('1^128^__NULL__^Chart/Actions/ShowDetail_Case2', caseName)
      def objs = [[
         chartName: 'Chart1',
         colIdx: 0,
         isRange: false,
         selected: 'Sum(Quantity Purchased)^INDEX:2'
         ],[
         chartName: 'Chart1',
         colIdx: 0,
         isRange: false,
         selected: 'Day(Date)^VALUE:2018-10-01'
         ],[
         chartName: 'Chart1',
         colIdx: 0,
         isRange: false,
         selected: 'Sum(Quantity Purchased)^INDEX:1``Sum(Quantity Purchased)^INDEX:10'
         ],[
         chartName: 'Chart2',
         colIdx: 0,
         isRange: false,
         selected: 'MonthOfQuarter(Date)^VALUE:1^INDEX:0'
         ],[
         chartName: 'Chart2',
         colIdx: 0,
         isRange: false,
         selected: 'Quarter(Date)^VALUE:2019-10-01 00:00:00^INDEX:7^AND^MonthOfQuarter(Date)^VALUE:2``Quarter(Date)^VALUE:2019-04-01 00:00:00^INDEX:5^AND^MonthOfQuarter(Date)^VALUE:2'
         ],[
         chartName: 'Chart2',
         colIdx: 0,
         isRange: false,
         selected: '% Change from previous quarter of Quarter(Date): Sum(Paid)^INDEX:12``Sum(Paid)^INDEX:7'
         ]]

      vsactionsTest.testShowDetailOnChart(null, objs)

      expect:
      	vsactionsTest.compareData(null)
   }
}

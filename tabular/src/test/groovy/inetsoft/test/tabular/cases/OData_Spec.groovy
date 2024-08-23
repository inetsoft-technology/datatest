package inetsoft.test.tabular.cases

import inetsoft.test.global.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class OData_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check OData query and database query
    */
   def 'OData query and database query1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^OData/OData Base Query', null)
      expect:
      globalTest.compareData()
   }

    /*
    check OData query and database query
    */
   def 'OData query and database query2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^OData/OData Query', null)
      expect:
      globalTest.compareData()
   }

   /*
    check OData query and database query
    */
   def 'OData query and database query3'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^OData/OData Query_filter', null)
      expect:
      globalTest.compareData()
   }

   /*
    check OData query and database query
    */
   def 'OData query and database query4'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^OData/OData Query_function', null)
      expect:
      globalTest.compareData()
   }

    /*
    check OData query and database query
    */
   def 'OData query and database query5'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^OData/OData Query1', null)
      expect:
      globalTest.compareData()
   }

   /*
    check OData used in vs and report
    */
   def 'OData used in vs and report'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      paras1.put('Date', ['2000-01-01 00:00:00','2006-01-01 00:00:00'] as String[])
      globalTest.executeTest('1^128^__NULL__^OData/OData VS', ['(Home)', 'bk_brush', 'bk_filter'] as String[], paras1)

      expect:
      globalTest.compareImage()
   }
}

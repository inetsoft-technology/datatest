package inetsoft.test.tabular.cases

import inetsoft.test.modules.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class DataGov_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   @IgnoreRest
   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check DataGov query and database query
    */
   def 'DataGov query and database query'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      paras1.put('impact', ['10'] as String[])
      globalTest.executeTest('1^2^__NULL__^DataGov/DataGov DataBase Query', null)

      expect:
      globalTest.compareData()
   }

   /*
    check DataGov used in vs
    */
   def 'DataGov used in vs'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras = new HashMap<String, String[]>()
      paras.put('para1', ['2013-04-01 00:00:00'] as String[])
      paras.put('para2', ['2014-04-01 00:00:00'] as String[])
      globalTest.executeTest('1^128^__NULL__^DataGov/DataGov VS', ['(Home)', 'bk_filter', 'style' ] as String[], paras)

      expect:
      globalTest.compareImage()
   }
}

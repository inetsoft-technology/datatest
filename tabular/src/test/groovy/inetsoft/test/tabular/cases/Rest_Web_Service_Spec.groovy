package inetsoft.test.tabular.cases

import inetsoft.test.modules.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Issue

class Rest_Web_Service_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check Rest_Web_Service query and database query
    */
   def 'Rest_Web_Service query and database query'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/Rest Database Query', null)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/RestQuery', null)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/RestJsonPath', null)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/RestURLSuffix', null)

      expect:
      globalTest.compareData()
   }

   /*
    check Rest_Web_Service used in vs and report
    */
   def 'Rest_Web_Service used in vs and report'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      def paras2 = new HashMap<String, String[]>()
      paras1.put('var1', ['NY','NJ'] as String[])
      paras2.put('para', ['true'] as String[])
      globalTest.executeTest('1^128^__NULL__^Rest_Web_Service/Rest VS', ['(Home)','bk_Zoom', 'bk_brush' ,'bk_filter'] as String[], paras1)

      expect:
      globalTest.compareImage()
   }

   /*
    check Feature #53817
    */
   def 'Feature #53817'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/Feature #53817', null)

      expect:
      globalTest.compareData()
   }

   /*
    check lookup query,manual check,data is different in every time
    */
   @Issue('value is dynamic,need manual check')
   def 'lookup query'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^Rest_Web_Service/lookupcase', null)

      expect:
      1 == 1
   }
}

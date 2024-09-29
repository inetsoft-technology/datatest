package inetsoft.test.tabular.cases

import inetsoft.test.modules.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class ElasticSearch_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check elasticsearch query and database query
    */
   def 'elasticsearch query and database query'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^ElasticSearch Rest/ElasticSearch Database Query', null)
      globalTest.executeTest('1^2^__NULL__^ElasticSearch Rest/ElasticSearch Query', null)

      expect:
      globalTest.compareData()
   }

   /*
    check elasticsearch used in vs
    */
   def 'elasticsearch used in vs'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      def paras2 = new HashMap<String, String[]>()
      paras1.put('2015-05-18 19', ['2015-05-18 19:00:00'] as String[])
      paras2.put('date', ['2015-05-18 19:00:00'] as String[])
      globalTest.executeTest('1^128^__NULL__^ElasticSearch Rest/ElasticSearch VS', ['Home','bk1', 'filter'] as String[], paras1)
      globalTest.executeTest('ElasticSearch Rest/Elastic Report', paras2)

      expect:
      globalTest.compareImage()
   }
}

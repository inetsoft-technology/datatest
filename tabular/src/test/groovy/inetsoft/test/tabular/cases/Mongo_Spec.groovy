package inetsoft.test.tabular.cases

import inetsoft.test.global.GlobalTest
import spock.lang.Issue
import spock.lang.Specification

class Mongo_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check mongoDB jason query and database query
    check WS composite table
    */
   @Issue('Inner Join table auto exist issue with mv.spark.enabled property,maual is ok')
   def 'mongoDB query and composite table'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      paras1.put('company', ['inetsoft'] as String[])

      globalTest.executeTest('1^2^__NULL__^MongoDB/MongoJasonQuery', paras1)
      globalTest.executeTest('1^2^__NULL__^MongoDB/WS_CompositeTable', null)

      expect:
      globalTest.compareData()
   }

   /*
    check mongoDB used in vs and report
    */
   def 'mongoDB used in vs and report'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      def paras2 = new HashMap<String, String[]>()
      paras1.put('para',['false'] as String[])
      paras2.put('var_date', ['2015-07-29 00:00:00'] as String[])
      paras2.put('var1',['B'] as String[])
      globalTest.executeTest('1^128^__NULL__^MongoDB/VS_Parameters', paras2)
      globalTest.executeTest('1^128^__NULL__^MongoDB/VS_Filter', ['(Home)','filter1'] as String[], null)

      expect:
      globalTest.compareImage()
   }
}

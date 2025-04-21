package inetsoft.test.tabular.cases

import inetsoft.test.modules.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification

@IgnoreRest
class Cassandra_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check cassandra sql query and database query
    covered composite table and condition
    */
   def 'cassandraDB query and composite table'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      paras1.put('var_date1', ['2014-08-03 00:00:00'] as String[])
      paras1.put('var_date2', ['2015-05-03 00:00:00'] as String[])
      globalTest.executeTest('1^2^__NULL__^Cassandra/WS_Condition&Parameter', paras1)
      globalTest.executeTest('1^2^__NULL__^Cassandra/WS_Join&Concatenate', null)

      expect:
      globalTest.compareData()
   }

   /*
    check cassandra used in vs
    covered parameters and filters
    */
   def 'cassandra used in vs'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      def paras2 = new HashMap<String, String[]>()
      paras1.put('day1', ['2014-07-03'] as String[])
      paras1.put('day2', ['2015-05-03'] as String[])
      paras2.put('var_date1', ['2014-05-02 20:13:23'] as String[])
      paras2.put('var_date2', ['2015-03-03 20:13:23'] as String[])
      paras2.put('var_f_paid', ['143'] as String[])
      globalTest.executeTest('1^128^__NULL__^Cassandra/VS_Parameters', ['Home','state_CA'] as String[], paras1)
      globalTest.executeTest('1^128^__NULL__^Cassandra/VS_Relation&Filter', ['Home','EmbTab1','filter2'] as String[], null)
      globalTest.executeTest('Cassandra/R_Parameters1', null)
      globalTest.executeTest('Cassandra/R_Parameters2', paras2)

      expect:
      globalTest.compareImage()
   }
}

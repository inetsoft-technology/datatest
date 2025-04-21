package inetsoft.test.tabular.cases

import inetsoft.test.modules.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification


class ServerFile_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check ServerFile query and database query
    */
   def 'ServerFile query and database query'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras1 = new HashMap<String, String[]>()
      paras1.put('int', ['120'] as String[])
      globalTest.executeTest('1^2^__NULL__^ServerFile/ServerFile DataBase Query',paras1)
      globalTest.executeTest('1^2^__NULL__^ServerFile/ServerFile Query', null)
      expect:
      globalTest.compareData()
   }

   /*
    check ServerFile used in vs
    */
   def 'ServerFile used in vs'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      def paras = new HashMap<String, Object>()
      paras.put('para1', 'N')
      globalTest.executeTest('1^128^__NULL__^ServerFile/ServerFile VS', ['(Home)','bk_Zoom' ,'bk_brush', 'bk_filter'] as String[], null)

      expect:
      globalTest.compareImage()
   }
}

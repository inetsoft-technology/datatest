package inetsoft.test.worksheet.cases

import inetsoft.report.composition.execution.AssetQuerySandbox
import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Specification
import inetsoft.sree.security.SRPrincipal
import spock.lang.Ignore
import spock.lang.Retry
import spock.lang.IgnoreRest

class WSAsset_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }
   /**
    * check varible apply
    */
   def 'varible' () {
      def paras = new HashMap<String, Object>()
      paras.put('v_date', ['2003-01-01 00:00:00','2004-01-01 00:00:00'] as Object[])
      paras.put('v_string', 'N')
      paras.put('v_time', '09:09:09')
      paras.put('v_time1', '11:59:59')
      paras.put('v_boolean', false)
      paras.put('v_timeinstant', '2005-10-11 22:48:36')
      paras.put('v_string1', '>=500')
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Asset/varible', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check group apply
    */
   def 'group' () {
      def paras = new HashMap<String, Object>()
      paras.put('var1', '2002-01-05 05:15:11')
      paras.put('var2', '2002-12-30 10:27:55')
      paras.put('var3', 'True')
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Asset/group', paras, null)

      expect:
      wstest.compareData(null)
   }

}

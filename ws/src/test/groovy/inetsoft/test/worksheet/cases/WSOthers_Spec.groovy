package inetsoft.test.worksheet.cases

import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Ignore
import spock.lang.Specification

class WSOthers_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }

   @Ignore
   def 'ws group case check' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^wsCheck/ws1', null, null)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/vs1', null)

      expect:
      wstest.compareData(['VS'] as String[])
   }


   /**
    * check script formula cell ref1
    */
   def 'formula cellref1' () {
      caseName =  specificationContext.currentIteration.name
      def paras = new HashMap<String, Object>()
      paras.put('pid', 3)
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/CellRef1', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check script formula cell ref2
    */
   def 'formula cellref2' () {
      caseName =  specificationContext.currentIteration.name
      caseName =  specificationContext.currentIteration.name
      def paras = new HashMap<String, Object>()
      paras.put('startDate', '2012-12-01')
      paras.put('endDate', '2035-12-01')
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/CellRef2', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check sql formula normal function and key words
    */
   def 'formula mysql formula' () {
      caseName =  specificationContext.currentIteration.name
      def paras = new HashMap<String, Object>()
      paras.put('pcid', 10)
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/mysql sql formula', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check oracle formula normal function and key words
    */
   def 'formula oracle formula' () {
      caseName =  specificationContext.currentIteration.name
      def paras = new HashMap<String, Object>()
      paras.put('pnum', 10)
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/oracle sql formula', paras, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check sqlserver formula normal function and key words
    */
   def 'formula sqlserver formula' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/sqlserver sql formula', null, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check db2, postgres, informix formula normal function and key words
    */
   def 'formula other db formula' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Formula/others sql formula', null, null)

      expect:
      wstest.compareData(null)
   }

   /**
    * check top sql query used in vs and report, for bug #60292 and #60325
    */
   def 'top sql query' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/top query', null)

      expect:
      wstest.compareData(['VS'] as String[])
   }

   /**
    * check mysql sql query used in vs and report, for bug #60394 and #60460
    */
   def 'mysql sql query' () {
      def paras = ['contactid': ['20'] as String[], 'y2005': ['2005-01-01'] as String[]]
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/mysql sql query', paras)

      expect:
      wstest.compareData(['VS'] as String[])
   }
}

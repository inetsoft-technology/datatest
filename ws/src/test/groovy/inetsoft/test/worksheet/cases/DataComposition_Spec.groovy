package inetsoft.test.worksheet.cases

import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Specification
import spock.lang.Ignore
import spock.lang.IgnoreRest

class DataComposition_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }

   def 'checkMergeable CM1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/checkMergeable/CM1', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'checkMergeable CM2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/checkMergeable/CM2',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM1',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM2',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM3' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM3',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM4' () {
      caseName =  specificationContext.currentIteration.name
      def paras = new HashMap<String, Object>()
      paras.put('Region_id', '2')
      paras.put('State', 'MA')
      paras.put('Date', '2002-01-11')
      paras.put('DateTime', '2002-01-06 01:55:35')
      paras.put('Time', '05:23:23')
      paras.put('Discount', '0.1')
      paras.put('State2', 'CA')
      paras.put('Customer_id', '5')
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM4',paras, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM5' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM5', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM7' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM7', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'merge CM8' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/merge/CM8', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'postprocess' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^datacomposition/postprocess/postprocess', null, null)

      expect:
      wstest.compareData(null)
   }
}
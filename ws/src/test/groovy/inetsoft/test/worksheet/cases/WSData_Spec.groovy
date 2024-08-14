package inetsoft.test.worksheet.cases

import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Specification
import inetsoft.sree.security.SRPrincipal
import spock.lang.Ignore
import spock.lang.Retry
import spock.lang.IgnoreRest

class WSData_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
   }

   @Ignore
   def 'Bug' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/Bug', null, null)

      expect:
      wstest.compareData(null)
   }
   @Ignore
   def 'innerJoin1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/innerJoin1', null, null)

      expect:
      wstest.compareData(null)
   }

   @Ignore
   @Retry(count = 3)
   def 'innerJoin2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/innerJoin2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'crossJoin' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/crossJoin', null, null)

      expect:
      wstest.compareData(null)
   }
   @Ignore
   def 'concatenate' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/concatenate', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'unpivot' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/unpivot',null, null)

      expect:
      wstest.compareData(null)
   }

   //Bug #49283, Bug #49276
   def 'wsMaxRow' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/wsMaxRow', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'mirror' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/mirror', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'rotate' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/rotate', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'grouping1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/grouping1', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'grouping2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/grouping2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'condition1' () {
      def paras = new HashMap<String, Object>()
      paras.put('v_string', 'T')
      paras.put('v_num', '5')
      paras.put('v_date', '2004-11-10')
      paras.put('v_datetime', ['2002-01-01','2003-10-01'] as Object[])
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition1', paras, null)

      expect:
      wstest.compareData(null)
   }

    def 'condition2' () {
      def paras = new HashMap<String, Object>()
      paras.put('num1', 3)
      paras.put('num2', 5)
      paras.put('vBoolean', true)
      caseName =  specificationContext.currentIteration.name

      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition2', paras, null)

      expect:
      wstest.compareData(null)
   }

    def 'range&formula' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/range&formula',null, null)

      expect:
      wstest.compareData(null)
   }

    @Ignore
    def 'upload file' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/upload file', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'condition_session' () {
      caseName =  specificationContext.currentIteration.name
      SRPrincipal paiduser = TUtil.createPrincipal('paiduser', ['Everyone', 'Administrator'] as String[], ['DTest'] as String[])
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/condition_session', null, null, paiduser, null)
      expect:
      wstest.compareData(null)
   }

   def 'specialChar' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/specialChar', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'crosstab' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/crosstab',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'databaseQuery' () {
      def paras = new HashMap<String, Object>()
      paras.put('sum_cid', '25')
      paras.put('contactid', '10')
      paras.put('y2005', '2005-01-01')
      paras.put('cid', '30')
      def params1 = ['sum_cid': ['25'] as String[], 'contactid': ['20'] as String[],
        'y2005': ['2005-01-01'] as String[], 'cid': ['30'] as String[]]
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/databaseQuery', paras, null)
      wstest.exportVSComponentData('1^128^__NULL__^vsCheck/sql database query', params1)

      expect:
      wstest.compareData(['VS'] as String[])
   }

   def 'sqliteDateTest' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^LogicalChange/sqliteDateTest',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'emptyheader' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/emptyheader', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'columnalias' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest( caseName)
      wstest.executeWS('1^2^__NULL__^Common/columnalias',null, null)

      expect:
      wstest.compareData(null)
   }

   def 'SqlQuery' () {
      def paras = new HashMap<String, Object>()
      paras.put('sum_cid', '25')
      paras.put('contactid', '10')
      paras.put('y2005', '2005-01-01')
      paras.put('cid', '30')
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^Common/sql query', paras, null)

      expect:
      wstest.compareData(null)
   }
}
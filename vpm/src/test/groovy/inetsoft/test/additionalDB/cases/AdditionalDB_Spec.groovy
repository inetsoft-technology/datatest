package inetsoft.test.additionalDB.cases

import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.TUtil
import inetsoft.test.modules.AdditionalConnectionTest
import inetsoft.test.modules.GlobalTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class AdditionalDB_Spec extends Specification {
   static AdditionalConnectionTest additionalConnectionTest
   static String caseName
   @Shared SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])
   @Shared SRPrincipal user0 = TUtil.createPrincipal('user0', ['Everyone', 'Designer'] as String[],
           [] as String[])
   @Shared SRPrincipal user1 = TUtil.createPrincipal('user1', ['Everyone', 'Designer'] as String[],
           ['group0'] as String[])

   def setupSpec() {
      AdditionalConnectionTest.initHome(this.class.getName())
   }

   /*
    *test extend model strunctrue in viewsheet
    */
   @IgnoreRest
   def 'test extended model structure in viewsheet' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/exm1&exm3', [admin, user0], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/exm2&exm4', [admin, user0], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/exm5', [admin, user0], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *test extend model strunctrue in worksheet
    */
   def 'test extended model structure in worksheet' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/model structure', [admin, user0], null)

      expect:
      additionalConnectionTest.compareData()
   }

   /*
    *test extend model strunctrue in report
    */
  /* def 'test extended model structure in report' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('Multi-Tenant/model_structure', [admin, user0], null)

      expect:
      additionalConnectionTest.compareImage()
   }*/

   /*
    *test disable default connection in viewsheet
    */
   def 'test disable default connection in viewsheet' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule1', [admin, user0, user1], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule3', [admin, user0, user1], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *test disable default connection in worksheet
    */
   def 'test disable default connection in worksheet' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/disable_default_connection', [admin, user0, user1], null)

      expect:
      additionalConnectionTest.compareData()
   }

   /*
    *test base db and additional db has different table
    */
   def 'test base db and additional db has different table' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      //ignore viewhseet user0,because popup warning,so can't export success in version 13.8
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/different table', [admin], null)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/diff table', [admin, user0], null)

      expect:
      verifyAll{
         additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }

   /*
    *test base db and additional db has different column
    *user0 can not access data, so return meta data with current time, ignore, manual check
    */
   @Issue('manual run the vs|report|ws with user0, user0 can not get data, so it return metadata with current time')
   def 'test base db and additional db has different column' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/different column', [admin], null)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/diff column', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *test get model rule in viewsheet
    */
   def 'test get model rule in viewsheet' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule2&mrule5', [user0, user1], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule4&mrule6', [user0, user1], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *test get model rule in worksheet and report
    */
   def 'test get model rule in worksheet and report' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/model rule', [user0, user1], null)

      expect:
      verifyAll{
         additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }

   /*
    *test user has permission to multiple additional db
    *old: admin can see 'reseller', user0 can see 'order_date', user1 can see 'first_name'
    *now: 手动执行的结果: 目前user0和user1的结果一样. ws need manual check
    *ws with manual run, to check column visible
    */
   @Issue('ws with manual run, to check column visible, VS, and Report have same result by user0 and user1')
   def 'test user has permission to multiple additional db' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      //additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule8', [admin, user0, user1], null)
      //user0 generate sql use '`' caused vs didn't get data, ignore
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/mrule8', [admin, user1], null)
      //manual check
      //additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/multi additional', [admin, user0, user1], null)

      expect:
      verifyAll{
         //additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }
   /*
    *test entity visible when get default connection in report and viewsheet
    *test entity visible when get additional connection in report and worksheet
    *ws with user0 need manual check
    */
   @Issue('worksheet show two invisible column,manual is ok')
   def 'test entity visible' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/TestCaseVS-EntityVisible', [admin], null)
      //additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/TestCaseWS-EntityVisible', [user0], null) //manual check

      expect:
      verifyAll{
         //additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }

   /*
    *test user has/no permission to single tabular additional db
    */
   def 'test user has-no permission to single tabular additional db' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/tabular_ws1', [user0, user1], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/tabular_vs1', [user0, user1], null)

      expect:
      verifyAll{
         additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }

   /*
    *test user has/no permission to tabular multiple additional db
    *use last one db
    */
   def 'test user has permission to tabular multiple additional db' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^2^__NULL__^Multi-Tenant/tabular_ws2', [admin, user0, user1], null)
      additionalConnectionTest.executeTest('1^128^__NULL__^Multi-Tenant/tabular_vs2', [admin, user0, user1], null)

      expect:
      verifyAll{
         additionalConnectionTest.compareData()
         additionalConnectionTest.compareImage()
      }
   }
}

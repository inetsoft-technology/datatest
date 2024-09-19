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

class AdditionalDBVPM_Spec extends Specification {
   static GlobalTest globalTest
   static AdditionalConnectionTest additionalConnectionTest
   static String caseName
   @Shared SRPrincipal admin = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[],
           [] as String[])
   @Shared SRPrincipal user0 = TUtil.createPrincipal('user0', ['Everyone', 'Designer'] as String[],
           [] as String[])
   // @Shared SRPrincipal informix = TUtil.createPrincipal('informix', ['Everyone', 'Designer'] as String[],
   //          [] as String[])
   @Shared SRPrincipal user1 = TUtil.createPrincipal('user1', ['Everyone', 'Designer'] as String[],
           ['group0'] as String[])

   def setupSpec() {
      AdditionalConnectionTest.initHome(this.class.getName())
   }

   /*
    *test has one vpm under a ds and define one condition
    *test multi vpm apply on one partition
    */
   def 'test one and multi vpm apply' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M1_C1', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
    /*
    *test vpm applied,define vpm condition by script
    */
   def 'test define vpm condition by script' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M1_C2', [admin, user1], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test multi conditions for same partition
    *test multi conditions for diffrent partition
    */
   def 'test multi conditions for same or diffderent partition' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M1_C3', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test vpm conditions for partition and table
    */
   def 'test vpm conditions for partition and table' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M1_C4', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test add join relationship; condition type is unary such as [field] is null
    */
   def 'test condition type is unary' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M2_C1', [user0], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test add join relationship;
    *condition type is binary such as [field] op [field]
    *condition type is binary such as [field] op value
    *condition type is trinary such as [field] between [field] and [field]
    *condition type is trinary such as [field] between [subquery] and [subquery]
    */
   def 'test condition type' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/TestcaseVS-VPM_M2_C2', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test binding normal table from base partition
    *test binding manual alias-one level table from base partition
    *test binding manual alias-two level table from base partition
    *test binding auto alias-alias table from base partition
    *test binding auto alias-prefix alias table from base partition
    */
   def 'test get base partition1' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M3_C1', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *condition use manual alias-one level table from base partition
    *test condition use manual alias-two level  table from base partition
    *test condition use auto alias-alias table from base partition
    *test condition use auto alias-prefix alias table from base partition
    */
   def 'test get base partition2' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M3_C2', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
   /*
    *test condition defined on normal table from base partition,binding normal table from extended partition
    *test condition defined on normal table from base partition,binding manual alias-two level table from extended partition
    *test condition defined on normal table from base partition,binding auto alias-alias table from extended partition
    *test condition defined on normal table from base partition,binding auto alias-prefix alias table from extended partition
    */
   def 'test get extended partition1' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M3_C3', [user0], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *test condition defined on manual alias-one level table from base partition,binding normal table from extended partition
    *test condition defined on manual alias-two  level table  from base partition,binding normal table from extended partition
    *test condition defined on auto alias-alias table from base partition,binding normal table from extended partition
    *test condition defined on auto alias-prefix alias table from base partition,binding prefix alias table from extended partition
    table Model29 export to png is lost on linux,manual is right,ignore
    */
   def 'test get extended partition2' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/TestcaseVS-VPM_M3_C4', [user0], null)

      expect:
      additionalConnectionTest.compareImage()
   }

   /*
    *Test vpm applied,when condition  table has two alias tables
    */
   def 'test condition defined and binding on different table' () {
      caseName = specificationContext.currentIteration.name
      additionalConnectionTest = new AdditionalConnectionTest(caseName)
      additionalConnectionTest.executeTest('1^128^__NULL__^MultiTenantVPM/Testcase-VPM_M4_C1', [admin], null)

      expect:
      additionalConnectionTest.compareImage()
   }
}

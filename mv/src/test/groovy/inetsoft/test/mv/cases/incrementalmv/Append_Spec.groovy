package inetsoft.test.mv.cases.incrementalmv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Execution(ExecutionMode.SAME_THREAD)
class Append_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'S_TestCase1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   //need update exp for incremental mv every new year
   def 'S_TestCase7'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase7'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase8'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase8'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase9'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase9'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase10'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase10'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase11'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase11'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   @Issue('http://173.220.179.100/issues/45013')
   def 'S_TestCase12'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase12'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase13'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase13'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase14'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase14'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase15'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase15'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase16'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase16'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase18'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase18'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase19'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase19'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase20'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase20'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase21'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase21'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase22'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase22'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVproducts1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase27'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase27'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase28'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase28'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'S_TestCase29'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase29'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   @Issue('http://173.220.179.100/issues/47598')
   def 'S_TestCase30'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase30'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   //add by agile
   def 'S_TestCase_like'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/S_TestCase_like'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders2|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders2|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders2|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MinusTable|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase7'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase7'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['IntersectTable|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase8'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase8'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase9'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase9'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase10'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase10'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase11'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase11'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders2|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase12'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase12'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase13'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase13'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase14'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase14'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase15'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase15'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase16'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase16'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase17'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase17'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase18'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase18'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase19'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase19'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase20'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase20'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase22'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase22'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase23'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase23'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase24'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase24'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorderdetails1_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'M_Testcase25'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/M_Testcase25'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_Datetime'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/maxValue_Datetime'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_Double'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/maxValue_Double'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_Integer'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/maxValue_Integer'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_String'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/maxValue_String'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Datetime'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/minValue_Datetime'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Double'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/minValue_Double'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Integer'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/minValue_Integer'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_String'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/minValue_String'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'mvLastUpdatetime'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/mvLastUpdatetime'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Script6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Append/Script6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

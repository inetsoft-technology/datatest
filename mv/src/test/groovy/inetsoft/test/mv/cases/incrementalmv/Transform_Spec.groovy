package inetsoft.test.mv.cases.incrementalmv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

@Execution(ExecutionMode.SAME_THREAD)
class Transform_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'Other_TestCase1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVCustomers1|TopMV', 'MVCustomers1|TopMV', 'MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 2)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query1|TopMV', 'ASSOCIATION_orders1|TopMV', 'Query1|TopMV', 'orders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase7'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase7'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase8'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase8'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase9'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase9'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase10'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase10'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['orders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase11'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase11'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      materializedViews.createIncrementMV(false, 2)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVCustomers1|TopMV', 'ASSOCIATION_Query1|TopMV', 'MVCustomers1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase12'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase12'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase13'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase13'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase14'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase14'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      materializedViews.createIncrementMV(false, 2)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase15'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase15'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase16'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase16'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase17'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase17'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Other_TestCase18'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/Other/Other_TestCase18'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVproducts1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'full-outer join'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/full-outer join'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVproducts1|SubMV', 'MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'hidecol1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/hidecol1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'left-outer join'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/left-outer join'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVproducts1|SubMV', 'MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxrow'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/maxrow'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'merge table'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/merge table'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV', 'MVproducts1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Minus'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/Minus'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV', 'MVCustomers2|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'namegroup'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/namegroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'right-outer join'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/right-outer join'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV', 'MVorders2|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'rotate'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/rotate'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'topTable with agg'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/topTable with agg'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'topTable with aggformula'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/topTable with aggformula'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'topTable with post'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/topTable with post'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query11|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'toptable with rangecol'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/toptable with rangecol'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'topTable with ranking'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/topTable with ranking'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'union join'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/union join'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorderdetails1|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'union-distinct join'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/SubMV/union-distinct join'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV', 'MVproducts1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'dategroup'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/dategroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'distinct'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/distinct'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'formula col'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/formula col'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'hidecol2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/hidecol2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'subTable with subquery'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/subTable with subquery'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase7'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase7'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase8'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase8'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase9'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase9'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVproducts1|SubMV', 'MVCustomers1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase10'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase10'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV', 'MVorders2|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase11'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase11'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|SubMV', 'MVproducts1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase12'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase12'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Top_TestCase13'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/Top_TestCase13'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'VSCondition'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Transform/TopMV/VSCondition'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ST_Query1_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

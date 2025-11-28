package inetsoft.test.mv.cases.vpm

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

class VPM_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])
   @Shared user0 = MVTest.createPrincipal('user0', ['Everyone', 'role1'] as
         String[], ['group0'] as String[])
   @Shared user1 = MVTest.createPrincipal('user1', ['Everyone'] as
         String[], ['group0_1'] as String[])
   @Shared user2 = MVTest.createPrincipal('user2', ['Everyone', 'role0'] as
         String[], ['group1'] as String[])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'condition'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/condition'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user0)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //submv ==> change to topmv ????
   def 'condition in submv'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/condition in submv'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user0)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'group-group'() {
      given:
      def user5 = MVTest.createPrincipal('user5', ['Everyone', 'role2'] as
            String[], ['group2'] as String[])
      String asset_id = '1^128^__NULL__^vpm/group-group'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, true)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)
      mvtest.executeVS(null, null, false, false, user5)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'group-subgroup'() {
      given:
      def user6 = MVTest.createPrincipal('user6', ['Everyone'] as
            String[], ['group0_2'] as String[])
      def user7 = MVTest.createPrincipal('user7', ['Everyone'] as
            String[], ['group1_0'] as String[])
      String asset_id = '1^128^__NULL__^vpm/group-subgroup'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user6)
      mvtest.executeVS(null, null, false, false, user7)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'group-user'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/group-user'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'hiddencolumn'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/hiddencolumn'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, true, user0)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'hiddencolumn in submv'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/hiddencolumn in submv'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, true, user0)
      mvtest.executeVS(null, null, false, true, admin)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'role-group'() {
      given:
      def user4 = MVTest.createPrincipal('user4', ['Everyone'] as
            String[], ['group1'] as String[])
      def user5 = MVTest.createPrincipal('user5', ['Everyone', 'role2'] as
            String[], ['group2'] as String[])
      String asset_id = '1^128^__NULL__^vpm/role-group'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, true)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user4)
      mvtest.executeVS(null, null, false, false, user5)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'role-role'() {
      given:
      def user3 = MVTest.createPrincipal('user3', ['Everyone', 'role0'] as String[], []
            as String[])
      def user4 = MVTest.createPrincipal('user4', ['Everyone'] as String[], ['group1']
            as String[])
      def user8 = MVTest.createPrincipal('user8', ['Everyone', 'role2'] as String[], []
            as String[])

      String asset_id = '1^128^__NULL__^vpm/role-role'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user3)
      mvtest.executeVS(null, null, false, false, user4)
      mvtest.executeVS(null, null, false, false, user8)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'role-subgroup'() {
      given:
      def user6 = MVTest.createPrincipal('user6', ['Everyone'] as
            String[], ['group0_2'] as String[])
      def user7 = MVTest.createPrincipal('user7', ['Everyone'] as
            String[], ['group1_0'] as String[])
      String asset_id = '1^128^__NULL__^vpm/role-subgroup'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user6)
      mvtest.executeVS(null, null, false, false, user7)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'role-user'() {
      given:
      def user3 = MVTest.createPrincipal('user3', ['Everyone', 'role0'] as String[], []
            as String[])
      String asset_id = '1^128^__NULL__^vpm/role-user'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, admin)
      mvtest.executeVS(null, null, false, false, user2)
      mvtest.executeVS(null, null, false, false, user3)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'user-user'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-user'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, admin)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'user-aliastable1'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-aliastable1'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, true, user1)
      mvtest.executeVS(null, null, false, true, user2)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['a1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'user-aliastable2'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-aliastable2'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(true, false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['b1|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

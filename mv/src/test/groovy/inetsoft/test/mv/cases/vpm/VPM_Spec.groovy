package inetsoft.test.mv.cases.vpm

import inetsoft.test.mv.MVTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class VPM_Spec extends Specification {
   @Shared def admin
   @Shared def user0
   @Shared def user1
   @Shared def user2

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
         user0 = MVTest.createPrincipal('user0', ['Everyone', 'role1'] as String[], ['group0'] as String[])
         user1 = MVTest.createPrincipal('user1', ['Everyone'] as String[], ['group0_1'] as String[])
         user2 = MVTest.createPrincipal('user2', ['Everyone', 'role0'] as String[], ['group1'] as String[])
      }
   }

   def cleanup() {
      mvtest?.removeMV()
   }

   //order_time is a postgres "time with time zone"; it is read as the stored wall clock with and
   //without MV (#79508).
   @Issue('https://issues.inetsoft.com/issues/79508')
   def 'condition'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/condition'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user0)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user0)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //submv ==> change to topmv ????
   def 'condition in submv'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/condition in submv'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user0)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user0)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //order_time is a postgres "time with time zone"; it is read as the stored wall clock with and
   //without MV (#79508).
   @Issue('https://issues.inetsoft.com/issues/79508')
   def 'group-group'() {
      given:
      def user5 = MVTest.createPrincipal('user5', ['Everyone', 'role2'] as
            String[], ['group2'] as String[])
      String asset_id = '1^128^__NULL__^vpm/group-group'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user0)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user2)
      mvtest.executeVSWithoutMV(null, null, false, false, user5)
      mvtest.createMV(true, true)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)
      mvtest.executeVS(null, null, false, false, user5)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //order_time is a postgres "time with time zone"; it is read as the stored wall clock with and
   //without MV (#79508).
   @Issue('https://issues.inetsoft.com/issues/79508')
   def 'group-subgroup'() {
      given:
      def user6 = MVTest.createPrincipal('user6', ['Everyone'] as
            String[], ['group0_2'] as String[])
      def user7 = MVTest.createPrincipal('user7', ['Everyone'] as
            String[], ['group1_0'] as String[])
      String asset_id = '1^128^__NULL__^vpm/group-subgroup'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user6)
      mvtest.executeVSWithoutMV(null, null, false, false, user7)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user6)
      mvtest.executeVS(null, null, false, false, user7)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // "Bypass VPM": createMV(false) analyzes with bypass (MaterializedViewResource), so the MV is
   // built without VPM and used without it (ViewsheetSandbox.java:249, the VPM check of
   // MVManager.isVPMCompatible), and every user sees all rows through the MV. Without MV the VPM
   // of vpm/group-user keeps 11 of the 700 rows for user0 and user1 (the golden files of
   // 'group-user'). Checked on the MV run itself, so also in golden mode.
   def 'group-user, MV built with bypass VPM: every user sees all rows through the MV'() {
      given:
      mvtest = new MVTest('1^128^__NULL__^vpm/group-user')

      when: 'no MV yet: the VPM applies'
      def noMV = [user0: lines(user0), admin: lines(admin)]
      mvtest.takeMVHits()

      and: 'an MV built with bypass'
      mvtest.createMV(false)
      def defs = MVTest.getMVDefInfo()
      def withMV = [user0, user1, user2, admin].collectEntries { user ->
         def rows = lines(user)
         [(user.getName()): [rows, mvtest.takeMVHits()]]
      }

      then:
      verifyAll {
         noMV.user0.size() == 11
         noMV.admin.size() == 700
         defs == ['orders1|TopMV']
         withMV.values().every { it[1] == ['orders1|TopMV'] }
         withMV.values().every { it[0] == noMV.admin }
      }
   }

   /**
    * The data rows (without the size and header lines) of Query1 run by the user. The export is
    * deleted after it is read: the folder is the one of 'group-user', whose compareData counts
    * its files.
    */
   private List<String> lines(def user) {
      mvtest.executeVS(null, null, false, false, user)
      File file = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                           'exportedData/vpm/group-user/' + user.getIdentityID().getName() + '_Query1__MV.txt')
      List<String> rows = file.readLines('UTF-8').drop(2).findAll { it }
      file.delete()
      return rows
   }

   //order_time is a postgres "time with time zone"; it is read as the stored wall clock with and
   //without MV (#79508).
   @Issue('https://issues.inetsoft.com/issues/79508')
   def 'group-user'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/group-user'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user0)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user2)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['orders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'hiddencolumn'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/hiddencolumn'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, true, user0)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, true, user0)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'hiddencolumn in submv'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/hiddencolumn in submv'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, true, user0)
      mvtest.executeVSWithoutMV(null, null, false, true, admin)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, true, user0)
      mvtest.executeVS(null, null, false, true, admin)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['products2|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'role-group'() {
      given:
      def user4 = MVTest.createPrincipal('user4', ['Everyone'] as
            String[], ['group1'] as String[])
      def user5 = MVTest.createPrincipal('user5', ['Everyone', 'role2'] as
            String[], ['group2'] as String[])
      String asset_id = '1^128^__NULL__^vpm/role-group'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user4)
      mvtest.executeVSWithoutMV(null, null, false, false, user5)
      mvtest.createMV(true, true)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user4)
      mvtest.executeVS(null, null, false, false, user5)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
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

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user3)
      mvtest.executeVSWithoutMV(null, null, false, false, user4)
      mvtest.executeVSWithoutMV(null, null, false, false, user8)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user3)
      mvtest.executeVS(null, null, false, false, user4)
      mvtest.executeVS(null, null, false, false, user8)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'role-subgroup'() {
      given:
      def user6 = MVTest.createPrincipal('user6', ['Everyone'] as
            String[], ['group0_2'] as String[])
      def user7 = MVTest.createPrincipal('user7', ['Everyone'] as
            String[], ['group1_0'] as String[])
      String asset_id = '1^128^__NULL__^vpm/role-subgroup'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user6)
      mvtest.executeVSWithoutMV(null, null, false, false, user7)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user6)
      mvtest.executeVS(null, null, false, false, user7)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'role-user'() {
      given:
      def user3 = MVTest.createPrincipal('user3', ['Everyone', 'role0'] as String[], []
            as String[])
      String asset_id = '1^128^__NULL__^vpm/role-user'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, admin)
      mvtest.executeVSWithoutMV(null, null, false, false, user2)
      mvtest.executeVSWithoutMV(null, null, false, false, user3)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, admin)
      mvtest.executeVS(null, null, false, false, user2)
      mvtest.executeVS(null, null, false, false, user3)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['categories1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'user-user'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-user'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, admin)
      mvtest.executeVSWithoutMV(null, null, false, false, user0)
      mvtest.executeVSWithoutMV(null, null, false, false, user2)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, admin)
      mvtest.executeVS(null, null, false, false, user0)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['products1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'user-aliastable1'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-aliastable1'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, true, user1)
      mvtest.executeVSWithoutMV(null, null, false, true, user2)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, true, user1)
      mvtest.executeVS(null, null, false, true, user2)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['a1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'user-aliastable2'() {
      given:
      String asset_id = '1^128^__NULL__^vpm/user-aliastable2'

      mvtest = new MVTest(asset_id)
      mvtest.executeVSWithoutMV(null, null, false, false, user1)
      mvtest.executeVSWithoutMV(null, null, false, false, user2)
      mvtest.createMV(true, false)
      mvtest.executeVS(null, null, false, false, user1)
      mvtest.executeVS(null, null, false, false, user2)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['b1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   MVTest mvtest
}

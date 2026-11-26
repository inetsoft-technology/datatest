package inetsoft.test.mv.cases.topmv

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
class TopMV_TableStructures_Spec extends Specification {
   @Shared def admin

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }

   def cleanup() {
      mvtest?.removeMV()
   }

   def 'child-aggregate-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child-aggregate-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'child-plain-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child-plain-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('https://issues.inetsoft.com/issues/37226')
   def 'parent-aggregate-distinct-selection col not public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-distinct-selection col not public'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'parent-aggregate-distinct-selection col public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-distinct-selection col public'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('https://issues.inetsoft.com/issues/37244')
   def 'parent-aggregate-maxrow-selection col not public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-maxrow-selection col not public'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'parent-plain-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-plain-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'composite1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'composite2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //Query3 joins RETURNS1 twice. With a selection on RETURNS1.REASON (bk1, bk2, bk4, bk5) the MV used to
   //return more rows than without MV (#80445, fixed). The expected data is the result without MV.
   @Issue('https://issues.inetsoft.com/issues/80445')
   def 'composite3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite3'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6'] as String[], false, true)
      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'crossjoin'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/crossjoin'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CATEGORIES1|TopMV', 'CATEGORIES1|TopMV', 'Query1|TopMV'])
         mvtest.getMVHitInfo().containsAll(['CATEGORIES1|TopMV', 'Query1|TopMV', 'ASSOCIATION_CATEGORIES1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'date range'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/date range'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'expression'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/expression'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'intersect'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/intersect'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'left outer join-select left'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/left outer join-select left'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'right outer join-select right'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/right outer join-select right'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'merge-join'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-join'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'unmerge-join'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-join'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query4_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'minus'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/minus'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'union'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/union'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'threetables'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/threetables'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|SubMV', 'RETURNS1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'name group'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/name group'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'numeric range'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/numeric range'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'rename'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/rename'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ORDERS1|TopMV', 'Query1_O|TopMV'])
         mvtest.getMVHitInfo().containsAll(['ORDERS1|TopMV', 'Query1_O|TopMV', 'ASSOCIATION_ORDERS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'selection on aggregate second col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on aggregate second col'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'selection on crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on crosstab'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'selection on hidden group col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on hidden group col'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'selection on other col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on other col'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // Query2 is the rotated table of Query1, an embedded table grouped by Year(ORDER_DATE),
   // DISCOUNT, PAID. A rotated column holds values of different types; its MV stored them as
   // text (numbers without their type, dates with ".0", string order). No MV is built on a
   // rotated table now, only on the table before the rotation (#82323). rotate1 is then the
   // same as without MV in every component. In rotate2 the table is the same; its selection list
   // still differs in the order and the state of the values (#82323 back in Feedback), so only
   // the table is compared. No golden files: the checks are on the MV run.
   @Issue('https://issues.inetsoft.com/issues/82323')
   def '#name: no MV on the rotated table'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/' + name
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      List<String> table = mvtest.exportedLines('admin_Query1')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().findAll { it.contains('Query2|') } == []
         mvtest.getMVHitInfo().contains('Query1|SubMV')
         table[0] == size
         table.contains(row)
         !table.any { it.contains(' 00:00:00.0]') }
         mvtest.compareWithoutMV(compared)
      }

      // the size and a row of the table as without MV; no date with ".0", which the dates had when
      // the MV of the rotated table held them as text
      where:
      name      | compared         | size                                       | row
      'rotate1' | null             | 'The table size(row x col) is: (5 x 5)'    | '[CUSTOMER_ID][607][498][2881][153]'
      'rotate2' | ['admin_Query1'] | 'The table size(row x col) is: (5 x 36)'   | '[EMPLOYEE_ID][76][1][277][43][4][2][3][8][2][4][5][60][318][35][4][5][4][5][4][397][17][4][2][24][4][3][15][20][263][58][4][11][7][6][4]'
   }

   MVTest mvtest
}

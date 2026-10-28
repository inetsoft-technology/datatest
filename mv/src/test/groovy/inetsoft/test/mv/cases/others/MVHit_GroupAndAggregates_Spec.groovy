package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
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
class MVHit_GroupAndAggregates_Spec extends Specification {
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

   def 'chart1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

   }

   def 'chart2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart3'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart4'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart5'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart5'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart6'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart6'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart7'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart7'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

   }

   def 'chart8'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart8'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart9'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart9'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart10'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart10'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart11'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart11'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart12'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart12'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                            'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

   }

   def 'chart13'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart13'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|TopMV', 'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('http://173.220.179.100/issues/49867')
   def 'discrete_chart'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/discrete_chart'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['OrdersAndReturns|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart-dategroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-dategroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query3_O|TopMV', 'Query3_O|TopMV', 'Query3_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart-namegroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-namegroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'chart-normalgroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-normalgroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'crosstab1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab1'
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

   def 'crosstab2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'crosstab2_1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab2_1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'crosstab3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab3'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'freehand1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand1'
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

   def 'freehand2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('http://173.220.179.100/issues/43498')
   def 'freehand3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand3'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_RETURNS1|TopMV', 'ST_Query2_O|SubMV', 'RETURNS1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'freehand4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand4'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: with date level None the MV keeps only the time of day
   //(1970-01-01 00:02:07), without MV the date is kept (2018-12-20 00:02:07).
   def 'date level is none'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/date level is none'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('EM2_M_GandA|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'name group on first aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/name group on first aggregate'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, false)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('OrdersAndReturns|TopMV')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: the range slider labels are formatted differently with and without MV
   //(e.g. 1000 / 1,000, 01:01 / 1:01 AM, 2002 Jan / 2002).
   def 'change from'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/change from'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('l1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'value of'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/value of'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('ORDERS1|TopMV')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'moving average'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/moving average'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('lm1|TopMV')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //Expected data was generated from the MV result, which groups 23:59:59+07:36:36 into hour 00
   //instead of 23 (#79508). Update it after the fix.
   //No compareWithoutMV: Average keeps the integer type with MV (formula.avg.keep.type), so 73 with MV is
   //73.5 without MV (Bug #54404).
   @Issue('https://issues.inetsoft.com/issues/79508')
   def 'running total'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/running total'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('lm1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'percent of'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/percent of'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll('l1|TopMV')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   MVTest mvtest
}

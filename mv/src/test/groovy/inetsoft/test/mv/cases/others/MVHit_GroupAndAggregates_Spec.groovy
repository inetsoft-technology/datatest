package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class MVHit_GroupAndAggregates_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'chart1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }

   }

   def 'chart2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                            'Query3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                           'Query3|TopMV', 'Query3|TopMV',
                                           'Query3|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                           'Query3|TopMV', 'Query3|TopMV',
                                           'Query3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart5'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart6'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart6'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart7'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart7'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }

   }

   def 'chart8'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart8'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart9'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart9'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart10'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart10'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart11'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart11'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart12'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart12'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }

   }

   def 'chart13'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart13'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/49867')
   def 'discrete_chart'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/discrete_chart'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['OrdersAndReturns|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-dategroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-dategroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV', 'Query3_O|TopMV', 'Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-namegroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-namegroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-normalgroup-selectcolnotpublic'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-normalgroup-selectcolnotpublic'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'crosstab1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'crosstab2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'crosstab2_1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab2_1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'crosstab3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/crosstab3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'freehand1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'freehand2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1' ] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/43498')
   def 'freehand3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_RETURNS1|TopMV', 'ST_Query2_O|SubMV', 'RETURNS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'freehand4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/freehand4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'date level is none'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/date level is none'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('EM2_M_GandA|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'name group on first aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/name group on first aggregate'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('OrdersAndReturns|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'change from'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/change from'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('l1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'value of'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/value of'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('ORDERS1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'moving average'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/moving average'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('lm1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'running total'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/running total'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('lm1|TopMV')
         mvtest.compareData(false)
      }
   }

   def 'percent of'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/percent of'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll('l1|TopMV')
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

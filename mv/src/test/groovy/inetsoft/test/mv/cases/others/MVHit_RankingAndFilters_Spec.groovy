package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

@Execution(ExecutionMode.SAME_THREAD)
class MVHit_RankingAndFilters_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'chart-dategroup'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-dategroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
      }

   }

   @Issue('http://173.220.179.100/issues/38616')
   def 'chart-namegroup'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-namegroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV',
                                           'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-normalgroup'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-normalgroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers|TopMV', 'customers|TopMV',
                                           'customers|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-dategroup-noneaggr'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-dategroup-noneaggr'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }

   }

   def 'chart-namegroup-noneaggr'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-namegroup-noneaggr'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-normalgroup-noneaggr'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-normalgroup-noneaggr'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-dategroup2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-dategroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV', 'Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-normalgroup2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-normalgroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom', 'bk_brush'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-namegroup2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart/chart-namegroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_zoom'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'comparsionfilter-year'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/comparsionfilter-year'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV', 'Query3|TopMV',
                                           'Query3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'comparsionfilter-month'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/comparsionfilter-month'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)


      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS|TopMV', 'ORDERS|TopMV',
                                           'ORDERS|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38791')
   def 'comparsionfilter-week'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/comparsionfilter-week'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV', 'Order Model|TopMV',
                                           'Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'comparsionfilter-day'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/comparsionfilter-day'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|TopMV', 'query1|TopMV',
                                           'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection list'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection list'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection tree'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection tree'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2', 'bk_select3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection tree2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection tree2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Final|TopMV', 'Final|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/42260')
   def 'parent-child selection tree'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/parent-child selection tree'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2', 'bk_select3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'rangeslider'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/rangeslider'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'rangeslider-log'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/rangeslider-log'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'rangeslider-compositevalue'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/rangeslider-compositevalue'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ASSOCIATION_Query1|TopMV', 'ORDERS1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'calendar-single'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/calendar-single'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2', 'bk_select3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'calendar-range'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/calendar-range'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection association'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection association'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'selection association2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection association2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'selection association3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection association3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select1', 'bk_select2', 'bk_select3'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   @Issue('http://173.220.179.100/issues/52677')
   def 'selection association4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection association4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'filterwithparameter-date'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/filterwithparameter-date'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      def params = ['startdt': ['2018-07-01 12:00:00'] as String[], 'enddt':
            ['2019-07-01 12:00:00'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'filterwithparameter-double'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/filterwithparameter-double'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      def params = ['bol': ['true'] as String[], 'dis': ['0', '0.01', '0.05'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'filterwithparameter-string'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/filterwithparameter-string'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      def params = ['sts': ['MA'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'otherassembly-plaintable'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-plaintable'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'otherassembly-selectgroupcol'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-selectgroupcol'
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

   def 'otherassembly-selectaggrcol'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-selectaggrcol'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['All Sales1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'otherassembly-selectothercol'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-selectothercol'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38887')
   def 'otherassembly-crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['crosstab|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/55927')
   def 'otherassembly-text'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/otherassembly-text'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Ignore
   def 'chart-time type'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart-time type'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-convert to measure'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart-convert to measure'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'chart-convert to dimension'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/chart-convert to dimension'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection col from crosstab header'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection col from crosstab header'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection on measure col'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection on measure col'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'col as dimension and measure'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/col as dimension and measure'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate result is not number'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/aggregate result is not number'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Ignore
   def 'input5'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/input5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'column as aggregate and group in bivariate aggregation'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/column as aggregate and group in bivariate aggregation'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query11|TopMV', 'Query11|TopMV', 'Query3|TopMV', 'Query7|TopMV', 'Query8|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/54918')
   def 'incremental adhoc filter'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/incremental adhoc filter'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CUSTOMERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   @Issue('http://173.220.179.100/issues/60645')
   @Issue('http://173.220.179.100/issues/60746')
   def 'adhoc filter and drill filter'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/adhoc filter and drill filter'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS1|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'CUSTOMERS1|TopMV', 'Query1|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

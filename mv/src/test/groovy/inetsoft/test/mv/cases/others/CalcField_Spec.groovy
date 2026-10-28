package inetsoft.test.mv.cases.others

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
class CalcField_Spec extends Specification {
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

   def 'NotUsed_Assembly'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotUsed_Assembly'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk_select'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Output'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Output'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Chart'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Crosstab'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Table'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_SelectionTree'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_SelectionTree'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('http://173.220.179.100/issues/43410')
   def 'Public_Calendar'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Calendar'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_RangeSlider'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_RangeSlider'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_Text'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Text'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         // TODO: only query1|TopMV is observed. The text shows a plain value, so the MV it reads (the
         // sub mv is picked while the query runs) can not be seen; confirm and switch to getMVHitInfo()
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_Chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Chart'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_Crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Crosstab'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Table'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: the selection tree shows a year as 2003-01-01 00:00:00 with MV
   //and as 2003 without MV.
   def 'NotPublic_SelectionTree'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_SelectionTree'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Calendar'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Calendar'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_RangeSlider'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_RangeSlider'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Brush1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Brush1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Brush2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Brush2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_NameGroup1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NameGroup1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_NameGroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NameGroup2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_NormalGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NormalGroup'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Zoom1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Zoom1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Public_Zoom2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Zoom2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_NameGroup1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NameGroup1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_NameGroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NameGroup2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_NormalGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NormalGroup'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'NotPublic_Zoom1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Zoom1'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'Aggregate_Public_Model'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Model'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: Average keeps the integer type with MV (formula.avg.keep.type),
   //so 17 with MV is 17.5 without MV (Bug #54404).
   def 'Aggregate_Public_Query'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Query'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query|TopMV', 'query|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Public_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Table'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: without MV the exported chart data also has the aggregates the calc
   //fields are computed from; the shared columns are the same.
   def 'Aggregate_Public_Brush1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Brush1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //No compareWithoutMV: without MV the exported chart data also has the aggregates the calc
   //fields are computed from; the shared columns are the same.
   def 'Aggregate_NotPublic_NameGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_NotPublic_NameGroup'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_zoom', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //No compareWithoutMV: without MV the exported chart data also has the aggregates the calc
   //fields are computed from; the shared columns are the same.
   @Issue('http://173.220.179.100/issues/48283')
   def 'chart_NthLargest for calcfield'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_NthLargest for calcfield'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['customers|TopMV'])
         mvtest.compareData(false)
      }
   }

   //No compareWithoutMV: without MV the exported chart data also has the aggregates the calc
   //fields are computed from; the shared columns are the same.
   @Issue('http://173.220.179.100/issues/49832')
   def 'chart_namegroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_namegroup'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'b1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Order Model|TopMV', 'Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/49777')
   def 'chart_namegroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_namegroup2'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)
      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_OrdersAndReturns|SubMV', 'OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('http://173.220.179.100/issues/50262')
   def 'calcfield on group other'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on group other'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['OrdersAndReturns|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   //No compareWithoutMV: without MV the exported chart data also has the aggregates the calc
   //fields are computed from; the shared columns are the same.
   @Issue('http://173.220.179.100/issues/51346')
   def 'multiple calc on chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/multiple calc on chart'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_OrdersAndReturns|SubMV', 'OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'calcfield on date legend'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on date legend'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Sales|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   def 'calcfield on crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on crosstab'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_OrdersAndReturns|SubMV', 'OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   @Issue('http://173.220.179.100/issues/75659')
   /**
    * The live query and the materialized view legitimately iterate the rows in different orders,
    * so they can return different (but equally valid) values for the tied rows.
    * Making `FIRST`/`LAST` non-combinable would force detail materialization to make the two paths agree,
    * but the performance cost is not justified.
    */
   //No compareWithoutMV: CalcField5 is First([Product:Total], [Customer:Address]); the first
   //address has several rows, and the one used depends on the row order. The range slider
   //labels and the exported chart data also differ in representation.
   def 'Aggregate_ForDate'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_ForDate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Expression'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Expression'
      mvtest = new MVTest(asset_id)

      mvtest.executeVSWithoutMV(null, ['(Home)', 'bk_select'] as String[], false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['lm1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   MVTest mvtest
}

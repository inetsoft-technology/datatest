package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class CalcField_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'NotUsed_Assembly'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotUsed_Assembly'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Output'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Output'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Chart'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Table'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_SelectionTree'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_SelectionTree'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/43410')
   def 'Public_Calendar'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Calendar'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_RangeSlider'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_RangeSlider'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Text'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Text'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Chart'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Table'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_SelectionTree'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_SelectionTree'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
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
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_RangeSlider'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_RangeSlider'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Brush1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Brush1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_view|TopMV', 'view|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Brush2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Brush2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_NameGroup1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NameGroup1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_NameGroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NameGroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_NormalGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_NormalGroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Zoom1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Zoom1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Public_Zoom2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Public_Zoom2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_NameGroup1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NameGroup1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_NameGroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NameGroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_NormalGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_NormalGroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'NotPublic_Zoom1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/NotPublic_Zoom1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Public_Model'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Model'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['view_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Public_Query'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Query'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Public_Table'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Table'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Public_Brush1'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Public_Brush1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select', 'bk_zoom'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_query1|TopMV', 'Query2|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_NotPublic_NameGroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_NotPublic_NameGroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_zoom', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query1|TopMV', 'query1|SubMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48283')
   def 'chart_NthLargest for calcfield'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_NthLargest for calcfield'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/49832')
   def 'chart_namegroup'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_namegroup'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'b1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/49777')
   def 'chart_namegroup2'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/chart_namegroup2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)
      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/50262')
   def 'calcfield on group other'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on group other'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['OrdersAndReturns|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/51346')
   def 'multiple calc on chart'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/multiple calc on chart'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'calcfield on date legend'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on date legend'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Sales|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'calcfield on crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/calcfield on crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['OrdersAndReturns_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_ForDate'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_ForDate'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Order Model|SubMV', 'Order Model_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Aggregate_Expression'() {
      given:
      String asset_id = '1^128^__NULL__^CalcField/Aggregate_Expression'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_lm1|SubMV', 'lm1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

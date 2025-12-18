package inetsoft.test.mv.cases.topmv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

class TopMV_Others_Spec extends Specification{
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'selection on crosstab2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on crosstab2'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate table as subquery'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/aggregate table as subquery'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDER_DETAILS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection nested'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection nested'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection nested1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection nested1'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection nested2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection nested2'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ORDERS1|TopMV', 'ST_Query2_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'child has maxrow'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child has maxrow'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'base is embedded setted name group'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/base is embedded setted name group'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'select on aggregate col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/select on aggregate col'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable col is not public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/variable col is not public'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'vs assembly-starting with'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/vs assembly-starting with'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/44221')
   def 'v-contains'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-contains'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = ['product_name': ['a'] as String[]]
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['PRODUCTS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-starting with'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-starting with'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = ['product_name': ['F'] as String[]]
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['PRODUCTS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'vs assembly-contains'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/vs assembly-contains'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/47853')
   def 'vs_donut'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/vs_donut'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48175')
   @Retry(count = 3)
   def 'chart_discrete'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/chart_discrete'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, false, admin)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48293')
   def 'ganttchart'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/ganttchart'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48293')
   def 'treechart'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/treechart'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48939')
   def 'parameter prompts is false'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parameter prompts is false'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Actions Raw|TopMV'])
         mvtest.compareData(false)
      }
   }

   //add case for interval chart
   def 'interval chart'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/interval chart'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null,['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV','Query2|TopMV','ORDERS1|TopMV','CUSTOMERS1|TopMV','ASSOCIATION_Query1|TopMV','ASSOCIATION_CUSTOMERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //add value as color on chart
   def 'value as color'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/value as color'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['upload|TopMV'])
         mvtest.compareData(false)
      }
   }

   //Bug #53475, chart can't display for 'bk1', display warning info
   @Ignore
   def 'date range and number range'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/date range and number range'
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

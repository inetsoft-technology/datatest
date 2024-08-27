package inetsoft.test.mv.cases.topmv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class TopMV_Conditions_Spec extends Specification{
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'value condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/value condition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'field condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/field condition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'expression condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/expression condition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'subquery condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/subquery condition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'in range condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/in range condition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'postcondition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/postcondition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'ranking condition'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/rankingcondition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post1'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('employee_id', ['10'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Aggregate_ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post-aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post-aggregate'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post-group'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post-group'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post-hide'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post-hide'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post-plain'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post-plain'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-post-plain-hide'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-post-plain-hide'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('300', ['300'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre1'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('order_id', ['12010'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('order_id', ['12010'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Detail_ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre3'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('dis', ['0.1'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre4'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre4'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('dis', ['0.1'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre5'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre5'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('order_id', ['12010'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre6'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre6'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('order_id', ['12010'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre7'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre7'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('dis', ['0.1'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre8'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre8'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('dis', ['0.1'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-pre9'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-pre9'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('dis', ['0.1'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/23220')
   def 'v-ranking1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking1'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['3'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV','Ranking1_O|SubMV','Ranking3_O|SubMV',
                                           'Ranking4_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['3'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking2-hide'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking2-hide'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['3'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking3'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['3'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking4'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking4'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['10'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking5'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking5'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['3'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'v-ranking6'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/v-ranking6'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('top', ['4'] as String[])
      params.put('top2', ['2'] as String[])
      mvtest.executeVS(params, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/8268')
   def 'variable expression'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/variable expression'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('var2', ['CA', 'CO'] as String[])
      mvtest.executeVS(params,  ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS1|TopMV', 'CUSTOMERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/60758')
   @Ignore
   def 'variable in subquery'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/variable in subquery'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('cid', ['10'] as String[])
      mvtest.executeVS(params,  ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

package inetsoft.test.mv.cases.submv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.IgnoreRest

class SubMV_Others_Spec extends Specification{
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'mergejoin2'() {
      String asset_id = '1^128^__NULL__^submv/mergejoin2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'postcondition'() {
      String asset_id = '1^128^__NULL__^submv/postcondition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate col is expression2'() {
      String asset_id = '1^128^__NULL__^submv/aggregate col is expression2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate second col is expression2'() {
      String asset_id = '1^128^__NULL__^submv/aggregate second col is expression2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate formula'() {
      String asset_id = '1^128^__NULL__^submv/aggregate formula'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'hidecolumn2'() {
      String asset_id = '1^128^__NULL__^submv/hidecolumn2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate-twocolumnsformula'() {
      String asset_id = '1^128^__NULL__^submv/aggregate-twocolumnsformula'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'calc field selection in child'() {
      String asset_id = '1^128^__NULL__^submv/calc field selection in child'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2_O|TopMV',
            'ASSOCIATION_query1|TopMV', 'Query2_O|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'child has percentage'() {
      String asset_id = '1^128^__NULL__^submv/child has percentage'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'crosstab'() {
      String asset_id = '1^128^__NULL__^submv/crosstab'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|SubMV',
                                           'ORDERS1|SubMV', 'ORDERS1|SubMV',
                                           'ORDERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'full outer join'() {
      String asset_id = '1^128^__NULL__^submv/full outer join'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['categories1|SubMV', 'customers1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'hideintop'() {
      String asset_id = '1^128^__NULL__^submv/hideintop'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ST_Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'hide-notgroupnotaggregate'() {
      String asset_id = '1^128^__NULL__^submv/hide-notgroupnotaggregate'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'intersect-selection on right node'() {
      String asset_id = '1^128^__NULL__^submv/intersect-selection on right node'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['RETURNS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'join col as aggregate col 1'() {
      String asset_id = '1^128^__NULL__^submv/join col as aggregate col 1'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'join col as aggregate col 2'() {
      String asset_id = '1^128^__NULL__^submv/join col as aggregate col 2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'join col as aggregate col 3'() {
      String asset_id = '1^128^__NULL__^submv/join col as aggregate col 3'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'join col as aggregate col 4'() {
      String asset_id = '1^128^__NULL__^submv/join col as aggregate col 4'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'join-selection col not public'() {
      String asset_id = '1^128^__NULL__^submv/join-selection col not public'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'left outer join-select both'() {
      String asset_id = '1^128^__NULL__^submv/left outer join-select both'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'left outer join-select right'() {
      String asset_id = '1^128^__NULL__^submv/left outer join-select right'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'minus-selection on right node'() {
      String asset_id = '1^128^__NULL__^submv/minus-selection on right node'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'RETURNS1|SubMV'])
         mvtest.compareData(false)
      }
   }
   //Bug #38203
   def 'namegroup-select on group col'() {
      String asset_id = '1^128^__NULL__^submv/namegroup-select on group col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'namegroup-select on other col'() {
      String asset_id = '1^128^__NULL__^submv/namegroup-select on other col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   //bug #54403, 建mv之后数据细微差别
   def 'percentage-select on group col'() {
      String asset_id = '1^128^__NULL__^submv/percentage-select on group col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   //bug #54403, 建mv之后数据细微差别
   def 'percentage-select on other col'() {
      String asset_id = '1^128^__NULL__^submv/percentage-select on other col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'ranking'() {
      String asset_id = '1^128^__NULL__^submv/ranking'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'ranking-aggregate1'() {
      String asset_id = '1^128^__NULL__^submv/ranking-aggregate1'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'ranking-aggregate2'() {
      String asset_id = '1^128^__NULL__^submv/ranking-aggregate2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   //have sort issue: sort looks changed
   def 'right outer join-select both'() {
      String asset_id = '1^128^__NULL__^submv/right outer join-select both'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   //have sort issue: sort looks changed
   def 'right outer join-select left'() {
      String asset_id = '1^128^__NULL__^submv/right outer join-select left'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'rotate'() {
      String asset_id = '1^128^__NULL__^submv/rotate'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'select on aggregate used col'() {
      String asset_id = '1^128^__NULL__^submv/select on aggregate used col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   //Bug#37854
   @Ignore
   def 'top-aggregate-maxrow'() {
      String asset_id = '1^128^__NULL__^submv/top-aggregate-maxrow'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'top has percentage'() {
      String asset_id = '1^128^__NULL__^submv/top has percentage'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['PRODUCTS1|SubMV', 'ORDER_DETAILS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'top-plain-maxrow'() {
      String asset_id = '1^128^__NULL__^submv/top-plain-maxrow'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query21|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unionall2'() {
      String asset_id = '1^128^__NULL__^submv/unionall2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'union-select in both childs'() {
      String asset_id = '1^128^__NULL__^submv/union-select in both childs'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'union-select in one child'() {
      String asset_id = '1^128^__NULL__^submv/union-select in one child'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable col is hidden'() {
      String asset_id = '1^128^__NULL__^submv/variable col is hidden'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Detail_ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable col is name grouped'() {
      String asset_id = '1^128^__NULL__^submv/variable col is name grouped'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = ['aaa': ['12001', '12002', '12003'] as String[]]
      mvtest.executeVS(params, null, false, false, admin)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable condition on aggregate col'() {
      String asset_id = '1^128^__NULL__^submv/variable condition on aggregate col'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Detail_ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   //Bug #38284
   def 'variable condition on aggregate col 2'() {
      String asset_id = '1^128^__NULL__^submv/variable condition on aggregate col 2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Detail_Copy ofORDERS11_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   //Bug #48338 add case by agile on v13.4
   def 'unionsub'() {
      String asset_id = '1^128^__NULL__^submv/unionsub'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query1|TopMV', 'Query1|TopMV', 'Query1|SubMV', 'Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   //add by agile on v13.4
   def 'parameter in calc field'() {
      String asset_id = '1^128^__NULL__^submv/parameter in calc field'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

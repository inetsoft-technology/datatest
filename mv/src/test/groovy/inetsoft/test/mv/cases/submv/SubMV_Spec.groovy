package inetsoft.test.mv.cases.submv

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

class SubMV_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   //average 存在数据精度问题, bug #54404
   def 'aggbase'() {
      given:
      String asset_id = '1^128^__NULL__^submv/aggbase'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV',
                                           'Query1|SubMV', 'Query1|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggbase2'() {
      given:
      String asset_id = '1^128^__NULL__^submv/aggbase2'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll((['ORDERS1|SubMV', 'ORDERS1|SubMV']))
         mvtest.compareData(false)
      }
   }

   def 'aggregate col is expression'() {
      given:
      String asset_id = '1^128^__NULL__^submv/aggregate col is expression'
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

   def 'aggregate second col is expression'() {
      given:
      String asset_id = '1^128^__NULL__^submv/aggregate second col is expression'
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

   def 'contact'() {
      given:
      String asset_id = '1^128^__NULL__^submv/contact'
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
   //存在数据精度变化问题
   def 'crossjoin'() {
      given:
      String asset_id = '1^128^__NULL__^submv/crossjoin'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['PRODUCTS1|SubMV', 'ORDER_DETAILS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'dategroup'() {
      given:
      String asset_id = '1^128^__NULL__^submv/dategroup'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS2|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'distinct'() {
      given:
      String asset_id = '1^128^__NULL__^submv/distinct'
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

   def 'group+group'() {
      given:
      String asset_id = '1^128^__NULL__^submv/group+group'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6', 'bk7'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
      }
   }
   //pop up npe
   def 'group+plain'() {
      given:
      String asset_id = '1^128^__NULL__^submv/group+plain'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CUSTOMERS1|SubMV', 'ST_Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'hideaggcolumn'() {
      given:
      String asset_id = '1^128^__NULL__^submv/hideaggcolumn'
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

   def 'hidecolumn'() {
      given:
      String asset_id = '1^128^__NULL__^submv/hidecolumn'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)
      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'hidegroupcolumn'() {
      given:
      String asset_id = '1^128^__NULL__^submv/hidegroupcolumn'
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

   def 'hideothercolumn'() {
      given:
      String asset_id = '1^128^__NULL__^submv/hideothercolumn'
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

   def 'intersect'() {
      given:
      String asset_id = '1^128^__NULL__^submv/intersect'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'jsformula'() {
      given:
      String asset_id = '1^128^__NULL__^submv/jsformula'
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

   def 'mergejoin'() {
      given:
      String asset_id = '1^128^__NULL__^submv/mergejoin'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-nthlargest'() {
      given:
      String asset_id = '1^128^__NULL__^submv/merge-nthlargest'
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

   def 'merge-nthmostfrequent'() {
      given:
      String asset_id = '1^128^__NULL__^submv/merge-nthmostfrequent'
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

   def 'merge-nthsmallest'() {
      given:
      String asset_id = '1^128^__NULL__^submv/merge-nthsmallest'
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

   def 'merge-product'() {
      given:
      String asset_id = '1^128^__NULL__^submv/merge-product'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-pthpercentile'() {
      given:
      String asset_id = '1^128^__NULL__^submv/merge-pthpercentile'
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

   def 'minus'() {
      given:
      String asset_id = '1^128^__NULL__^submv/minus'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query2|SubMV'])
         mvtest.compareData(false)
      }
   }
   //average存在数据精度问题
   def 'namegroup'() {
      given:
      String asset_id = '1^128^__NULL__^submv/namegroup'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDER_DETAILS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'outerjoin'() {
      given:
      String asset_id = '1^128^__NULL__^submv/outerjoin'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['RETURNS1|SubMV', 'ORDER_DETAILS1|SubMV',
                                           'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'postcondition_expression'() {
      given:
      String asset_id = '1^128^__NULL__^submv/postcondition_expression'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS2|SubMV',
                                           'ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'postcondition_variable'() {
      given:
      String asset_id = '1^128^__NULL__^submv/postcondition_variable'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)
      def params = new HashMap<String, String[]>()
      params.put('Quantity', ['10000'] as String[])

      mvtest.executeVS(params, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'precondition_field'() {
      given:
      String asset_id = '1^128^__NULL__^submv/precondition_field'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   //Bug #37384, fixed in version13.2
   def 'precondition-subquery'() {
      given:
      String asset_id = '1^128^__NULL__^submv/precondition-subquery'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDER_DETAILS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'rangecolumn'() {
      given:
      String asset_id = '1^128^__NULL__^submv/rangecolumn'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ST_ST_Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'rankingcondition'() {
      given:
      String asset_id = '1^128^__NULL__^submv/rankingcondition'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|SubMV', 'Query1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'sqlformula'() {
      given:
      String asset_id = '1^128^__NULL__^submv/sqlformula'
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

   def 'top_crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^submv/top_crosstab'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDER_DETAILS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'twocolumnsformula'() {
      given:
      String asset_id = '1^128^__NULL__^submv/twocolumnsformula'
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

   def 'union'() {
      given:
      String asset_id = '1^128^__NULL__^submv/union'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDER_DETAILS1|SubMV', 'ORDER_DETAILS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unionall'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unionall'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ST_Query1|SubMV', 'ST_Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-nthlargest'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unmerge-nthlargest'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-nthmostfrequent'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unmerge-nthmostfrequent'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)

      mvtest = new MVTest(asset_id)

      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-nthsmallest'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unmerge-nthsmallest'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-product'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unmerge-product'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-pthpercentile'() {
      given:
      String asset_id = '1^128^__NULL__^submv/unmerge-pthpercentile'
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest = new MVTest(asset_id)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|SubMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

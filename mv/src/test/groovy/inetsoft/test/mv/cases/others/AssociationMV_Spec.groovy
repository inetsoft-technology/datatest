package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class AssociationMV_Spec extends Specification {
   @Shared admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
         String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'TestCase_CalcField1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CalcField1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS|TopMV', 'ORDERS|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CalcField2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CalcField2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_l1|TopMV', 'l1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompatileSelection'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompatileSelection'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['C_CUSTOMERS1_CUSTOMERS2|TopMV', 'CUSTOMERS1|SubMV', 'CUSTOMERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompositeRangeSlider1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeRangeSlider1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_MVSCRIPT2|TopMV', 'ASSOCIATION_MVSCRIPT3|TopMV',
            'ASSOCIATION_Query1|TopMV', 'ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'MVSCRIPT1|TopMV', 'MVSCRIPT2|TopMV',
            'MVSCRIPT3|TopMV', 'Query1|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/40344')
   def 'TestCase_CompositeRangeSlider2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeRangeSlider2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/40344')
   def 'TestCase_CompositeRangeSlider3'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeRangeSlider3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/40344')
   def 'TestCase_CompositeRangeSlider4'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeRangeSlider4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompositeSelectionlist'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeSelectionlist'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Copy of Query11|TopMV', 'ASSOCIATION_Copy of Query12|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'Copy of Query11|TopMV', 'Copy of Query12|TopMV', 'Query1|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompositeSelectionTree1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeSelectionTree1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompositeSelectionTree2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeSelectionTree2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_CompositeSelectionTree3'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_CompositeSelectionTree3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Condition'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Condition'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ASSOCIATION_Query1|TopMV', 'ORDERS1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_ExpressionCondition'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_ExpressionCondition'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_PRODUCTS1|TopMV', 'PRODUCTS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Formulacol'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Formulacol'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Hiddencolumn'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Hiddencolumn'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_products1|TopMV', 'products1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_MaxRow'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_MaxRow'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS1|TopMV', 'ASSOCIATION_Query1|TopMV', 'CUSTOMERS1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_MirrorCondition'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_MirrorCondition'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_MVCondition1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_MVCondition1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true, admin)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS2|TopMV', 'CUSTOMERS2|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'TestCase_MVCondition2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_MVCondition2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true, admin)

      materializedViews.createIncrementMV(1)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS3|TopMV', 'ASSOCIATION_Query3|TopMV', 'CUSTOMERS3|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'TestCase_NotSupportAssomv'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_NotSupportAssomv'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CUSTOMERS1|SubMV', 'ORDERS1|SubMV', 'CUSTOMERS1|SubMV', 'ORDERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Rangecol'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Rangecol'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ASSOCIATION_Query1|TopMV', 'ORDERS1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_RangeSlider1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_RangeSlider1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_RangeSlider2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_RangeSlider2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT2|TopMV', 'ASSOCIATION_Query2|TopMV', 'MVSCRIPT2|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_SelectionContainer'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_SelectionContainer'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'ORDERS1|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Selectionlist_Byte'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist_Byte'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_Query1|TopMV', 'MVSCRIPT1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Selectionlist_Long'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist_Long'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_Query1|TopMV', 'MVSCRIPT1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Selectionlist_Short'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist_Short'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_Query1|TopMV', 'MVSCRIPT1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Selectionlist1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_MVSCRIPT2|TopMV', 'ASSOCIATION_MVSCRIPT3|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'MVSCRIPT1|TopMV', 'MVSCRIPT2|TopMV', 'MVSCRIPT3|TopMV', 'Query1|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   //建mv之后有精度问题
   def 'TestCase_Selectionlist2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_MVSCRIPT2|TopMV', 'ASSOCIATION_MVSCRIPT3|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'MVSCRIPT1|TopMV', 'MVSCRIPT2|TopMV', 'MVSCRIPT3|TopMV', 'Query1|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Selectionlist3'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Selectionlist3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_MVSCRIPT1|TopMV', 'ASSOCIATION_MVSCRIPT2|TopMV', 'ASSOCIATION_MVSCRIPT3|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'ASSOCIATION_Query3|TopMV', 'MVSCRIPT1|TopMV', 'MVSCRIPT2|TopMV', 'MVSCRIPT3|TopMV', 'Query1|TopMV', 'Query2|TopMV', 'Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_SelectionTree'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_SelectionTree'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Copy of Query11|TopMV', 'ASSOCIATION_Copy of Query12|TopMV', 'ASSOCIATION_Query1|TopMV',
            'ASSOCIATION_Query2|TopMV', 'Copy of Query11|TopMV', 'Copy of Query12|TopMV', 'Query1|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_VariableCondition1'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_VariableCondition1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      def params = ['var': ['0.01', '0.05', '0.1'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Query1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_VariableExpresion'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_VariableExpresion'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      def params = ['sts': ['CA', 'NJ', 'NY'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS1|TopMV', 'CUSTOMERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_VariableRanking'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_VariableRanking'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      def params = ['tn': ['20'] as String[]]
      mvtest.executeVS(params, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_AdhocFilter'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_AdhocFilter'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_OrdersAndReturns|TopMV', 'OrdersAndReturns|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_AdhocFilter2'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_AdhocFilter2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'TestCase_Census'() {
      given:
      String asset_id = '1^128^__NULL__^AssociationMV/TestCase_Census'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id, MVTest.getControllersResource())
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk_select'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_Data|TopMV', 'Data|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

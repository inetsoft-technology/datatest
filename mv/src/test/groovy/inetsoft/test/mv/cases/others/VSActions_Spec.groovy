package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.test.mv.MaterializedViewResource
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Execution(ExecutionMode.CONCURRENT)
class VSActions_Spec extends Specification {
   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'numberic_jsrange'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/numberic_jsrange'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'numberic_sqlrange'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/numberic_sqlrange'
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

   def 'numberic-rangeinc'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Other/numberic-rangeinc'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'selection_compatible1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection_compatible1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCUSTOMERS1|SubMV', 'CUSTOMERS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection_compatible2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection_compatible2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CUSTOMERS1|TopMV', 'CUSTOMERS2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection_compatible3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection_compatible3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false,true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection_compatible4'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection_compatible4'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['C_ORDERS1_ORDERS2|TopMV', 'ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection_compatible5'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/selection_compatible5'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'formulacol_aggrsinc'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Other/formulacol_aggrsinc'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'element binding1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/element binding1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['l1|TopMV', 'l1|TopMV', 'l1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'element binding2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/element binding2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV', 'Order Model|TopMV',
                                           'Order Model|TopMV', 'Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38256')
   def 'element binding3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/element binding3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|TopMV', 'query1|TopMV', 'query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'astimeseries-table'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/astimeseries-table'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Retry(count = 3)
   def 'astimeseries-chart'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/astimeseries-chart'
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

   def 'astimeseries-crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/astimeseries-crosstab'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV', 'Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'astimeseries-freehand'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/astimeseries-freehand'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS|TopMV', 'ORDERS|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'astimeseries-inc'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Other/astimeseries-inc'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, null, false, false)

      materializedViews.createIncrementMV(false, 1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'drill down filter1'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/drill down filter1'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'drill down filter2'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/drill down filter2'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/45181')
   def 'drill down filter3'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/drill down filter3'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/48178')
   def 'brush filter'() {
      given:
      String asset_id = '1^128^__NULL__^hit mv/brush filter'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Order Model|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

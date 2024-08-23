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
class TabularSource_Spec extends Specification {
   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      materializedViews.removeMV()
   }

   def 'data.gov'() {
      given:
      String asset_id = '1^128^__NULL__^tabular source/data.gov'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter1', 'filter2'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'server file'() {
      given:
      String asset_id = '1^128^__NULL__^tabular source/server file'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'odata'() {
      given:
      String asset_id = '1^128^__NULL__^tabular source/odata'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   def 'rest json'() {
      given:
      String asset_id = '1^128^__NULL__^tabular source/rest json'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], false, true)
      materializedViews.createIncrementMV(false, 2)
      mvtest.executeVS(null, ['(Home)', 'filter'] as String[], true, true)

      expect:
      verifyAll {
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'mongo'() {
      given:
      String asset_id = '1^128^__NULL__^tabular source/mongo'
      mvtest = new MVTest(asset_id)
      materializedViews = new MaterializedViewResource(asset_id)
      materializedViews.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'filter', 'brush', 'group'] as String[], false, true)

      expect:
      mvtest.compareData(false)
   }

   MVTest mvtest
   MaterializedViewResource materializedViews
}

package inetsoft.test.mv.cases.topmv

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
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
class TopMV_Subquery_Spec extends Specification {
   @Shared
           admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
                   String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      mvtest.removeMV()
   }

   def 'Subquery_Mode1_1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode1_1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode1_2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode1_2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //鎵嬪姩涓嶇ǔ瀹氶噸鐜癴ilter涔嬪悗鏁版嵁涓嶅鐨勯棶棰樸€傜涓€娆℃瘮杈冨鏄撻噸鐜? 鑷姩鍖杄xport鍑烘潵鐨勬暟鎹笉瀵?   def 'Subquery_Mode1_3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode1_3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV', 'orders1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode1_4'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode1_4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV', 'Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV', 'Detail_customers2|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ST_customers1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_4'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_5'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_5'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode2_6'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode2_6'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products1|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38278')
   def 'Subquery_Mode3_4'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_5'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_5'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_6'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_6'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Subquery_Mode3_7'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_7'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['products1|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38278')
   def 'Subquery_Mode3_8'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_8'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query4_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Ignore
   //鑷姩鍖杕v table 浼犲叆鐨則able len 涓嶅锛屾槸selection list 鐨?full table
   def 'Subquery_Mode3_9'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_9'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Detail_Query4_O|SubMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/38278')
   def 'Subquery_Mode3_10'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/Subquery_Mode3_10'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query4_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
}

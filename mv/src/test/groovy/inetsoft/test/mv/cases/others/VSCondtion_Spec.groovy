package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class VSCondtion_Spec extends Specification {
   @Shared
           admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
                   String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      mvtest.removeMV()
   }

   def 'assembly_model'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/assembly_model'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_l2|TopMV', 'l2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'assembly_query'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/assembly_query'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_query3|TopMV', 'query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'assembly_table'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/assembly_table'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CUSTOMERS|TopMV', 'CUSTOMERS|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable_ws_1'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/variable_ws_1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      def params = ['string startwith': ['F'] as String[], 'string contains': ['Mail'] as String[]]
      mvtest.executeVS(params, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable_ws_2'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/variable_ws_2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      def params = ['float startwith': ['0.1'] as String[], 'char contains': ['b'] as String[]]
      mvtest.executeVS(params, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable_ws_3'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/variable_ws_3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      def params = ['char startwith': ['d'] as String[], 'float contains': ['0.2'] as String[]]
      mvtest.executeVS(params, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable_ws_4'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/variable_ws_4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      def params = ['int startwith': ['2'] as String[], 'int contains': ['5'] as String[]]
      mvtest.executeVS(params, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_1'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_2'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_3'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_4'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_5'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_5'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_6'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_6'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_7'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_7'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_8'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_8'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_9'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_9'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_10'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_10'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_11'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_11'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_12'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_12'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_13'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_13'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_14'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_14'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_15'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_15'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_16'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_16'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_17'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_17'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_18'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_18'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_19'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_19'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_20'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_20'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_21'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_21'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_22'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_22'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_23'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_23'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_24'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_24'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_25'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_25'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_26'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_26'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }


   def 'C_V_27'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_27'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'C_V_28'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_28'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/44348')
   def 'C_V_29'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/C_V_29'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'variable'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/variable'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'expression'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/expression'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/44378')
   @Ignore
   def 'field'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/field'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'session'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/session'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'condition1'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/condition1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_l2|TopMV', 'l2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'condition2'() {
      given:
      String asset_id = '1^128^__NULL__^vs query and condition/condition2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['PRODUCTS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
}

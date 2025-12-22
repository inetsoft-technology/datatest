package inetsoft.test.mv.cases.incrementalmv

import inetsoft.test.mv.MVTest
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

@Execution(ExecutionMode.SAME_THREAD)
class Delete_Spec extends Specification {
   @Shared
           admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
                   String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      mvtest.removeMV()
   }

   def 'maxValue_Datetime'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/maxValue_Datetime'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_Double'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/maxValue_Double'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_Integer'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/maxValue_Integer'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'maxValue_String'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/maxValue_String'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(4)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Datetime'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/minValue_Datetime'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Double'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/minValue_Double'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_Integer'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/minValue_Integer'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVSCRIPT1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'minValue_String'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/minValue_String'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(2)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVScript1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Testcase1'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   //Add by agile
   def 'Testcase_like'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase_like'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase2'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase3'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase4'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase4'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorderdetails1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase5'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase5'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase6'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase6'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase7'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase7'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase8'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase8'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase9'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase9'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }
   //no bs block generated, so don not check mv table
   def 'Testcase10'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase10'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      mvtest.compareData(false)
   }

   def 'Testcase11'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase11'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase12'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase12'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase13'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase13'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase14'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase14'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV', 'MVorderdetails1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase15'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase15'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders2|SubMV', 'MVorders1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase16'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase16'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MinusTable|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase17'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase17'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['IntersectTable|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase18'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase18'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase19'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase19'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase20'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase20'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase21'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase21'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase22'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase22'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase23'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase23'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|SubMV', 'MVorders2|SubMV'])
         mvtest.compareData(false)
      }
   }
   //no bs block generated, so don not check mv table
   def 'Testcase24'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase24'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      mvtest.compareData(false)
   }

   def 'Testcase25'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase25'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVCustomers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase26'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase26'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      mvtest.createIncrementMV(1)
      mvtest.executeVS(null, null, true, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['MVorders1|TopMV'])
         mvtest.compareData(false)
         mvtest.compareData(true)
      }
   }

   def 'Testcase27'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase27'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['customers1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'Testcase28'() {
      given:
      String asset_id = '1^128^__NULL__^MV_Creator/Delete/Testcase28'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['orderdetails1|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
}

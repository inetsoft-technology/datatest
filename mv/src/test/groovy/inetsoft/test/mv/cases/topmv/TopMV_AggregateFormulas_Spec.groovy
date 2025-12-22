package inetsoft.test.mv.cases.topmv

import inetsoft.test.mv.MVTest
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class TopMV_AggregateFormulas_Spec extends Specification {
   @Shared
           admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
                   String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      mvtest.removeMV()
   }

   def 'child has aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child has aggregate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'parent has aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent has aggregate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'date group'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/date group'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'date group-none'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/date group-none'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate col is expression'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/aggregate col is expression'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate second col is expression'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/aggregate second col is expression'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Copy ofQuery12|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'two columns formula'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/two columns formula'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'aggregate formula'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/aggregate formula'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['distinctcount|TopMV', 'median|TopMV', 'mode|TopMV'])
         mvtest.compareData(false)
      }
   }

   //数据精度发生变化, 产品上数据精度从第3位开始发生变化
   def 'aggregate formula2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/aggregate formula2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'both have aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/both have aggregate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'no aggregate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/no aggregate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-average'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-average'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-concatenate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-concatenate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-convariance'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-convariance'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-correlation'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-correlation'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-std deviation'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-std deviation'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-std deviation pop'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-std deviation pop'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-variance'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-variance'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-variance pop'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-variance pop'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-weighted average'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-weighted average'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-average'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-average'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-concatenate'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-concatenate'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query4_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-convariance'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-convariance'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-correlation'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-correlation'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-std deviation'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-std deviation'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-std deviation pop'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-std deviation pop'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-variance'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-variance'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-variance pop'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-variance pop'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-weighted average'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-weighted average'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/49843')
   def 'datelevel_none'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/datelevel_none'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, false)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['EM2_M_GandA|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
}

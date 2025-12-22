package inetsoft.test.mv.cases.topmv

import inetsoft.test.mv.MVTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class TopMV_TableStructures_Spec extends Specification {
   @Shared
           admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as
                   String[], new String[0])

   def setupSpec() {
      MVTest.initHome()
   }

   def cleanup() {
      mvtest.removeMV()
   }

   def 'child-aggregate-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child-aggregate-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'child-plain-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/child-plain-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/37226')
   def 'parent-aggregate-distinct-selection col not public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-distinct-selection col not public'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'parent-aggregate-distinct-selection col public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-distinct-selection col public'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Issue('http://173.220.179.100/issues/37244')
   def 'parent-aggregate-maxrow-selection col not public'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-aggregate-maxrow-selection col not public'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'parent-plain-distinct'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/parent-plain-distinct'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'composite1'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite1'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'composite2'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite2'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   @Ignore
   def 'composite3'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/composite3'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6'] as String[], false, true)
      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query3|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'crossjoin'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/crossjoin'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_CATEGORIES1|TopMV', 'CATEGORIES1|TopMV', 'Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'date range'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/date range'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'expression'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/expression'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'intersect'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/intersect'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'left outer join-select left'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/left outer join-select left'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'right outer join-select right'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/right outer join-select right'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'merge-join'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/merge-join'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'unmerge-join'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/unmerge-join'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query4_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'minus'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/minus'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'union'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/union'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4', 'bk5'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['CATEGORIES1|SubMV', 'REGIONS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'threetables'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/threetables'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'RETURNS1|SubMV', 'PRODUCTS1|SubMV'])
         mvtest.compareData(false)
      }
   }

   def 'name group'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/name group'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'numeric range'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/numeric range'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'rename'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/rename'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ASSOCIATION_ORDERS1|TopMV', 'ORDERS1|TopMV', 'Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection on aggregate second col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on aggregate second col'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection on crosstab'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on crosstab'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query2|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection on hidden group col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on hidden group col'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1', 'bk2', 'bk3', 'bk4'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['ORDERS1|SubMV', 'ORDERS1|TopMV'])
         mvtest.compareData(false)
      }
   }

   def 'selection on other col'() {
      given:
      String asset_id = '1^128^__NULL__^topmv/selection on other col'
      mvtest = new MVTest(asset_id)

      mvtest.createMV(false)
      mvtest.executeVS(null, ['(Home)', 'bk1'] as String[], false, true)

      expect:
      verifyAll {
         mvtest.getMVDefInfo().containsAll(['Query1_O|TopMV'])
         mvtest.compareData(false)
      }
   }

   MVTest mvtest
}

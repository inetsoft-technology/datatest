package inetsoft.test.olap.cases

import inetsoft.test.global.GlobalTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class CubeWSAndApply_Spec extends Specification{
   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }

   /*
    check single cube_vs and apply '
    */
   def 'SingeleTable_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/SingleTable', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_Table', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check mirror cube_vs and apply '
    */
   def 'MirrorTable_Baning'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/Mirror', null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_Chart', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   def 'EmbeddedWS_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/EmbeddedWS', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Binding_Input',['bk1', '(Home)'] as String[], null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check InnerJoin_Inner cube_vs and apply '
    */
   def 'InnerJoin_Inner_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/InnerJoin_Inner', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/GroupOption', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check InnerJoin_Full cube_ws and apply '
    */
   def 'InnerJoin_Full_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/InnerJoin_Full', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_RangeSlider', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check InnerJoin_Full cube_ws and apply '
    */
   def 'MergeJoin_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/MergeJoin', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_Filter', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check CrossJoin cube_ws and apply '
    */
   def 'CrossJoin_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/CrossJoin', null, null)


      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

      /*
    check Concatenate cube_ws and apply '
    */
   def 'Concatenate_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/Concatenate', null, null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check SubQuery cube_ws and apply '
    */
   def 'Subquery_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/Subquery', null, null)


      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check SubQuery cube_ws and apply '
    */
   def 'Union_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/Union', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/AggregateOption', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check SubQuery cube_ws and apply '
    */
   def 'AdvancedCondition_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/AdvancedCondition', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_FormAndOutput', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check cube worksheet has alias and with alias do group then applied on worksheet '
    */
   def 'InvisibleRename_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/InvisibleRename', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Banding_FreehandTable', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check base table(cube setting drill&format) apply on viewsheet  '
    */
   def 'BaseTable_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/AutodrillAndFormat', null)


      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /*
    check others single table and cube worksheet be used on js runquery '
    */
   def 'OthersSingeTable_Banding'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      //globalTest.executeTest('1^2^__NULL__^cube worksheet/CubeWS_tables', null, null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/Source_RunQuery', null)

      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /*
    check others single table and cube worksheet be used on js runquery '
    */
   def 'CubeWS_HierarchyDrill'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/HierarchyDrill',["Expand","DrillFilter_Down1","DrillFilter_Down2","DrillFilter_Up", "Home"] as String[], null)

      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * check ws col use complex formula, and apply on vs.
    * Issue #59675
    */
   def 'CubeWS_wsWithFormula' () {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/wsWithFormula', null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/WSFormulaApply', null)


      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /**
    * check ws col use name group, and apply on vs
    * Issue #59692
    */
   def 'CubeWS_wsWithGroup' () {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/wsWithGroup', null)
      globalTest.executeTest('1^128^__NULL__^CubeWS_Apply/CubeWS_VS/WSGroupApply', null)


      expect:
      verifyAll {
         globalTest.compareImage()
         globalTest.compareData()
      }
   }

   /**
    * check ws use post condition and variable
    */
   def 'CubeWS_wsWithCondition' () {
      def paras = new HashMap<String, Object>()
      paras.put('TopCost', 3)

      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^2^__NULL__^cube worksheet/wsWithCondition', paras)

      expect:
      verifyAll {
         globalTest.compareData()
      }
   }
}
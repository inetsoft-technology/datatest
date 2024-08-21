package inetsoft.test.olap.cases

import inetsoft.test.global.GlobalTest
import spock.lang.Specification

class DBCube_Component_Spec extends Specification {

   static String caseName
   static GlobalTest globalTest

   def setupSpec() {
      GlobalTest.initHome(this.class.getName())
   }



   /**
    * test Marimekko Funnel,BoxPlot,Map type use cube binding, then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type6'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type6',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Tree/Network/ Circular Network, then do drill, bursh filter
    * Issue #59761
    */
   def 'CubeChart_Chart_Type5'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type5',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Treemap/Circle Packing /Sunburst/Icicle, then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type4'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type4',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Stock/Candle then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type3'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type3',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test pie, radar then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type2',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test StepLine, Jump line, line, point then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type1',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test WaterFul, Patro then do drill, bursh filter
    */
   def 'CubeChart_Chart_Type1_2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type1_2',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test multi style, see Bug #60650
    */
   def 'CubeChart_Chart_Type1_3'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Type1_3', null, null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test some special situation, See #60652
    */
   def 'CubeChart_Chart_Special1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Special1', null, null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test scattermatrix chart, Bug #60664
    */
   def 'CubeChart_Chart_Special2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Chart/Chart_Speical2',
              ['(Home)', 'brush'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test only group or only Aggregate then do drill, bursh filter
    */
   def 'Cube_Crosstab_CrosstabBind1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Crosstab/CrosstabBind1',
              ['(Home)', 'drill1', 'drill2'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test crosstab, calculate on grand total and group total
    */
   def 'Cube_Crosstab_CrosstabBind2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Crosstab/CrosstabBind2',
              ['(Home)', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test crosstab, calculate on grand total and group total
    */
   def 'Cube_Crosstab_CrosstabBind3'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Crosstab/CrosstabBind3',
              ['(Home)', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test range slider binding crosstab aggrgeate, which use calcfield, sum_calcfield, normal measure.
    */
   def 'Cube_Crosstab_CrosstabBind4'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Crosstab/CrosstabBind4',
              ['(Home)', 'drill1', 'drill2'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test filter binding on db olap
    */
   def 'Cube_Others_CFilter1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CFilter1',
              ['(Home)', 'bk1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test output and text use cube measure, use calcfield
    */
   def 'Cube_Others_COutput'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/COutPut',
              ['(Home)', 'bk1', 'bk2'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test component binding: use crosstab
    */
   def 'Cube_Others_ComBinding1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/ComBinding1',
              ['(Home)', 'bk1', 'brush1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test component binding: use crosstab
    */
   def 'Cube_Others_ComBinding2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/ComBinding2',
              null, null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Crosstab use Aggregate type
    */
   def 'Cube_Others_CAggregate1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CAggregate1',
              ['(Home)', 'drill1', 'drill2'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Crosstab,Chart,Output, Filter use calcfield type
    */
   def 'Cube_Others_CCalcField1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CCalcField1',
              ['(Home)', 'brush1', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Crosstab,Chart,Output, Filter use calcfield type
    */
   def 'Cube_Others_CCalcField2'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CCalcField2',
              ['(Home)', 'drill1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Crosstab,Chart use same measure to do diff aggregate
    */
   def 'Cube_Others_CDiffAggregate1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CDiffAggregate1',
              null, null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }

   /**
    * test Crosstab,Chart use dynimac binding
    */
   def 'Cube_Others_CDynamic1'() {
      caseName = specificationContext.currentIteration.name
      globalTest = new GlobalTest(caseName)
      globalTest.executeTest('1^128^__NULL__^DBOlap/Cube_Others/CDynamic1',
              ['(Home)', 'bk1'] as String[], null)
      expect:
      verifyAll {
         globalTest.compareImage()
      }
   }



}

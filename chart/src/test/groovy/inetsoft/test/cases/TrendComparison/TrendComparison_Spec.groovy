package inetsoft.test.cases.TrendComparison

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class TrendComparison_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   //check value of previous when binding different date level
   def 'previous1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous1', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   //check value of previous apply when change col infor or properties
   def 'previous2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous2', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   //check value of previous apply when style is multile style and fact chart
   def 'previous3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous3', caseName)
      vstest.exportAsPNG(null, null)
      vstest.exportAsPDF(null, null)


      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   //check value of previous apply when binding string col and brush string col
   def 'previous4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous4', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1', 'bk2'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   //check valur of previous when exsit pdate
   def 'previous5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous5', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //check valur of previous on pareto
   def 'previous6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previous6', caseName)
      vstest.exportAsPNG(null, null)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }


   //check change from previous forstring type
   def 'previousstring'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previousstring', caseName)
      vstest.executeVS(null, ['Home','bk1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //check change from previous for string type<some special cases>
   def 'previousstring2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/previousstring2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //check change from first/last
   def 'changefirstlast'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/changefirstlast', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //check percenttotal
   def 'percentoftotal'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/percentoftotal', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }


   //check percenttotal
   def 'movingaverage'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/movingaverage', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    //check compoundgrowth1
   def 'compoundgrowth1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/compoundgrowth1', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    //check compoundgrowth2
   def 'compoundgrowth2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/TrendComparison/compoundgrowth2', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
}
package inetsoft.test.cases.Binding

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class Binding_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   def 'binding1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding1', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //Chart3, bug #49953, rejected
   def 'binding2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding2', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'binding3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding3', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }


   def 'binding5'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding5', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //Note:chart3's  prent of subtotal col be lost in exported text exist issue, ignore.
   def 'binding6'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding6', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //Bug #52161, be rejected
   def 'binding7'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding7', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'binding8'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding8', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'binding9'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding9', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'binding10'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding10', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'binding11'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding11', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //Chart3, bug #50069, ignored
   //Note:chart4's  prent of subtotal col be lost in exported text exist issue, ignore.
   def 'binding12'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding12', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   //check dimension data exist empty
   def 'binding13'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding13', caseName)
      vstest.exportAsPNG(null,['Home', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   //check ws's formual can draw chart righly
   def 'binding14'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding14', caseName)
      vstest.exportAsPNG(null,['Home', 'bk1'] as String[])
      vstest.exportAsPDF(null,['Home', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   //Bug #52224
   def 'binding_stock'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/binding_stock', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])
      //vstest.exportAsPDF(null, null)

      expect:
      verifyAll {
         vstest.compareImage(null)
      }
   }

   def 'date group1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/date group1', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'aggregate2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/aggregate2', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'aggregate1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/aggregate1', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'discrete1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/discrete1', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'discrete2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/discrete2', caseName)
      vstest.executeVS(null, ['Home', 'filter'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'discrete3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/ChartBinding/discrete3', caseName)
      vstest.executeVS(null, ['Home', 'brush1', 'brush2'] as String[])

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
}
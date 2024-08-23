package inetsoft.test.viewsheet.cases.vscrosstab

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class Crosstab_Relation_Spec extends Specification {
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check crosstab banding multi aggregate add define ad hoc filter for bug50280
    */
   def 'crosstab _adhocfilter1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab _adhocfilter1', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check crosstab banding ad hoc filter when dimension column be dragged to aggregate column for bug49926
    */
   def 'crosstab _adhocfilter2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab _adhocfilter2', caseName)
      vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
      vstest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check crosstab banding adhoc filter when correlation and date none level highlight for bug50749&50233
    */
   def 'crosstab_adhocfilter3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab_adhocfilter3', caseName)
      vstest.executeVS(null, ['(Home)','filter1'] as String[])
      vstest.exportAsPNG(null, ['(Home)','filter1'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    * check binidng more measures and set trend&comparion, adhoc filter apply
    * Bug #51750
    */
   def 'crosstab_adhocfilter4'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab_adhocfilter4', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check crosstab's annotaion when banding calc filed and do drill,filter for bug50237
    */
   def 'crosstab annotation'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab annotation', caseName)
      vstest.executeVS(null, ['(Home)', 'drill','filter'] as String[])
      vstest.exportAsPNG(null, ['(Home)','drill','filter'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check crosstab's format when crosstab be created by scripting
    */
   def 'Crosstab_fomat1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Risk Dashboard V2', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check crosstab's format setting prensenter
    */
   def 'Crosstab_fomat2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Vehicle Fuel Efficiency', caseName)
      vstest.executeVS(null, ['(Home)'] as String[])
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   def 'crosstab_ExpandHierchy1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/crosstab_ExpandHierchy1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','bk1_expand','bk2_collasped'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check format keep after drill down/up
    */
   def 'Drilling_KeepFormat1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Drilling_KeepFormat1', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','expand1','expand2'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

    /*
    check format keep after drill down/up  --user defined hierachy
    */
   def 'Drilling_KeepFormat2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Drilling_KeepFormat2', caseName)
      vstest.executeVS(null, null)
      vstest.exportAsPNG(null, ['(Home)','collpase','expand'] as String[])

      expect:
      verifyAll{
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   static ViewsheetTest vstest
   static String caseName
}

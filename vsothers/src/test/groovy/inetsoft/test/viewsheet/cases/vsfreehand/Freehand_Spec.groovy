package inetsoft.test.viewsheet.cases.vsfreehand

import inetsoft.test.modules.VSCalcTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class Freehand_Spec extends Specification{

   def setupSpec() {
      VSCalcTest.initHome(this.class.getName())
   }
   /**
    * freehand binding ccrosstab, crosstab have 2 agg, summary side by side as true,
    * freehand is formuls script, use TopN
    */

   def 'CM11_1_1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_1', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1','filter1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * freehand binding ccrosstab, crosstab have 2 agg, summary side by side as true,
    *    freehand is fomula script, use name group
    */
   def 'CM11_1_2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_2', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','filter1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding lm, lm have calcfield. freehand have convert, binding, script
    */
   def 'CM11_1_3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_3', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding lm, lm have calcfield. freehand have convert, binding, script
    * freehand use TopN, name group, date level use period
    */
   def 'CM11_1_4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_4', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding ws, ws have formula col, date,range col
    * freehand set row/group total
    */
   def 'CM11_1_5'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_5', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding ws, ws have formula col, date,range col
    * freehand convert
    */
   def 'CM11_1_6'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_6', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding ws, ws have formula col, date,range col, calcfield
    * freehand binding
    */
   def 'CM11_1_7'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_7', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding query, query have detail and agg calcfield
    * script freehand as time is true
    */
   def 'CM11_1_8'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_8', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * binding table
    */
   def 'CM11_1_9'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_9', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check binding freehand table
    */
   def 'CM11_1_10'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/CM11_1_10', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check name group with lm, normal col
    */
   def 'NG1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/NG1', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check name group, range col from ws
    */
   def 'NG2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/NG2', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check name group, normal col, datetime col use ng
    */
   def 'NG3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/NG3', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check name group, use variable to transform data
    */
   def 'NG5'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/NG5', caseName)
      vsCalcTest.exportAsPNG(null, 'bk1')

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check ranking on special col
    */
   def 'Rank1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/Rank1', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check rank on detail calcfield
    */
   def 'Rank2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/Rank2', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check rank on NthFrequent type
    */
   def 'Rank3'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/Rank3', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check freehand use crosstab binding, then set rank
    */
   def 'Rank4'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/Rank4', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check freehand use all calcfield
    */
   def 'Calc1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Relation/Calc1', caseName)
      vsCalcTest.exportAsPNG(null, ['bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check the complex freehand used TopN
    */
   def 'bug_topN'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/topN', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','bk1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   def 'Time_None'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Time_None', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   def 'Freehand _distinctcount1'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand _distinctcount1', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   def 'Freehand _distinctcount2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand _distinctcount2', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   def 'Freehand_Others_Aggreagte'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand_Others_Aggreagte', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   def 'Freehand_annotation'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand_annotation', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check manual include null value&Named Group apply
    */
   def 'Freehand_manual'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand_manual', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)', 'adhoc_filter'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check manual,special custom case
    */
   def 'Freehand_manual2'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand_manual2', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * check date&string convert to mea, adhocfilter apply
    */
   def 'Freehand_adhocfilter'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Freehand_adhocfilter', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)', 'filter1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   /**
    * test set hyperlink on zero. --bug #56969
    * test set date format on Date column,FillBlank with Zero  is true.  --bug #56970
    * coverd crosstab,非crosstab，formula script structure
    */
   def 'FillBlankZeroDate'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/FillBlankZeroDate', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }
    /**
    * test trailter apply
    */
   def 'Trailer_Apply'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/Trailer_Apply', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }
   /**
    * test NamedGroup with special character. --bug##60816
    */
   def 'NG_specialChar'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/NG_specialChar', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }
   /**
    * test applying the cell formatting to cells that are set from script.. --bug##62464
    */
   def 'CellFormat_script'() {
      caseName = specificationContext.currentIteration.name
      vsCalcTest = new VSCalcTest('1^128^__NULL__^FreehandTable/Others/CellFormat_script', caseName)
      vsCalcTest.exportAsPNG(null, ['(Home)','format1'] as String[])

      expect:
      verifyAll{
         vsCalcTest.compareImage(null)
      }
   }

   static VSCalcTest vsCalcTest
   static String caseName
}

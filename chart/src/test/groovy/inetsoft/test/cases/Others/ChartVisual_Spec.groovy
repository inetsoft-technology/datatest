package inetsoft.test.cases.Others

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.Specification

class ChartVisual_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

 //test multi style visual pane all use static value
   def 'Aesthetic_noBanding'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_noBanding', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

 //test multi style visual pane use dim or measure banding 
    def 'Aesthetic_Banding'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_Banding', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

//special chart style use dimormeasure banding on visual pane
      def 'Aesthetic_chartstyle1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_chartstyle1', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

//special chart style use dimormeasure banding on visual pane
   def 'Aesthetic_chartstyle2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_chartstyle2', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

//special chart style use dimormeasure banding on visual pane
      def 'Aesthetic_chartstyle3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_chartstyle3', caseName)
      vstest.exportAsPNG(null, null)
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }


//column banding not display entra space(customer bug)
   def 'Aesthetic_forScale'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_forScale', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])

     expect:
      vstest.compareImage(null)
   }


 //test merge legend：color & size，color& shape
   def 'MergeLengend'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/MergeLengend', caseName)
      vstest.exportAsPNG(null, ['Home', 'filter'] as String[])

     expect:
      vstest.compareImage(null)
   }


   //test map asethtic when setting topn
   def 'MapTopN'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/MapTopN', caseName)
      vstest.exportAsPNG(null, ['Home', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   //test value as color frame
   def 'value as color'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/value as color', caseName)
      vstest.exportAsPNG(null, ['Home','bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

    //check multistyle chart column color apply
   def 'value as color2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/value as color2', caseName)
      vstest.exportAsPNG(null, ['Home', 'brush1'] as String[])

      expect:
      vstest.compareImage(null)
   }

    //check Bug#50573: check map chart default color&boxplot brush relation
   def 'value as color3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/value as color3', caseName)
      vstest.exportAsPNG(null, ['Home', 'brush1'] as String[])

      expect:
      vstest.compareImage(null)
   }

    //check Bug#59121&Bug#59119 portal shape from image use auto size
   def 'Shape_Autosize1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Shape_Autosize1', caseName)
      vstest.exportAsPNG(null, ['Home'] as String[])

      expect:
      vstest.compareImage(null)
   }

    //check Bug#59121&Bug#59119 portal shape from image use auto size
   def 'Shape_Autosize2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Shape_Autosize2', caseName)
      vstest.exportAsPNG(null, ['Home'] as String[])

      expect:
      vstest.compareImage(null)
   }

 //test text highlight and format setting
    def 'Aesthetic_Text'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Aesthetic/Aesthetic_Text', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }
}


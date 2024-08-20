package inetsoft.test.css.cases

import inetsoft.test.css.CSSTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class TableStyle_Spec extends Specification {
   static CSSTest cssTest
   static String caseName

   def setupSpec() {
      CSSTest.initHome(this.class.getName())
   }

   def property1 = "   color: red; \n" +
             "   background-color: #FFDF81; \n" +
             "   font-family: Arial; \n" +
             "   font-weight: bold; \n" +
             "   font-style: italic; \n" +
             "   font-size: 14px; \n" +
             "   text-decoration: underline; \n" +
             "   border-style:double; \n" +
             "   border-color: #FFFFFF; \n" +
             "   text-align: left; \n" +
             "   vertical-align: top; \n"

   def property2 = "   color: blue; \n" +
             "   background-color: #D4D0F2; \n" +
             "   font-family: Arial; \n" +
             "   font-weight: bold; \n" +
             "   font-style: normal; \n" +
             "   font-size: 12px; \n" +
             "   text-decoration: line-through; \n" +
             "   border-style:dotted; \n" +
             "   border-color: #00FFFF; \n" +
             "   text-align: right; \n" +
             "   vertical-align: middle; \n"

   def property3 = "   color: orange; \n" +
             "   background-color: #C0DCC0; \n" +
             "   font-family: Arial; \n" +
             "   font-style: italic; \n" +
             "   font-weight: bold; \n" +
             "   font-size: 14px; \n" +
             "   text-decoration: underline; \n" +
             "   border-style:dashed; \n" +
             "   border-color: rgb(243,123,123); \n" +
             "   text-align: left; \n" +
             "   vertical-align: bottom; \n"

   def property4 = "   color: rgb(243,123,123); \n" +
             "   background-color: #A6CAF0; \n" +
             "   font-family: Arial; \n" +
             "   font-style: normal; \n" +
             "   font-weight: bold; \n" +
             "   font-size: 12px; \n" +
             "   text-decoration: line-through; \n" +
             "   border-style:double; \n" +
             "   border-color: #FFFFFF; \n" +
             "   text-align: right; \n" +
             "   vertical-align: middle; \n"

   def css1 = "TableStyle[region=HeaderRow]{ \n" +
               property1 +
               "   padding-left: 5px; \n" +
               "   padding-top: 5px; \n" +
               "   padding-bottom: 8px; \n" +
          "   } \n" +
          "TableStyle[region=HeaderCol]{ \n" +
               property2 +
          "   } \n" +
          "TableStyle[region=TrailerRow]{ \n" +
               property3 +
          "   } \n" +
          "TableStyle[region=TrailerCol]{ \n" +
               property4 +
          "   } \n" +
          "TableStyle[region=Table]{ \n" +
             "   border-color: #0012FF; \n" +
             "   border-style: solid; \n" +
          "   } \n" +
          "TableStyle[region=Body]{ \n" +
             "   color: green; \n" +
             "   background-color: #ECEBE2; \n" +
             "   font-family: Arial; \n" +
             "   font-style: italic; \n" +
             "   font-weight: normal; \n" +
             "   font-size: 12px; \n" +
             "   text-decoration: underline; \n" +
             "   border-style:thin line; \n" +
             "   border-color: #FFFFFF; \n" +
             "   text-align: right; \n" +
             "   vertical-align: top; \n" +
             "   padding-right: 10px; \n" +
             "   padding-top: 5px; \n" +
             "   padding-bottom: 8px; \n" +
          "   } \n"

   def css2 = "TableStyle[region=Body][pattern=OddRow]{ \n" +
               property1 +
          "   } \n" +
          "TableStyle[region=Body][pattern=EvenRow]{ \n" +
               property2 +
          "   } \n"

   def css3 = "TableStyle[region=Body][pattern=OddCol]{ \n" +
               property1 +
          "   } \n" +
          "TableStyle[region=Body][pattern=EvenCol]{ \n" +
               property2 +
          "   } \n"

   def border1 = "   border:double black; \n"
   def border2 = "   border-style:dotted; \n" +
                 "   border-color: rgb(255,0,0); \n"
   def border3 = "   border-right: dotted #99cc00; \n" +
                 "   border-left: solid green; \n" +
                 "   border-bottom: solid yellow; \n" +
                 "   border-top: dotted blue; \n"
   def border4 = "   border-style:solid; \n" +
                 "   border-top-color: #ffffff; \n" +
                 "   border-right-color:#ff00ff; \n" +
                 "   border-bottom-color: blue; \n" +
                 "   border-left-color:green; \n"

   def borders = "TableStyle[region=Table]{ \n" +
                     border1 +
                "   } \n" +
                "TableStyle[region=HeaderRow]{ \n" +
                     border2 +
                "   } \n" +
                "TableStyle[region=TrailerCol]{ \n" +
                     border3 +
                "   } \n" +
                "TableStyle[region=Body][pattern=OddRow]{ \n" +
                     border4 +
                "   } \n"

   def "test TableStyle selector apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test TableStyle OddRow and EvenRow apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css2)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   //bug #59601, ignored
   def "test TableStyle OddCol and EvenCol apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css3)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   def "test borders apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(borders)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   //bug #59637
   def "test different table style apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)

      def css = "Crosstab TableStyle[region=HeaderRow]{ \n" +
                    property1 +
                "}\n" +
                "Table TableStyle[region=HeaderRow]{ " +
                    property2 +
                "}\n" +
                "FreehandTable TableStyle[region=HeaderRow]{ " +
                    property3 +
                "}\n" +
                "Table {\n" +
                "     background-color: yellow;\n" +
                "}\n"

      cssTest.InitCSS(css)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }
}
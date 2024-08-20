package inetsoft.test.css.cases

import inetsoft.test.css.CSSTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class Global_Spec extends Specification {
   static CSSTest cssTest
   static String caseName

   def setupSpec() {
      CSSTest.initHome(this.class.getName())
   }

   def css1 = "*{ \n" +
          "   color: blue;\n" +
          "   background-color: #BAE3F7;\n" +
          "   opacity:0.1;\n" +
          "   font-family: SansSerif;\n" +
          "   font-weight: bold;\n" +
          "   font-style: italic;\n" +
          "   font-size: 11px;\n" +
          "   border: double dotted orange;\n" +
          "   text-decoration: line-through;\n" +
          "   text-align: right;\n" +
          "   vertical-align: top;\n" +
          "   word-wrap: break-word;\n" +
          "}"

   def "test global css selector apply on vs chart" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/chart')

      expect:
      cssTest.compareImage()
   }

   def "test global css selector apply on vs tables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }

   //bug #59499
   def "test global css selector apply on vs filter" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/filter')

      expect:
      cssTest.compareImage()
   }

   def "test global css selector apply on vs form" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/form')

      expect:
      cssTest.compareImage()
   }

   def "test global css selector apply on vs output and shape" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      cssTest.InitCSS(css1)
      cssTest.VSCSSTest('1^128^__NULL__^vs/output_shape')

      expect:
      cssTest.compareImage()
   }

   def "test css variables" () {
      caseName = specificationContext.currentIteration.name
      cssTest = new CSSTest(caseName)
      def cssv = "* {\n" +
                 "  --color: red;\n" +
                 "  --bg-color: yellow;\n" +
                 "}\n" +
                 "Viewsheet {\n" +
                 "   background-color: var(--bg-color);\n" +
                 "}\n"+
                 "Table {\n" +
                 "   color: var(--color);\n" +
                 "}\n"
      cssTest.InitCSS(cssv)
      cssTest.VSCSSTest('1^128^__NULL__^vs/tables')

      expect:
      cssTest.compareImage()
   }
}

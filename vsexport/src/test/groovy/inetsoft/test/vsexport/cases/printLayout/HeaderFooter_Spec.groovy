package inetsoft.test.vsexport.cases.printLayout

import inetsoft.test.viewsheet.VSExportTest
import spock.lang.Specification

class HeaderFooter_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check Text format
    */
   def 'HF_Format1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/FormatExecute_1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check text, image in, out, part in line
    */
   def 'HF_Exec1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Table,Freehand,Crosstab print in Header and Footer
    */
   def 'HF_Exec2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Chart, Image,Gauge print in Header and Footer
    */
   def 'HF_Exec3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_3' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Text,Selections,Input print in Header and Footer
    */
   def 'HF_Exec4' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_4' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check Group,Tab, EMVS print in Header and Footer
    */
   def 'HF_Exec5' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_5' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check componnet position in Header and Footer
    */
   def 'HF_Exec6' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/HeaderFooter/PreviewExecute_6' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check all element on page break
    */
   def 'PageBreak1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/PageBreak1' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check all element on page break
    */
   def 'PageBreak2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/PageBreak2' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }

   /**
    * check group, tab, embedded vs on page break
    */
   def 'PageBreak3' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Others/PageBreak3' , caseName)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      vsExportTest.comparePNG()
   }
}

package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.viewsheet.ViewsheetTest
import spock.lang.Specification

/**
 * use to check input and output properties, options and format
 */
class InputOutput_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check slider with diff binding, options and format
    */
   def 'InputSliderPro1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputSliderPro1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    *check spinner all properties, format, diff binding
    */
   def 'InputSpinnerPro1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputSpinnerPro1', caseName)
      vstest.exportAsPNG(null,null)

      expect:
      vstest.compareImage()
   }

   /**
    * check checkbox and radiobutton binding diff type col and format
    */
   def 'InputCheckRadioPro1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputCheckRadioPro1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'asc', 'desc'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check checkbox and radiobutton binding diff type col and format on value
    * Bug #65621 Since the checkbox in this case shows a 12-hour clock, the stylebi uses a 24-hour clock, so this bug can be ignored.
    */
   def 'InputCheckRadioPro2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputCheckRadioPro2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'asc', 'desc'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check checkbox embedded&query list value, sort, first select item option and filter the data
    * first select item option can't check, because this property apply on current view, not 'Home' bookmark. see Bug #65210
    */
   def 'InputCheckRadioPro3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputCheckRadioPro3', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check others(combox, textinput, submit, upload) format
    */
   def 'InputOthers'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/InputOthers', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check output diff binding, format , properties
    */
   def 'OutputGauge'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/OutputGauge', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'labelIsFalse'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check text diff binding, format , properties
    */
   def 'OutputText'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/OutputText', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage()
   }

   /**
    * check image diff binding, format , properties
    */
   def 'OutputImage'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^InputOutput/OutputImage', caseName)
      vstest.exportAsPNG(null, ['img1', 'img2', 'img3'] as String[])

      expect:
      vstest.compareImage()
   }
}

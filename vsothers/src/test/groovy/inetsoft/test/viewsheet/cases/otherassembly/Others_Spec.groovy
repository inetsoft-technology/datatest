package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import org.springframework.test.context.ContextConfiguration

/**
 * use to check input and output properties, options and format
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
class Others_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /**
    * check tab, tab[Active Tab] format
    */
   def 'Tab_format'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Others/Tab_format', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      vstest.compareImage()
   }

   /**
    * check tab, tab[Active Tab] format in embedded vs
    */
   def 'Embedded vs_tab'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Others/Embedded vs_tab', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])

      expect:
      vstest.compareImage()
   }

}

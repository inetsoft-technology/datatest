package inetsoft.test.vsexport.cases.printLayout

import inetsoft.test.modules.VSExportTest
import spock.lang.IgnoreRest
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class DataView_Spec extends Specification{
   static VSExportTest vsExportTest
   static String caseName

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check crosstab: visible, Dynimac binding, drill down/up, hide col, DC.
    */
   def 'Data_Crosstab1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^PrintLayout/Data/crosstab1' , caseName)
      vsExportTest.testExportWithPrintLayout(['(Home)', 'bk1', 'bk2', 'bk3'] as String[], null)

      expect:
      vsExportTest.comparePNG()
   }
}

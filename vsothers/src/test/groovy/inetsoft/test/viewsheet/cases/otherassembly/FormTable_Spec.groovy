package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.VSFormImportTest
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

class FormTable_Spec extends Specification {
   static VSFormImportTest vsFormImportTest
   static String caseName

   def setupSpec() {
      VSFormImportTest.initHome(this.class.getName())
   }

   /**
    * check delete row(first,middle,last) and table header in embedded form table
    * date/time format changed if header is deleted, ignore
    */
   def 'delete row and header in embedded form table' () {
      caseName = specificationContext.currentIteration.name
      vsFormImportTest = new VSFormImportTest('1^128^__NULL__^FormTable/Embedded Form', caseName)
      vsFormImportTest.importXLSToVS("Embedded_Form_Delete_Row.xlsx")

      expect:
      vsFormImportTest.compareImage()
   }

   /**
    * check add and modify rows in embedded form table
    */
    @IgnoreRest
   def 'add and modify in embedded form table' () {
      caseName = specificationContext.currentIteration.name
      vsFormImportTest = new VSFormImportTest('1^128^__NULL__^FormTable/Embedded Form', caseName)
      vsFormImportTest.importXLSToVS("Embedded_Form_Add_Modify_Row.xlsx")

      expect:
      vsFormImportTest.compareImage()
   }

   /**
    * check highlight and condition not apply to un-submitted rows
    */
   def 'un-submitted changes' () {
      caseName = specificationContext.currentIteration.name
      vsFormImportTest = new VSFormImportTest('1^128^__NULL__^FormTable/Form2', caseName)
      vsFormImportTest.importXLSToVS("Embedded_Form_Add_Modify_Row.xlsx")

      expect:
      vsFormImportTest.compareImage()
   }
}

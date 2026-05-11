package inetsoft.test.viewsheet.cases.otherassembly

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Ignore
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

/**
 * Note: all case will store a exp png, use to compare after case failed. all case be executed by E2E
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class Filter_Scenario_Spec extends Specification {
   static ViewsheetTest vstest
   static String caseName
   def setupSpec() {
      ViewsheetTest.initHome(this.class.getPackage().getName())
   }

   /**
    * check two Selection list relation on single and multi status
    */
   def 'R_S_Scenario1' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Scenario/R_S_Scenario1', caseName)
      vstest.executeVS(null, ['bk1', 'bk2', 'bk3', 'bk4', 'bk5', 'bk6'] as String[])

      expect:
      verifyAll {
         vstest.compareData()
      }
   }

   /**
    * check Selection list and Selection tree relation on single and multi status
    */
   def 'R_S_Scenario2' () {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Relation/Scenario/R_S_Scenario2', caseName)
      vstest.executeVS(null, ['bk1', 'bk1-1', 'bk2', 'bk2-1', 'bk3', 'bk4', 'bk5', 'bk6', 'bk7'] as String[])

      expect:
      verifyAll {
         vstest.compareData()
      }
   }
}

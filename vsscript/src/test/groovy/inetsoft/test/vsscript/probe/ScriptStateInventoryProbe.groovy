package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE: which viewsheets of the test storage hold which assembly types (TextInput, CalcTable, embedded
 * Viewsheet, outputs with highlights), to pick assets for the port-gap sweep (#81430). Prints
 * "PROBE INV <viewsheet> <type>=<count>,..." for the types of interest. Not a case (no _Spec suffix).
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest=ScriptStateInventoryProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateInventoryProbe extends Specification {
   static final List WANTED = ['TextInputVSAssembly', 'CalcTableVSAssembly', 'ViewsheetVSAssembly', 'TextVSAssembly',
                               'GaugeVSAssembly', 'ComboBoxVSAssembly']

   def "inventory"() {
      expect:
      ScriptStateBase.viewsheetIds().each { id ->
         try {
            def t = new ScriptStateBase(id)
            def counts = [:]
            t.vs.getAssemblies().each { a ->
               def n = a.class.simpleName
               if(WANTED.contains(n)) { counts[n] = (counts[n] ?: 0) + 1 }
            }
            if(counts) { println "PROBE INV ${id} ${counts.collect { k, v -> k - 'VSAssembly' + '=' + v }.join(',')}" }
         }
         catch(Throwable e) {
            println "PROBE INV ${id} OPEN-FAILED ${e.class.simpleName}"
         }
      }
      true
   }
}

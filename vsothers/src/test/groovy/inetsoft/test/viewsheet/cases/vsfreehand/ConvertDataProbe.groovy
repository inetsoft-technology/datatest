package inetsoft.test.viewsheet.cases.vsfreehand

import inetsoft.test.modules.VSCalcTest
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

/**
 * EXPLORATION ONLY (G2), not part of the CI run (name does not end with Spec).
 * Run: ./mvnw test -pl vsothers -Pvsfreehand -Dtest=ConvertDataProbe
 * For every ConvertToCalc viewsheet: compare the data of each table/crosstab before and after
 * convert to freehand. Nothing is asserted; the result is written to
 * vsothers/target/g2_convert_probe.txt.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ConvertDataProbe extends Specification {
   static final List<String> VSS = [
      'ConvertToCalc/Crosstabs/CroTable1', 'ConvertToCalc/Crosstabs/CroTable2',
      'ConvertToCalc/Crosstabs/CroTable3', 'ConvertToCalc/Crosstabs/CroTable4',
      'ConvertToCalc/Crosstabs/CroTable5', 'ConvertToCalc/Crosstabs/CroTable6',
      'ConvertToCalc/Crosstabs/CroTable7', 'ConvertToCalc/Crosstabs/CroTable8',
      'ConvertToCalc/Crosstabs/DCPTrendTable1', 'ConvertToCalc/Crosstabs/DrillCroTable1',
      'ConvertToCalc/Crosstabs/PdateManulCroTable1', 'ConvertToCalc/Crosstabs/PercentFormat',
      'ConvertToCalc/GroupAndTab',
      'ConvertToCalc/Tables/CTable1', 'ConvertToCalc/Tables/CTable2',
      'ConvertToCalc/Tables/ComTable1', 'ConvertToCalc/Tables/ComTable2',
      'ConvertToCalc/Tables/ETable1', 'ConvertToCalc/VSScriptEffect']

   def setupSpec() {
      VSCalcTest.initHome(this.class.getName())
   }

   def 'probe'() {
      def lines = []

      [true, false].each { confirmed ->
         VSS.each { vs ->
            String name = vs.substring(vs.lastIndexOf('/') + 1)

            try {
               def test = new VSCalcTest('1^128^__NULL__^' + vs, 'probe_' + confirmed + '_' + name)
               Map<String, String> res = test.compareConvertData(null, confirmed)

               if(res.isEmpty()) {
                  lines << "confirmed=${confirmed} ${vs}: NO-ASSEMBLY".toString()
               }

               res.each { k, v -> lines << "confirmed=${confirmed} ${vs} / ${k}: ${v}".toString() }
            }
            catch(Throwable t) {
               lines << "confirmed=${confirmed} ${vs}: PROBE-FAILED ${t}".toString()
            }
         }
      }

      new File('target/g2_convert_probe.txt').text = lines.join('\n')

      expect:
      true
   }
}

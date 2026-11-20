package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.test.vsscript.support.XlsxText
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/** PROBE, NOT part of the suite: what an Excel export of a viewsheet holds (-Dprobe.vs, optional -Dprobe.script=Assembly=script) */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateExcelProbe extends Specification {
   def 'excel'() {
      when:
      def t = new ScriptStateBase(System.getProperty('probe.vs'))
      def s = System.getProperty('probe.script')

      if(s) {
         def (a, sc) = s.split('=', 2)
         t.setScript(a, sc)
      }

      t.vs.getAssemblies().each { a -> println "PROBE asm ${a.name} ${a.getClass().simpleName} visible=${t.eval(a.name + '.visible')} script=${(a.getVSAssemblyInfo().script ?: '').take(60).replaceAll(/\s+/, ' ')}" }
      def file = t.exportFile(FileFormatInfo.EXPORT_TYPE_EXCEL, '.xlsx')

      XlsxText.read(file).each { name, rows ->
         println "PROBE sheet ${name} rows=${rows.size()}"
         rows.each { n, cells -> println "PROBE  r${n}: ${cells.take(12)}" }
      }

      then:
      true

      cleanup:
      file?.delete()
   }
}

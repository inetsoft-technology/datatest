package inetsoft.test.vsscript.probe

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.VSAssembly
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * PROBE, NOT part of the suite (no _Spec suffix). Two helpers for the manual cases (docs/test-gap-assessment/vsscript-manual-automation.md):
 * dump_manual_scripts writes the scripts of every viewsheet of the Manual folder to -Dprobe.out, export_xml writes the viewsheet XML and
 * the base worksheet XML of -Dprobe.vs (for example OtherScript/scopePara1) to -Dprobe.dir, so that a viewsheet can be run on the 13.8 build.
 *
 * Run: ./mvnw -o test -pl vsscript -Dtest='ManualDumpProbe#dump_manual_scripts' -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dprobe.out=<file>
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ManualDumpProbe extends Specification {
   def 'dump_manual_scripts'() {
      when:
      def out = new File(System.getProperty('probe.out', 'manual-dump.txt'))
      out.text = ''
      def ids = ScriptStateBase.viewsheetIds().findAll { it.contains('^Manual/') }

      ids.each { id ->
         out << "##### ${id}\n"

         try {
            def t = new ScriptStateBase(id)
            def info = t.vs.getViewsheetInfo()
            out << "onInit: ${info.getOnInit()}\n"
            out << "onLoad: ${info.getOnLoad()}\n"

            t.vs.getAssemblies().each { VSAssembly a ->
               def ai = a.getVSAssemblyInfo()
               out << "- ${a.name} (${a.class.simpleName}) visible=${ai.getVisibleValue()}\n"

               if(ai.getScript()?.trim()) {
                  out << "  script: ${ai.getScript().replace('\n', '\n          ')}\n"
               }
            }
         }
         catch(Throwable e) {
            out << "ERROR ${e.class.simpleName}: ${e.message}\n"
         }
      }


      then:
      true
   }

   /** writes the viewsheet XML and the base worksheet XML of one Manual viewsheet, for the 13.8 harness: -Dprobe.vs=OtherScript/scopePara1 -Dprobe.dir=<dir> */
   def 'export_xml'() {
      when:
      def path = System.getProperty('probe.vs')
      def dir = new File(System.getProperty('probe.dir', '.'))
      def t = path ? new ScriptStateBase("1^128^__NULL__^Manual/${path}") : null

      if(t != null) {
         def vsw = new StringWriter(); def pw = new PrintWriter(vsw); t.vs.writeXML(pw); pw.flush()
         new File(dir, 'vs.xml').setText(vsw.toString(), 'UTF-8')
         def ws = t.vs.getBaseWorksheet()
         def wsw = new StringWriter(); def pw2 = new PrintWriter(wsw); ws?.writeXML(pw2); pw2.flush()
         new File(dir, 'ws.xml').setText(wsw.toString(), 'UTF-8')
      }

      then:
      true
   }
}

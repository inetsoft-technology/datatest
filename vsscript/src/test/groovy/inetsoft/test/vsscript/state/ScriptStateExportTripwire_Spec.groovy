package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.web.viewsheet.service.ExportResponse
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Export path of the scripts. The viewer refreshes a viewsheet and swallows a script error (ScriptStateRefreshTripwire_Spec), but the
 * export runs the component scripts again and a script error stops it: Bug #81796, "a script error in any component script makes the
 * export answer HTTP 500 with no file". This opens every viewsheet of the test storage, exports it to Excel and PDF through the real
 * export service, and fails on an export that throws and is not in the list below, or on a listed one that no longer throws (then the
 * list or the bug is out of date). The control is a component script with a syntax error.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateExportTripwire_Spec extends Specification {
   static final Map FORMATS = [EXCEL: FileFormatInfo.EXPORT_TYPE_EXCEL, PDF: FileFormatInfo.EXPORT_TYPE_PDF]

   /**
    * Viewsheets whose export throws today, with the reason. Both read parameter.state in a component script
    * (Text1.value = parameter.state.toString()) and the export has no such parameter, so the script throws a TypeError, the case of #81796.
    */
   static final Map EXPECTED = [
      'Manual/OtherScript/Script_Order_OneWay4'      : 'TypeError: Cannot read property \'toString\' of undefined',
      'ScriptAuto/ScriptEngine/Script_Order_OneWay4': 'TypeError: Cannot read property \'toString\' of undefined'
   ]

   /** @return null when the export worked, else the innermost message */
   static String export(ScriptStateBase t, int format) {
      def out = new ByteArrayOutputStream()

      try {
         t.res.exportVS(format, true, false, false, false, false, ['(Home)'] as String[], false, false, null, new ExportResponse(out), t.principal)
         return out.size() > 0 ? null : 'empty file'
      }
      catch(Throwable e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) c = c.cause
         return "${c.class.simpleName}: ${c.message}".replaceAll(/\s+/, ' ')
      }
   }

   def 'a_script_error_stops_the_export_Bug_81796'() {
      // control, Bug #81796: a component script that does not compile makes the export fail, whatever the format.
      // When the bug is fixed this case turns red: change the expectation to "the export works" and empty EXPECTED if it is not needed.
      given:
      def t = new ScriptStateBase('1^128^__NULL__^Manual/Chart/TestCase_DrillEnabled')
      def healthy = FORMATS.collectEntries { n, f -> [(n): export(t, f)] }

      when:
      t.setScript('Text1', 'var = ;')
      def broken = FORMATS.collectEntries { n, f -> [(n): export(t, f)] }

      then: 'the untouched viewsheet exports'
      healthy.values().every { it == null }

      and: 'the broken one does not, and the message names the syntax error'
      broken.values().every { it != null && it.contains('SyntaxError') }
   }

   def 'every_viewsheet_exports_to_excel_and_pdf_except_the_listed'() {
      when:
      def failures = [:]

      ScriptStateBase.viewsheetIds().each { id ->
         def name = id.replaceAll(/^1\^128\^__NULL__\^/, '').replaceAll(/\^host-org$/, '')
         def t

         try {
            t = new ScriptStateBase(id)
         }
         catch(Throwable e) {
            failures[name] = "open: ${e.class.simpleName}"
            return
         }

         FORMATS.each { n, f ->
            def r = export(t, f)

            if(r != null) {
               failures["${name} (${n})".toString()] = r
            }
         }
      }

      def unexpected = failures.findAll { k, v -> !EXPECTED.keySet().any { k.startsWith(it) } || !EXPECTED.find { n, m -> k.startsWith(n) }.value.with { m -> v.contains(m) } }
      def gone = EXPECTED.keySet().findAll { n -> !failures.keySet().any { it.startsWith(n) } }

      then:
      unexpected == [:]
      gone.isEmpty()
   }
}

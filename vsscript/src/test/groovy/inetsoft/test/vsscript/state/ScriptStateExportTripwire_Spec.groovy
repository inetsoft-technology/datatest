package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.vsscript.support.XlsxText
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.web.viewsheet.service.ExportResponse
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Export path of the scripts. The viewer refreshes a viewsheet and swallows a script error (ScriptStateRefreshTripwire_Spec), and the
 * export runs the component scripts again. Bug #81796 ("a script error in any component script makes the export answer HTTP 500 with
 * no file") is fixed by PR #8467: while an export runs, a failing component script is skipped and logged, and the exported document
 * carries a warning text (vs.export.componentScriptFailed). This opens every viewsheet of the test storage, exports it to Excel and PDF
 * through the real export service, and fails on an export that throws. The viewsheets in SKIPPED threw before the fix; they must now
 * export and carry the warning. The control is a component script with a syntax error, exported to Excel, PDF and HTML.
 *
 * Stopped scripts (timeout) and table load failures still fail an export by design; they are covered by the Java tests of #8467
 * (AbstractVSExporterScriptFailure*Test), not here.
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
   static final Map FORMATS_WITH_HTML = FORMATS + [HTML: FileFormatInfo.EXPORT_TYPE_HTML]
   static final Map SUFFIX = [EXCEL: '.xlsx', PDF: '.pdf', HTML: '.html']
   static final String PFX = '1^128^__NULL__^'

   /** a part of vs.export.componentScriptFailed ("A component script failed and was skipped, so the exported content may differ ...") */
   static final String WARNING = 'component script failed'

   /**
    * Viewsheets whose export threw before the fix of #81796 (PR #8467). Both read parameter.state in a component script
    * (Text1.value = parameter.state.toString()) and the export has no such parameter, so the script throws a TypeError. The export now
    * skips that script and the document carries the warning.
    */
   static final List<String> SKIPPED = [
      'Manual/OtherScript/Script_Order_OneWay4',
      'ScriptAuto/ScriptEngine/Script_Order_OneWay4'
   ]

   /** @return true when the exported document holds the warning text (Excel: in a text box, not in a cell) */
   static boolean carriesWarning(ScriptStateBase t, String format, int type) {
      def file = t.exportFile(type, SUFFIX[format])

      try {
         def text = format == 'EXCEL' ? (XlsxText.drawingTexts(file) + XlsxText.allTexts(file)).join('\n') :
            format == 'PDF' ? PDDocument.load(file).withCloseable { new PDFTextStripper().getText(it) } :
            file.getText('UTF-8')
         text.replaceAll(/\s+/, ' ').contains(WARNING)
      }
      finally {
         file.delete()
      }
   }

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

   def 'a_script_error_does_not_stop_the_export_and_the_document_carries_a_warning_Bug_81796'() {
      // Bug #81796 (fixed by PR #8467): a component script that does not compile used to make the export fail, whatever the format.
      // Now the script is skipped and the document says so; the untouched viewsheet has no warning (control).
      given:
      def t = new ScriptStateBase(PFX + 'Manual/Chart/TestCase_DrillEnabled')
      def healthyFailure = FORMATS_WITH_HTML.collectEntries { n, f -> [(n): export(t, f)] }
      def healthyWarning = FORMATS_WITH_HTML.collectEntries { n, f -> [(n): carriesWarning(t, n, f)] }

      when:
      t.setScript('Text1', 'var = ;')
      def brokenFailure = FORMATS_WITH_HTML.collectEntries { n, f -> [(n): export(t, f)] }
      def brokenWarning = FORMATS_WITH_HTML.collectEntries { n, f -> [(n): carriesWarning(t, n, f)] }

      then: 'control: the untouched viewsheet exports and has no warning'
      healthyFailure.values().every { it == null }
      healthyWarning.values().every { !it }

      and: 'the one with the failing script exports too, in every format'
      brokenFailure.values().every { it == null }

      and: 'and the document carries the warning, in every format'
      brokenWarning.values().every { it }
   }

   def 'every_viewsheet_exports_to_excel_and_pdf_and_the_skipped_script_ones_carry_the_warning'() {
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

      // the viewsheets whose script error used to stop the export: the export works and the document says a script was skipped
      def notWarned = SKIPPED.collectMany { name ->
         def t = new ScriptStateBase(PFX + name)
         FORMATS.findAll { n, f -> !carriesWarning(t, n, f) }.collect { n, f -> "${name} (${n})".toString() }
      }

      then: 'no viewsheet fails to export (before the fix of #81796 two of them did)'
      failures == [:]

      and: 'the exports of the skipped-script viewsheets carry the warning'
      notWarned == []
   }
}

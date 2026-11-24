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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Failing script on every component type, exported (Bug #81796, PR #8467). The export skips a failing component script and the document carries a
 * warning text; ScriptStateExportTripwire_Spec checks that on a Text. This puts the same failing script on the first component of every type that
 * the test storage has and exports the viewsheet to Excel and PDF through the real export service.
 *
 * It fails on a type whose export fails or whose document has no warning and is not in the lists below, and on a listed one that now works (so the
 * lists can only shrink: remove an entry when its bug is fixed).
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateExportMatrix_Spec extends Specification {
   /** Excel and PDF as in the tripwire, plus the other writers: each has its own code per component type (the SelectionList PDF failure, #82105, was one of them) */
   static final Map FORMATS = [
      EXCEL     : FileFormatInfo.EXPORT_TYPE_EXCEL,
      PDF       : FileFormatInfo.EXPORT_TYPE_PDF,
      POWERPOINT: FileFormatInfo.EXPORT_TYPE_POWERPOINT,
      HTML      : FileFormatInfo.EXPORT_TYPE_HTML,
      PNG       : FileFormatInfo.EXPORT_TYPE_PNG
   ]
   static final Map SUFFIX = [EXCEL: '.xlsx', PDF: '.pdf', POWERPOINT: '.pptx', HTML: '.html', PNG: '.png']
   static final String PFX = ScriptStateExportTripwire_Spec.PFX

   /**
    * component type -> formats whose export still fails with a failing script on it, with the start of the failure.
    * Empty: the SelectionList PDF, HTML and PNG export was the last entry (#82105, fixed by stylebi#8671: the list is formatted even when its
    * script fails, and getRowHeights guards a missing format). A failure that comes back must be fixed, not listed.
    */
   static final Map<String, Map<String, String>> KNOWN_EXPORT_FAILURES = [:]

   /**
    * component types where a failing script already makes the refresh throw, so there is nothing to export (the viewsheet cannot be refreshed).
    * SelectionTree: SelectionTreeVSAQuery.refreshViewSelectionValue0 runs the script outside the paths that swallow it (#82107).
    */
   static final Map<String, String> KNOWN_REFRESH_FAILURES = [
      'SelectionTreeVSAssembly': 'ScriptException'
   ]

   /**
    * component type -> formats whose document has no warning text although the export works (a PNG has no text to read and is not checked). Not traced:
    * the script of a Tab in the PDF and of an embedded Viewsheet component is probably not run by the export, or its warning is not drawn; the exports
    * work, so this is only an inconsistency.
    */
   static final Map<String, List<String>> NO_WARNING = [
      'TabVSAssembly': ['PDF'],
      'Viewsheet'    : ['EXCEL', 'PDF', 'POWERPOINT', 'HTML']
   ]

   /** the text of a pptx: the text runs of its slides */
   static String pptxText(File file) {
      def zip = new java.util.zip.ZipFile(file)

      try {
         zip.entries().findAll { it.name ==~ /ppt\/slides\/[^\/]+\.xml/ }.collect { e ->
            zip.getInputStream(e).getText('UTF-8').replaceAll(/<[^>]+>/, ' ')
         }.join(' ')
      }
      finally {
         zip.close()
      }
   }

   /**
    * @return [failure: null or the innermost message, warning: whether the document carries the warning text, null for a PNG (an image has no text to read)]
    */
   static Map export(ScriptStateBase t, String format) {
      File file = null

      try {
         file = t.exportFile(FORMATS[format], SUFFIX[format])

         if(file.length() == 0) {
            return [failure: 'empty file', warning: false]
         }

         def text = format == 'EXCEL' ? (XlsxText.drawingTexts(file) + XlsxText.allTexts(file)).join(' ') :
            format == 'PDF' ? PDDocument.load(file).withCloseable { new PDFTextStripper().getText(it) } :
            format == 'POWERPOINT' ? pptxText(file) :
            format == 'HTML' ? file.getText('UTF-8') : null
         return [failure: null, warning: text == null ? null : text.replaceAll(/\s+/, ' ').contains(ScriptStateExportTripwire_Spec.WARNING)]
      }
      catch(Throwable e) {
         Throwable c = e
         while(c.cause != null && c.cause != c) c = c.cause
         return [failure: "${c.class.simpleName}: ${c.message}".replaceAll(/\s+/, ' ').take(120), warning: false]
      }
      finally {
         file?.delete()
      }
   }

   def 'a_failing_script_on_any_component_type_does_not_stop_the_export'() {
      when:
      def seen = [] as Set
      def observed = [:]
      def refreshFailures = [:]

      ScriptStateBase.viewsheetIds().each { id ->
         def t

         try { t = new ScriptStateBase(id) } catch(Throwable e) { return }

         t.vs.getAssemblies().each { a ->
            def type = a.class.simpleName

            if(a.name.contains('.') || !seen.add(type)) {
               return
            }

            try {
               t.setScript(a.name, 'noSuchFunction123();')
            }
            catch(Throwable e) {
               Throwable c = e
               while(c.cause != null && c.cause != c) c = c.cause
               refreshFailures[type] = c.class.simpleName
               try { t.setScript(a.name, '') } catch(Throwable ignore) {}
               return
            }

            observed[type] = FORMATS.keySet().collectEntries { n -> [(n): export(t, n)] }
            // clear the failing script before moving to the next type on the same viewsheet, so later
            // exports in this loop are not exported alongside an earlier type's unrelated failing script
            try { t.setScript(a.name, '') } catch(Throwable ignore) {}
         }
      }

      def exportFailures = [:]
      def unexpectedFailures = []
      def noWarning = [:]
      def unexpectedNoWarning = []

      observed.each { type, byFormat ->
         byFormat.each { n, r ->
            if(System.getProperty('matrix.print')) {
               println "MATRIX ${type} ${n} failure=${r.failure} warning=${r.warning}"
            }

            if(r.failure != null) {
               exportFailures.get(type, [:])[n] = r.failure

               if(!KNOWN_EXPORT_FAILURES[type]?.containsKey(n)) {
                  unexpectedFailures << "${type} ${n}: ${r.failure}".toString()
               }
            }
            else if(r.warning == false) {
               noWarning.get(type, []) << n

               if(!NO_WARNING[type]?.contains(n)) {
                  unexpectedNoWarning << "${type} ${n}".toString()
               }
            }
         }
      }

      refreshFailures.each { type, cls ->
         if(System.getProperty('matrix.print')) {
            println "MATRIX ${type} refresh fails with ${cls}"
         }
      }

      def failuresGone = KNOWN_EXPORT_FAILURES.findAll { type, byFormat -> byFormat.keySet().any { !(exportFailures[type]?.containsKey(it)) } }.keySet().sort()
      def refreshGone = KNOWN_REFRESH_FAILURES.keySet().findAll { !refreshFailures.containsKey(it) }.sort()
      def refreshUnexpected = refreshFailures.keySet().findAll { !KNOWN_REFRESH_FAILURES.containsKey(it) }.sort()
      def noWarningGone = NO_WARNING.findAll { type, formats -> formats.any { !(noWarning[type]?.contains(it)) } }.keySet().sort()

      then: 'the matrix covers the types of the storage (a vacuous run would pass)'
      observed.size() + refreshFailures.size() >= 20

      and: 'no export fails except the listed ones'
      unexpectedFailures == []

      and: 'a listed failure still occurs (else remove it from KNOWN_EXPORT_FAILURES)'
      failuresGone == []

      and: 'no failing script stops the refresh except the listed types'
      refreshUnexpected == []
      refreshGone == []

      and: 'every working export carries the warning, except the listed ones'
      unexpectedNoWarning == []
      noWarningGone == []
   }
}

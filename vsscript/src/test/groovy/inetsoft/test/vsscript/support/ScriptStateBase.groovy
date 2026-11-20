package inetsoft.test.vsscript.support

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.script.viewsheet.ViewsheetScope
import inetsoft.sree.security.SRPrincipal
import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.core.DatatestSpringRuntimeInitializer
import inetsoft.test.core.RuntimeViewsheetResource
import inetsoft.test.core.TUtil
import inetsoft.uql.viewsheet.VSAssembly
import inetsoft.uql.viewsheet.Viewsheet
import inetsoft.util.DataSpace
import inetsoft.util.IndexedStorage
import inetsoft.util.ThreadContext
import inetsoft.util.ConfigurationContext
import inetsoft.report.composition.ChangedAssemblyList
import inetsoft.test.core.MessageTestUtils
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.viewsheet.service.ExportResponse
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Spike harness: open a runtime viewsheet, apply scripts, read state back. No PNG export.
 */
class ScriptStateBase {
   /** link uri argument the services expect; no request is made to it */
   static final String BASE_URL = 'http://localhost:8080/sree'

   SRPrincipal principal
   RuntimeViewsheetResource res
   RuntimeViewsheet rvs

   /** @param parameters the viewsheet parameters of the open request (the URL query), or null */
   ScriptStateBase(String assetId, Map<String, String[]> parameters = null) {
      DatatestSpringRuntimeInitializer.ensureInitialized(System.getProperty('sree.home', '.'))
      DataSpace.getDataSpace()
      principal = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      ThreadContext.setContextPrincipal(principal)
      res = new RuntimeViewsheetResource(new ActionEventsUtil().createOpenViewsheetEvent(parameters, assetId))
      res.initRuntimeVS(principal)
      rvs = res.getRuntimeViewsheet(principal)
   }

   Viewsheet getVs() { rvs.getViewsheet() }

   String getRuntimeId() { res.@runtimeId }

   /** set the script of one assembly, then refresh so it runs */
   void setScript(String assembly, String script) {
      ((VSAssembly) vs.getAssembly(assembly)).getVSAssemblyInfo().setScript(script)
      res.refreshViewsheet(principal)
   }

   void setOnInit(String script) {
      vs.getViewsheetInfo().setOnInit(script)
      res.refreshViewsheet(principal)
   }

   /**
    * export the viewsheet through the real export service (the one the viewer uses) into a temporary file
    * @param format a FileFormatInfo.EXPORT_TYPE_* value
    * @return the file; the caller deletes it
    */
   File exportFile(int format, String suffix) {
      def file = File.createTempFile('vsscript-export', suffix)
      file.withOutputStream { out ->
         res.exportVS(format, true, false, false, false, false, ['(Home)'] as String[], false, false, null, new ExportResponse(out), principal)
      }
      file
   }

   ViewsheetScope getScope() { rvs.getViewsheetSandbox().get().getScope() }

   /** refresh the viewsheet with a recording dispatcher, so commands sent during refresh can be inspected */
   RecordingDispatcher.Recorder refreshRecorded() {
      def rec = RecordingDispatcher.create(principal)
      def svc = ConfigurationContext.getContext().getSpringBean(CoreLifecycleService)
      MessageTestUtils.withMockMessageContext(principal, runtimeId) {
         svc.refreshViewsheet(rvs, runtimeId, null, rec, true, true, true, new ChangedAssemblyList(true))
      }
      rec
   }

   /** evaluate a script expression and return the raw (not unwrapped) result */
   Object evalRaw(String expr) {
      scope.execute(expr, ViewsheetScope.VIEWSHEET_SCRIPTABLE)
   }

   /** evaluate a script expression in the viewsheet scope and return the unwrapped value */
   Object eval(String expr) {
      def r = scope.execute(expr, ViewsheetScope.VIEWSHEET_SCRIPTABLE)
      return inetsoft.util.script.JavaScriptEngine.unwrap(r)
   }

   /** ids of all viewsheets in the test storage (type 128); the storage keys end in ^host-org, which the specs leave out */
   static List<String> viewsheetIds() {
      IndexedStorage.getIndexedStorage().getKeys(null).findAll { it.startsWith('1^128^__NULL__^') }
         .collect { it.replaceAll(/\^host-org$/, '') }.unique().sort()
   }

   /**
    * Paint the real VGraph of a chart into an image and count pixels. paintGraph is called directly because
    * ChartPainter swallows paint exceptions. The wait for the graph is bounded, so that a stuck initialisation
    * fails the test instead of hanging the whole module.
    * @return failure (what paintGraph threw, or null), painted (non-transparent pixels), magenta (pixels of 0xff00ff),
    *         pairFailed (the graph pair itself failed)
    */
   Map paint(String chart) {
      def pair = rvs.getViewsheetSandbox().get().getVGraphPair(chart)
      def user = principal
      CompletableFuture.runAsync { ThreadContext.setContextPrincipal(user); pair.waitInit() }.get(120, TimeUnit.SECONDS)
      def vgraph = pair.getExpandedVGraph()

      if(vgraph == null || pair.isFailed()) {
         // report it as the failure of the case instead of failing later with a NullPointerException
         return [failure: pair.getFailure() ?: new IllegalStateException("no graph was created for ${chart}"),
                 painted: 0, magenta: 0, pairFailed: pair.isFailed()]
      }

      def img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
      def g = img.createGraphics()
      Throwable failure = null

      try {
         vgraph.paintGraph(g, true)
      }
      catch(Throwable e) {
         failure = e
      }
      finally {
         g.dispose()
      }

      int magenta = 0
      int painted = 0

      for(int x = 0; x < img.width; x++) {
         for(int y = 0; y < img.height; y++) {
            int argb = img.getRGB(x, y)

            if((argb >>> 24) != 0) {
               painted++

               if((argb & 0xffffff) == 0xff00ff) {
                  magenta++
               }
            }
         }
      }

      [failure: failure, painted: painted, magenta: magenta, pairFailed: pair.isFailed()]
   }
}

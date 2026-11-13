package inetsoft.test.vsscript.state

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
import inetsoft.util.ThreadContext

/**
 * Spike harness: open a runtime viewsheet, apply scripts, read state back. No PNG export.
 */
class ScriptStateBase {
   SRPrincipal principal
   RuntimeViewsheetResource res
   RuntimeViewsheet rvs

   ScriptStateBase(String assetId) {
      DatatestSpringRuntimeInitializer.ensureInitialized(System.getProperty('sree.home', '.'))
      DataSpace.getDataSpace()
      principal = TUtil.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      ThreadContext.setContextPrincipal(principal)
      res = new RuntimeViewsheetResource(new ActionEventsUtil().createOpenViewsheetEvent(null, assetId))
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

   ViewsheetScope getScope() { rvs.getViewsheetSandbox().get().getScope() }

   /** evaluate a script expression in the viewsheet scope and return the unwrapped value */
   Object eval(String expr) {
      def r = scope.execute(expr, ViewsheetScope.VIEWSHEET_SCRIPTABLE)
      return inetsoft.util.script.JavaScriptEngine.unwrap(r)
   }
}

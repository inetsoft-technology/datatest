package inetsoft.test.core;

import inetsoft.analytic.composition.ViewsheetService;
import inetsoft.report.composition.ChangedAssemblyList;
import inetsoft.report.composition.RuntimeViewsheet;
import inetsoft.report.composition.execution.ViewsheetSandbox;
import inetsoft.report.internal.table.FormatTableLens2;
import inetsoft.report.io.csv.CSVConfig;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.uql.asset.Assembly;
import inetsoft.uql.viewsheet.TableVSAssembly;
import inetsoft.uql.viewsheet.Viewsheet;
import inetsoft.uql.viewsheet.internal.TableVSAssemblyInfo;
import inetsoft.util.ConfigurationContext;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService;
import inetsoft.web.composer.vs.objects.event.ConvertToFreehandTableEvent;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.event.OpenViewsheetEvent;
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent;
import inetsoft.web.viewsheet.event.chart.VSChartShowDetailsEvent;
import inetsoft.web.viewsheet.controller.OpenViewsheetController;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsService;
import inetsoft.web.viewsheet.controller.chart.VSChartBrushService;
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef;
import inetsoft.web.viewsheet.service.CoreLifecycleService;
import inetsoft.web.viewsheet.service.CommandDispatcher;
import inetsoft.web.viewsheet.service.ExportResponse;
import inetsoft.web.viewsheet.service.VSExportService;

import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Optional;

public class RuntimeViewsheetResource {
   public RuntimeViewsheetResource(OpenViewsheetEvent openViewsheetEvent) {
      this.openViewsheetEvent = openViewsheetEvent;
   }
   
   public void initRuntimeVS(Principal principal) {
      runtimeId = MessageTestUtils.withMockMessageContext(principal, null, openViewsheetEvent,
              (ctx, event) -> openViewsheet(ctx, event));
   }
   
   private String openViewsheet(MessageTestUtils.MessageContext ctx, OpenViewsheetEvent openViewsheetEvent) {
      try {
         openViewsheetController().openViewsheet(
                 openViewsheetEvent, ctx.getUser(), ctx.getCommandDispatcher(),
                 "http://localhost:8080/sree");
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to open viewsheet", e);
      }
      return runtimeViewsheetRef().getRuntimeId();
   }
   
   public RuntimeViewsheet getRuntimeViewsheet(Principal principal) {
      try {
         return runtimeId == null ?
                 null : viewsheetService().getViewsheet(runtimeId, principal);
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to get runtime viewsheet", e);
      }
   }

   /**
    * Runtime viewsheet id from the last {@link #initRuntimeVS}. For tests that must set
    * {@code MessageContextHolder} (e.g. form import + PNG); not used by general export paths.
    */
   public String getRuntimeId() {
      return runtimeId;
   }
   
   public void exportVS(int format, boolean match, boolean expandSelections, boolean current,
                        boolean previewPrintLayout, boolean print, String[] bookmarks,
                        boolean embedded, boolean onlyDataComponents, CSVConfig csvConfig, ExportResponse response, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      try {
         vsExportService().exportViewsheet(runtimeViewsheet, format, match,
                 expandSelections, current, previewPrintLayout, print, bookmarks, embedded, onlyDataComponents, csvConfig, response, principal);
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to export viewsheet", e);
      }
   }
   
   /**
    * convert a table|crosstab to freehand
    *
    * @param principal
    * @param aname
    */
   public void convertToFreehand(SRPrincipal principal, String aname) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      ConvertToFreehandTableEvent cevent = new ConvertToFreehandTableEvent();
      cevent.setName(aname);
      cevent.setConfirmed(true);
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      try {
         composerVSTableService().convertToFreehandTable(runtimeId, cevent, principal,
                 "http://localhost:8080/sree", commandDispatcher);
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to convert freehand", e);
      }
   }
   
   public void refreshViewsheet(SRPrincipal principal) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      try {
         coreLifecycleService().refreshViewsheet(runtimeViewsheet, runtimeId, null,
                 commandDispatcher, true, true, true, new ChangedAssemblyList(true));
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
   
   /**
    * import excel to vs
    *
    * @param principal
    * @param multipartFile
    */
   public void processImportXLS(SRPrincipal principal, MultipartFile multipartFile) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      ImportXLSController importXLSController = importXLSController();
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      try {
         importXLSController.uploadExcelFile(runtimeId, "xlsx", multipartFile, principal);
         // processXLSUpload reads runtimeViewsheetRef.getRuntimeId() from MessageContextHolder,
         // which is cleared after initRuntimeVS(). Must restore the context with the correct runtimeId.
         MessageTestUtils.withMockMessageContext(principal, runtimeId, (Runnable) () -> {
            try {
               importXLSController.processXLSUpload("xlsx", "http://localhost:8080/sree",
                       principal, commandDispatcher);
            }
            catch(Exception e) {
               throw new RuntimeException("Failed to process XLS import", e);
            }
         });
         // After import updates fmap in-place, dmap.VSTABLE holds stale VSTableLens with
         // pre-computed filter state. Protect fmap via addScriptChangedForm so syncFormData
         // skips fmap.remove during reset, then reset to clear dmap so export rebuilds
         // fresh VSTableLens from the updated fmap.
         Optional<ViewsheetSandbox> sandboxOpt = runtimeViewsheet.getViewsheetSandbox();
         if(sandboxOpt.isPresent()) {
            ViewsheetSandbox sandbox = sandboxOpt.get();
            Viewsheet vs = runtimeViewsheet.getViewsheet();
            for(Assembly assembly : vs.getAssemblies()) {
               if(assembly instanceof TableVSAssembly) {
                  TableVSAssembly tableAssembly = (TableVSAssembly) assembly;
                  TableVSAssemblyInfo info = (TableVSAssemblyInfo) tableAssembly.getVSAssemblyInfo();
                  if(info.isForm()) {
                     sandbox.addScriptChangedForm(tableAssembly.getName());
                  }
               }
            }
            sandbox.reset(new ChangedAssemblyList());
         }
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
   
   /**
    * show detail on chart
    *
    * @param event
    * @param assemblyName
    * @param principal
    */
   public FormatTableLens2 showDetailOnChart(VSChartShowDetailsEvent event, String assemblyName, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      Method method = VSChartShowDetailsService.class.getDeclaredMethod("getShowDetailDatas", VSChartShowDetailsEvent.class,
              RuntimeViewsheet.class, String.class, Principal.class);
      method.setAccessible(true);
      return (FormatTableLens2) method.invoke(vsChartShowDetailsService(), event,
              runtimeViewsheet, assemblyName, principal);
   }
   
   
   /**
    * brush on chart
    */
   public void brushOnChart(VSChartBrushEvent event, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      vsChartBrushService().eventHandler(runtimeId, event,
              "http://localhost:8080/sree", principal, commandDispatcher);
   }
   
   private void closeViewsheet(String runtimeId) {
      if(runtimeId != null) {
         try {
            viewsheetService().closeViewsheet(runtimeId, null);
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }
   }

   private ViewsheetService viewsheetService() {
      return springBean(ViewsheetService.class);
   }

   private OpenViewsheetController openViewsheetController() {
      return springBean(OpenViewsheetController.class);
   }

   private RuntimeViewsheetRef runtimeViewsheetRef() {
      return springBean(RuntimeViewsheetRef.class);
   }

   private VSExportService vsExportService() {
      return springBean(VSExportService.class);
   }

   private CoreLifecycleService coreLifecycleService() {
      return springBean(CoreLifecycleService.class);
   }

   private ImportXLSController importXLSController() {
      return springBean(ImportXLSController.class);
   }

   private ComposerVSTableService composerVSTableService() {
      return springBean(ComposerVSTableService.class);
   }

   private VSChartShowDetailsService vsChartShowDetailsService() {
      return springBean(VSChartShowDetailsService.class);
   }

   private VSChartBrushService vsChartBrushService() {
      return springBean(VSChartBrushService.class);
   }

   private <T> T springBean(Class<T> type) {
      return ConfigurationContext.getContext().getSpringBean(type);
   }
   
   private final OpenViewsheetEvent openViewsheetEvent;
   private RuntimeViewsheet runtimeViewsheet;
   private String runtimeId;
}

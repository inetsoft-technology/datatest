package inetsoft.test.core;

import inetsoft.report.composition.ChangedAssemblyList;
import inetsoft.report.composition.RuntimeViewsheet;
import inetsoft.report.internal.table.FormatTableLens2;
import inetsoft.report.io.csv.CSVConfig;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.web.composer.vs.objects.event.ConvertToFreehandTableEvent;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.event.OpenViewsheetEvent;
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent;
import inetsoft.web.viewsheet.event.chart.VSChartShowDetailsEvent;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsService;
import inetsoft.web.viewsheet.service.CommandDispatcher;
import inetsoft.web.viewsheet.service.ExportResponse;

import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.security.Principal;

public class RuntimeViewsheetResource {
   public RuntimeViewsheetResource(OpenViewsheetEvent openViewsheetEvent,
                                   ControllersResource controllersResource) {
      this.openViewsheetEvent = openViewsheetEvent;
      this.controllersResource = controllersResource;
   }

   public void initRuntimeVS(Principal principal) {
      runtimeId = MessageTestUtils.withMockMessageContext(principal, null, openViewsheetEvent,
              (ctx, event) -> openViewsheet(ctx, event));
   }

   private String openViewsheet(MessageTestUtils.MessageContext ctx, OpenViewsheetEvent openViewsheetEvent) {
      try {
         controllersResource.getOpenViewsheetController().openViewsheet(
                 openViewsheetEvent, ctx.getUser(), ctx.getCommandDispatcher(),
                 "http://localhost:8080/sree");
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Failed to open viewsheet", e);
      }
      return controllersResource.getRuntimeId();
   }

   public RuntimeViewsheet getRuntimeViewsheet(Principal principal) {
      try {
         return runtimeId == null ?
                 null : controllersResource.getViewsheetService().getViewsheet(runtimeId, principal);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Failed to get runtime viewsheet", e);
      }
   }

   public void exportVS(int format, boolean match, boolean expandSelections, boolean current,
                        boolean previewPrintLayout, boolean print, String[] bookmarks,
                        boolean embedded, boolean onlyDataComponents, CSVConfig csvConfig, ExportResponse response, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      try {
         controllersResource.getVSExportService().exportViewsheet(runtimeViewsheet, format, match,
                 expandSelections, current, previewPrintLayout, print, bookmarks, embedded, onlyDataComponents, csvConfig, response, principal);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
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
         controllersResource.getComposerVSTableController().convertToFreehandTable(cevent, principal,
                 "http://localhost:8080/sree", commandDispatcher);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Failed to convert freehand", e);
      }
   }

   public void refreshViewsheet(SRPrincipal principal) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      try {
         controllersResource.getCoreLifecycleService().refreshViewsheet(runtimeViewsheet, runtimeId, null,
                 commandDispatcher, true, true, true, new ChangedAssemblyList(true));
      } catch (Exception e) {
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
      ImportXLSController importXLSController = controllersResource.getImportXLSController();
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      try {
         importXLSController.uploadExcelFile(runtimeId,
                 "xlsx", multipartFile, principal);
         importXLSController.processXLSUpload("xlsx", "http://localhost:8080/sree",
                 principal, commandDispatcher);

      } catch (Exception e) {
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
      return (FormatTableLens2) method.invoke(controllersResource.getVSChartShowDetailsService(), event,
              runtimeViewsheet, assemblyName, principal);
   }


   /**
    * brush on chart
    */
   public void brushOnChart(VSChartBrushEvent event, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      controllersResource.getVSChartBrushService().eventHandler(runtimeId, event,
              "http://localhost:8080/sree", principal, commandDispatcher);
   }

   private void closeViewsheet(String runtimeId) {
      if (runtimeId != null) {
         try {
            controllersResource.getViewsheetService().closeViewsheet(runtimeId, null);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private final OpenViewsheetEvent openViewsheetEvent;
   private final ControllersResource controllersResource;
   private RuntimeViewsheet runtimeViewsheet;
   private String runtimeId;
}

package inetsoft.test.core;

import inetsoft.report.composition.ChangedAssemblyList;
import inetsoft.report.composition.RuntimeViewsheet;
import inetsoft.report.internal.table.FormatTableLens2;
import inetsoft.report.io.csv.CSVConfig;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.web.composer.vs.objects.event.ConvertToFreehandTableEvent;
import inetsoft.web.messaging.MessageAttributes;
import inetsoft.web.viewsheet.command.ViewsheetCommand;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.event.OpenViewsheetEvent;
import inetsoft.web.viewsheet.event.chart.VSChartBrushEvent;
import inetsoft.web.viewsheet.event.chart.VSChartShowDetailsEvent;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsServiceProxy;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsService;
import inetsoft.web.viewsheet.service.CommandDispatcherService;
import inetsoft.web.viewsheet.service.CommandDispatcher;
import inetsoft.web.viewsheet.service.ExportResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.multipart.MultipartFile;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsServiceProxy;

import java.security.Principal;

public class RuntimeViewsheetResource extends MockMessageResource {
   public RuntimeViewsheetResource(OpenViewsheetEvent openViewsheetEvent,
                                    ControllersResource controllersResource){
      this.openViewsheetEvent = openViewsheetEvent;
      this.controllersResource = controllersResource;
   }

   public String getRuntimeId() {
      return runtimeId;
   }

   public void initRuntimeVS(Principal principal) {
      runtimeId = mockMessage(principal, openViewsheetEvent, this::openViewsheet);
   }

   private String openViewsheet(OpenViewsheetEvent openViewsheetEvent) {
      try {
         controllersResource.getOpenViewsheetController().openViewsheet(
               openViewsheetEvent, getHeaderAccessor().getUser(), getCommandDispatcher(),
               "http://localhost:8080/sree");
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to open viewsheet", e);
      }
      return controllersResource.getRuntimeId();
   }

   public RuntimeViewsheet getRuntimeViewsheet(Principal principal) {
      try {
         return runtimeId == null ?
               null : controllersResource.getViewsheetService().getViewsheet(runtimeId, principal);
      }
      catch(RuntimeException e ) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to get runtime viewsheet", e);
      }
   }

   public void exportVS(int format, boolean mactch, boolean expandSelections, boolean current,
                        boolean previewPrintLayout, boolean print, String[] bookmarks,
                        boolean embedded, boolean onlyDataComponents, CSVConfig csvConfig, ExportResponse response, SRPrincipal principal) throws Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      try {
         controllersResource.getVSExportService().exportViewsheet(runtimeViewsheet, format, mactch,
            expandSelections, current, previewPrintLayout, print, bookmarks, embedded, onlyDataComponents, csvConfig, response, principal);
      }
      catch (RuntimeException e) {
         throw e;
      }
      catch (Exception e) {
         throw new RuntimeException("Failed to export viewsheet", e);
      }
   }

   /**
    * convert a table|crosstab to freehand
    * @param principal
    * @param aname
    */
   public void convertToFreehand(SRPrincipal principal, String aname) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      ConvertToFreehandTableEvent cevent = new ConvertToFreehandTableEvent();
      cevent.setName(aname);
      cevent.setConfirmed(true);
      CommandDispatcher commandDispatcher = createCommandDispather();
      try {
         controllersResource.getComposerVSTableController().convertToFreehandTable(cevent, principal,
            "http://localhost:8080/sree", commandDispatcher);
      }
      catch (RuntimeException e) {
         throw e;
      }
      catch (Exception e) {
         throw new RuntimeException("Failed to convert freehand", e);
      }
   }

   public void refreshViewsheet(SRPrincipal principal) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      CommandDispatcher commandDispatcher = createCommandDispather();
      try {
         controllersResource.getCoreLifecycleService().refreshViewsheet(runtimeViewsheet, runtimeId, null,
            commandDispatcher, true, true, true, new ChangedAssemblyList(true));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * import excel to vs
    * @param principal
    * @param multipartFile
    */
   public void processImportXLS(SRPrincipal principal, MultipartFile multipartFile) {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      ImportXLSController importXLSController = controllersResource.getImportXLSController();
      try {
         importXLSController.processGetAssemblyImage(runtimeId,
            "xlsx", multipartFile);
         importXLSController.processXLSUpload("xlsx", "http://localhost:8080/sree",
            principal, commandDispatcher);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * show detail on chart
    * @param event
    * @param principal
    */
   /**
   public FormatTableLens2 showDetailOnChart(VSChartShowDetailsEvent event, String assemblyName, SRPrincipal principal) throws Exception {
    // 获取 runtimeViewsheet
    RuntimeViewsheet runtimeViewsheet = getRuntimeViewsheet(principal);
    
    // 调用 serviceProxy 的方法
    return serviceProxy.getShowDetailDatas(event, runtimeViewsheet, assemblyName, principal);
}  **/


   /**
    * brush on chart
    */
   public void brushOnChart(VSChartBrushEvent event, SRPrincipal principal) throws  Exception {
      runtimeViewsheet = getRuntimeViewsheet(principal);
      controllersResource.getVSChartBrushController().eventHandler(event, null, principal, commandDispatcher);
   }

   private void closeViewsheet(String runtimeId) {
      if(runtimeId != null) {
         try {
            controllersResource.getViewsheetService().closeViewsheet(runtimeId, null);
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }
   }

   private CommandDispatcher createCommandDispather() {
      GenericMessage<String> message = new GenericMessage<>("simulated");
      MessageAttributes messageAttributes = new MessageAttributes(message);
      StompHeaderAccessor headerAccessor = messageAttributes.getHeaderAccessor();
      headerAccessor.setUser(null);
      SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(new MessageChannel() {
         @Override
         public boolean send(Message<?> message) {
            return true;
         }

         @Override
         public boolean send(Message<?> message, long timeout) {
            return true;
         }
      });

      CommandDispatcher dispatcher = new CommandDispatcher(headerAccessor, dispatcherService, null)
      {
         @Override
         public void sendCommand(String assemblyName, ViewsheetCommand command) {
            // NO-OP
         }

         @Override
         public void flush() {
            // NO-OP
         }

         @Override
         public CommandDispatcher detach() {
            return createCommandDispather();
         }
      };
      return dispatcher;
   }

   private final OpenViewsheetEvent openViewsheetEvent;
   private final ControllersResource controllersResource;
   private RuntimeViewsheet runtimeViewsheet;
   private String runtimeId;
   private CommandDispatcher commandDispatcher = createCommandDispather();
   private CommandDispatcherService dispatcherService;
   //private final VSChartShowDetailsServiceProxy serviceProxy;
}

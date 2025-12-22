package inetsoft.test.core;

import inetsoft.report.composition.RuntimeWorksheet;
import inetsoft.report.composition.WorksheetService;
import inetsoft.web.composer.model.ws.ImportCSVDialogModel;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.event.OpenWorksheetEvent;
import inetsoft.web.viewsheet.service.CommandDispatcher;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;

public class RuntimeWorksheetResource {
   public RuntimeWorksheetResource(OpenWorksheetEvent openWorksheetEvent,
                                   ControllersResource controllersResource) {
      this.openWorksheetEvent = openWorksheetEvent;
      this.controllersResource = controllersResource;
   }
   
   public void initRuntimeWS(Principal principal) {
      runtimeId = MessageTestUtils.withMockMessageContext(principal, null, openWorksheetEvent,
              (ctx, event) -> openWorksheet(ctx, event));
   }
   
   private String openWorksheet(MessageTestUtils.MessageContext ctx, OpenWorksheetEvent openWorksheetEvent) {
      try {
         controllersResource.getOpenWorksheetController().openWorksheet(
                 openWorksheetEvent, ctx.getUser(), ctx.getCommandDispatcher());
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to open worksheet", e);
      }
      return controllersResource.getRuntimeId();
   }
   
   public RuntimeWorksheet getRuntimeWorksheet(Principal principal) {
      try {
         if(runtimeId == null) {
            return null;
         }
         WorksheetService worksheetService = controllersResource.getWorksheetService();
         if(worksheetService == null) {
            throw new IllegalStateException("WorksheetService is not initialized. Make sure controllers.initControllers() has been called.");
         }
         return worksheetService.getWorksheet(runtimeId, principal);
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to get runtime worksheet", e);
      }
   }
   
   private void closeWorksheet(String runtimeId) {
      if(runtimeId != null) {
         try {
            controllersResource.getWorksheetService().closeWorksheet(runtimeId, null);
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public HashMap<String, Object> processCSVUpload(ImportCSVDialogModel importCSVDialogModel, MultipartFile multipartFile, Principal principal) throws Exception {
      ImportCSVDialogController importCSVDialogController = controllersResource.getImportCSVDialogController();
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      importCSVDialogController.getUploadFile(multipartFile, runtimeId);
      HashMap<String, Object> result = importCSVDialogController.getPreviewTable(importCSVDialogModel, runtimeId);
      importCSVDialogController.setImportCSVDialogModel(importCSVDialogModel, principal, commandDispatcher);
      return result;
   }
   
   private final OpenWorksheetEvent openWorksheetEvent;
   private final ControllersResource controllersResource;
   private String runtimeId;
}

package inetsoft.test.core;

import inetsoft.report.composition.RuntimeWorksheet;
import inetsoft.report.composition.WorksheetService;
import inetsoft.util.ConfigurationContext;
import inetsoft.web.composer.model.ws.ImportCSVDialogModel;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.event.OpenWorksheetEvent;
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef;
import inetsoft.web.viewsheet.service.CommandDispatcher;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;

public class RuntimeWorksheetResource {
   public RuntimeWorksheetResource(OpenWorksheetEvent openWorksheetEvent) {
      this.openWorksheetEvent = openWorksheetEvent;
   }
   
   public void initRuntimeWS(Principal principal) {
      runtimeId = MessageTestUtils.withMockMessageContext(principal, null, openWorksheetEvent,
              (ctx, event) -> openWorksheet(ctx, event));
   }
   
   private String openWorksheet(MessageTestUtils.MessageContext ctx, OpenWorksheetEvent openWorksheetEvent) {
      try {
         openWorksheetController().openWorksheet(openWorksheetEvent, ctx.getUser(),
                 ctx.getCommandDispatcher());
      }
      catch(RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to open worksheet", e);
      }
      return runtimeViewsheetRef().getRuntimeId();
   }
   
   public RuntimeWorksheet getRuntimeWorksheet(Principal principal) {
      try {
         if(runtimeId == null) {
            return null;
         }
         WorksheetService worksheetService = worksheetService();
         if(worksheetService == null) {
            throw new IllegalStateException("WorksheetService is not initialized.");
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
            worksheetService().closeWorksheet(runtimeId, null);
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public HashMap<String, Object> processCSVUpload(ImportCSVDialogModel importCSVDialogModel, MultipartFile multipartFile, Principal principal) throws Exception {
      ImportCSVDialogController importCSVDialogController = importCSVDialogController();
      CommandDispatcher commandDispatcher = MessageTestUtils.createNoOpCommandDispatcher(principal);
      importCSVDialogController.getUploadFile(multipartFile, runtimeId, principal);
      HashMap<String, Object> result =
         importCSVDialogController.getPreviewTable(importCSVDialogModel, runtimeId, principal);
      importCSVDialogController.setImportCSVDialogModel(importCSVDialogModel, principal, commandDispatcher);
      return result;
   }

   private OpenWorksheetController openWorksheetController() {
      return springBean(OpenWorksheetController.class);
   }

   private RuntimeViewsheetRef runtimeViewsheetRef() {
      return springBean(RuntimeViewsheetRef.class);
   }

   private WorksheetService worksheetService() {
      return springBean(WorksheetService.class);
   }

   private ImportCSVDialogController importCSVDialogController() {
      return springBean(ImportCSVDialogController.class);
   }

   private <T> T springBean(Class<T> type) {
      return ConfigurationContext.getContext().getSpringBean(type);
   }
   
   private final OpenWorksheetEvent openWorksheetEvent;
   private String runtimeId;
}

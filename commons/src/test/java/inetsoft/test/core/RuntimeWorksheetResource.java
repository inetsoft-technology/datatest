package inetsoft.test.core;

import inetsoft.report.composition.RuntimeWorksheet;
import inetsoft.report.composition.WorksheetService;
import inetsoft.web.composer.model.ws.ImportCSVDialogModel;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.event.OpenWorksheetEvent;
import inetsoft.web.messaging.MessageAttributes;
import inetsoft.web.viewsheet.command.ViewsheetCommand;
import inetsoft.web.viewsheet.service.CommandDispatcher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.multipart.MultipartFile;
import inetsoft.web.viewsheet.service.CommandDispatcherService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class RuntimeWorksheetResource extends MockMessageResource {
   public RuntimeWorksheetResource(OpenWorksheetEvent openWorksheetEvent,
                                   ControllersResource controllersResource) {
      this.openWorksheetEvent = openWorksheetEvent;
      this.controllersResource = controllersResource;
   }

   public String getRuntimeId() {
      return  runtimeId;
   }

   public void initRuntimeWS(Principal principal) {
      runtimeId = mockMessage(principal, openWorksheetEvent, this::openWorksheet);
   }
   private String openWorksheet(OpenWorksheetEvent openWorksheetEvent) {
      try {
         controllersResource.getOpenWorksheetController().openWorksheet(
            openWorksheetEvent, getHeaderAccessor().getUser(), getCommandDispatcher());
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
      try{
         if (runtimeId == null) {
            return null;
         }
         WorksheetService worksheetService = controllersResource.getWorksheetService();
         if (worksheetService == null) {
            throw new IllegalStateException("WorksheetService is not initialized. Make sure controllers.initControllers() has been called.");
         }
         return worksheetService.getWorksheet(runtimeId, principal);
      }
      catch (RuntimeException e) {
         throw e;
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to get runtime worksheet", e);
      }
   }

   private void closeWorksheet(String runtimeId) {
      if(runtimeId != null) {
         try{
            controllersResource.getWorksheetService().closeWorksheet(runtimeId, null);
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public HashMap<String, Object> processCSVUpload(ImportCSVDialogModel importCSVDialogModel, MultipartFile multipartFile, Principal principal) throws Exception {
      ImportCSVDialogController importCSVDialogController = controllersResource.getImportCSVDialogController();
      CommandDispatcher commandDispatcher = createCommandDispather();
      importCSVDialogController.getUploadFile(multipartFile, runtimeId);
      HashMap<String, Object> result = importCSVDialogController.getPreviewTable(importCSVDialogModel, runtimeId);
      importCSVDialogController.setImportCSVDialogModel(importCSVDialogModel, principal, commandDispatcher);
      return result;
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

      CommandDispatcherService dispatcherService = new CommandDispatcherService(messagingTemplate) {
         @Override
         public void convertAndSendToUser(String user, String destination, Object payload,
                                          Map<String, Object> headers) throws MessagingException
         {
            // NO-OP
         }
      };

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

   private final OpenWorksheetEvent openWorksheetEvent;
   private final ControllersResource controllersResource;
   private String runtimeId;
}

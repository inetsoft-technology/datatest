/*
 * Copyright (c) 2018, InetSoft Technology Corp, All Rights Reserved.
 *
 * The software and information contained herein are copyrighted and
 * proprietary to InetSoft Technology Corp. This software is furnished
 * pursuant to a written license agreement and may be used, copied,
 * transmitted, and stored only in accordance with the terms of such
 * license and with the inclusion of the above copyright notice. Please
 * refer to the file "COPYRIGHT" for further copyright and licensing
 * information. This software and information or any other copies
 * thereof may not be provided or otherwise made available to any other
 * person.
 */
package inetsoft.test.core;

import inetsoft.web.messaging.MessageAttributes;
import inetsoft.web.messaging.MessageContextHolder;
import inetsoft.web.viewsheet.command.ViewsheetCommand;
import inetsoft.web.viewsheet.service.CommandDispatcher;

import org.mockito.Mockito;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.security.Principal;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lightweight utility class for mocking Spring WebSocket message context in tests.
 * This is a more lightweight alternative to MockMessageResource that uses static methods
 * and Mockito for mocking instead of creating real objects.
 */
public final class MessageTestUtils {

   private MessageTestUtils() {
      // Utility class
   }

   /**
    * Executes an action within a mocked message context.
    * Automatically cleans up resources after execution.
    *
    * @param principal the principal to set in the message context
    * @param runtimeId optional runtime ID to set in message attributes
    * @param action    the action to execute
    * @param <R>       return type
    * @return the result of the action
    */
   public static <R> R withMockMessageContext(Principal principal, String runtimeId, Supplier<R> action) {
      return withMockMessageContext(principal, runtimeId, (ctx) -> action.get());
   }

   /**
    * Executes an action within a mocked message context with access to the context.
    *
    * @param principal the principal to set in the message context
    * @param runtimeId optional runtime ID to set in message attributes
    * @param action    the action to execute with access to message context
    * @param <R>       return type
    * @return the result of the action
    */
   public static <R> R withMockMessageContext(Principal principal, String runtimeId,
                                              Function<MessageContext, R> action) {
      // Create minimal mock objects
      GenericMessage<String> message = new GenericMessage<>("test");
      MessageAttributes messageAttributes = new MessageAttributes(message);

      if (runtimeId != null) {
         messageAttributes.setAttribute("sheetRuntimeId", runtimeId);
      }

      StompHeaderAccessor headerAccessor = messageAttributes.getHeaderAccessor();
      headerAccessor.setUser(principal);

      // Use Mockito to mock SimpMessagingTemplate instead of creating real one
      SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);

      CommandDispatcher commandDispatcher = new CommandDispatcher(headerAccessor, messagingTemplate, null);

      // Set thread-local context
      MessageContextHolder.setMessageAttributes(messageAttributes);

      try {
         MessageContext ctx = new MessageContext(headerAccessor, messagingTemplate, commandDispatcher);
         return action.apply(ctx);
      } finally {
         // Cleanup
         MessageContextHolder.setMessageAttributes(null);
      }
   }

   /**
    * Executes a void action within a mocked message context.
    */
   public static void withMockMessageContext(Principal principal, String runtimeId, Runnable action) {
      withMockMessageContext(principal, runtimeId, (ctx) -> {
         action.run();
         return null;
      });
   }

   /**
    * Executes an action with a parameter within a mocked message context.
    */
   public static <T, R> R withMockMessageContext(Principal principal, String runtimeId, T param,
                                                 Function<T, R> action) {
      return withMockMessageContext(principal, runtimeId, (ctx) -> action.apply(param));
   }

   /**
    * Executes an action with a parameter and access to message context.
    */
   public static <T, R> R withMockMessageContext(Principal principal, String runtimeId, T param,
                                                 BiFunction<MessageContext, T, R> action) {
      return withMockMessageContext(principal, runtimeId, (ctx) -> action.apply(ctx, param));
   }

   /**
    * Executes a void action with a parameter within a mocked message context.
    */
   public static <T> void withMockMessageContext(Principal principal, String runtimeId, T param,
                                                 Consumer<T> action) {
      withMockMessageContext(principal, runtimeId, (ctx) -> {
         action.accept(param);
         return null;
      });
   }

   /**
    * Simplified version without principal and runtimeId.
    */
   public static <R> R withMockMessageContext(Supplier<R> action) {
      return withMockMessageContext(null, null, action);
   }

   /**
    * Simplified version for void actions.
    */
   public static void withMockMessageContext(Runnable action) {
      withMockMessageContext(null, null, action);
   }

   /**
    * Creates a no-op CommandDispatcher for testing purposes.
    * This dispatcher overrides sendCommand, flush, and detach methods to be no-ops,
    * which is useful when you need a CommandDispatcher but don't want to actually send commands.
    *
    * @param principal the principal to set in the message context (can be null)
    * @return a CommandDispatcher with no-op implementations
    */
   public static CommandDispatcher createNoOpCommandDispatcher(Principal principal) {
      GenericMessage<String> message = new GenericMessage<>("simulated");
      MessageAttributes messageAttributes = new MessageAttributes(message);
      StompHeaderAccessor headerAccessor = messageAttributes.getHeaderAccessor();
      headerAccessor.setUser(principal);
      SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);

      return new CommandDispatcher(headerAccessor, messagingTemplate, null) {
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
            return createNoOpCommandDispatcher(principal);
         }
      };
   }

   /**
    * Message context holder that provides access to mocked components.
    */
   public static final class MessageContext {
      private final StompHeaderAccessor headerAccessor;
      private final SimpMessagingTemplate messagingTemplate;
      private final CommandDispatcher commandDispatcher;

      private MessageContext(StompHeaderAccessor headerAccessor,
                             SimpMessagingTemplate messagingTemplate,
                             CommandDispatcher commandDispatcher) {
         this.headerAccessor = headerAccessor;
         this.messagingTemplate = messagingTemplate;
         this.commandDispatcher = commandDispatcher;
      }

      public StompHeaderAccessor getHeaderAccessor() {
         return headerAccessor;
      }

      public SimpMessagingTemplate getMessagingTemplate() {
         return messagingTemplate;
      }

      public CommandDispatcher getCommandDispatcher() {
         return commandDispatcher;
      }

      public Principal getUser() {
         return headerAccessor == null ? null : headerAccessor.getUser();
      }
   }
}


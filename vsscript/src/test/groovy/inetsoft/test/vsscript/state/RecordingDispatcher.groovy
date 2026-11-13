package inetsoft.test.vsscript.state

import inetsoft.sree.internal.cluster.Cluster
import inetsoft.web.messaging.MessageAttributes
import inetsoft.web.viewsheet.command.MessageCommand
import inetsoft.web.viewsheet.command.ViewsheetCommand
import inetsoft.web.viewsheet.service.CommandDispatcher
import inetsoft.web.viewsheet.service.CommandDispatcherService
import org.mockito.Mockito
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.support.GenericMessage

import java.security.Principal

/** A CommandDispatcher that records every command instead of sending it. */
class RecordingDispatcher {
   static class Recorder extends CommandDispatcher {
      final List<Object> commands = []

      Recorder(def headerAccessor, def service) { super(headerAccessor, service, null) }

      @Override void sendCommand(String assemblyName, ViewsheetCommand command) { commands << command }
      @Override void flush() {}
      @Override CommandDispatcher detach() { this }
   }

   static Recorder create(Principal principal) {
      def accessor = new MessageAttributes(new GenericMessage<String>('simulated')).getHeaderAccessor()
      accessor.setUser(principal)
      def service = new CommandDispatcherService(Mockito.mock(SimpMessagingTemplate), Mockito.mock(Cluster))
      new Recorder(accessor, service)
   }

   static List<MessageCommand> messages(Recorder r) {
      r.commands.findAll { it instanceof MessageCommand } as List<MessageCommand>
   }
}

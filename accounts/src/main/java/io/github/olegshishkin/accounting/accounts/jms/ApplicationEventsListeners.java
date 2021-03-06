package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.ExecutionCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionFailedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionStartedAppEvt;
import io.github.olegshishkin.accounting.operation.messages.commands.Header.HeaderBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.ExecutionCompletedEvt.ExecutionCompletedEvtBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.ExecutionStartedEvt.ExecutionStartedEvtBuilder;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Async
@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationEventsListeners {

  private final MessageSender messageSender;

  @EventListener
  public void onExecutionStarted(ExecutionStartedAppEvt<?> evt) {
    var header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(evt.startedAt())
        .build();
    var message = new ExecutionStartedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    messageSender.send(message);
    log.debug("Execution started notification sent: {}", message);
  }

  @EventListener
  public void onExecutionCompleted(ExecutionCompletedAppEvt<?, ?> evt) {
    var header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(evt.completedAt())
        .build();
    var message = new ExecutionCompletedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    messageSender.send(message);
    log.debug("Execution completed notification sent: {}", message);
  }

  @EventListener
  public void onExecutionError(ExecutionFailedAppEvt<?> evt) {
    messageSender.send(evt.command());
    log.debug("Execution failed notification sent: {}", evt.command());
  }
}

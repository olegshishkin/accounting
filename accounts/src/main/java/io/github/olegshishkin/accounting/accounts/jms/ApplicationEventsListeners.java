package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.OperationCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationFailedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationStartedAppEvt;
import io.github.olegshishkin.accounting.operation.messages.commands.Header.HeaderBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.OperationCompletedEvt.OperationCompletedEvtBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.OperationStartedEvt.OperationStartedEvtBuilder;
import java.time.Instant;
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
  public void onOperationStarted(OperationStartedAppEvt<?> evt) {
    var header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(Instant.now())
        .build();
    var message = new OperationStartedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    messageSender.send(message);
    log.debug("Operation started notification sent: {}", message);
  }

  @EventListener
  public void onOperationCompleted(OperationCompletedAppEvt<?> evt) {
    var header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(evt.operation().getCreatedAt())
        .build();
    var message = new OperationCompletedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    messageSender.send(message);
    log.debug("Operation completed notification sent: {}", message);
  }

  @EventListener
  public void onOperationError(OperationFailedAppEvt<?> evt) {
    messageSender.send(evt.command());
    log.debug("Operation failed notification sent: {}", evt.command());
  }
}

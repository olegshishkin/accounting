package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.OperationCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationStartedAppEvt;
import io.github.olegshishkin.accounting.operation.messages.commands.Header;
import io.github.olegshishkin.accounting.operation.messages.commands.Header.HeaderBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.OperationCompletedEvt;
import io.github.olegshishkin.accounting.operation.messages.events.OperationCompletedEvt.OperationCompletedEvtBuilder;
import io.github.olegshishkin.accounting.operation.messages.events.OperationStartedEvt;
import io.github.olegshishkin.accounting.operation.messages.events.OperationStartedEvt.OperationStartedEvtBuilder;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;

@Async
@Slf4j
@RequiredArgsConstructor
public class MessageQueuePublishers {

  private final JmsTemplate jmsTemplate;
  private final Map<String, String> queueNames;

  @EventListener
  public void onOperationStarted(OperationStartedAppEvt<?> evt) {
    log.info("Operation started: {}", evt.command());
    Header header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(Instant.now())
        .build();
    OperationStartedEvt message = new OperationStartedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    String queue = queueNames.get(message.getClass().getSimpleName());
    jmsTemplate.convertAndSend(queue, evt);
  }

  @EventListener
  public void onOperationCompleted(OperationCompletedAppEvt<?> evt) {
    log.info("Operation completed: {}", evt.command());
    Header header = new HeaderBuilder()
        .withId(UUID.randomUUID().toString())
        .withTime(evt.completedAt())
        .build();
    OperationCompletedEvt message = new OperationCompletedEvtBuilder()
        .withHeader(header)
        .withCommand(evt.command())
        .build();
    String queue = queueNames.get(message.getClass().getSimpleName());
    jmsTemplate.convertAndSend(queue, evt);
  }
}

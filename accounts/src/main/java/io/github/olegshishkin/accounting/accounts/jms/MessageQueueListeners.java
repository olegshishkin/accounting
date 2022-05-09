package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.OperationCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationStartedAppEvt;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageQueueListeners {

  private final OperationService service;
  private final OperationMapper mapper;
  private final ApplicationEventPublisher publisher;

  @JmsListener(destination = "${message.queue.CreateDepositCmd}")
  public void consumeCreateDepositQueue(CreateDepositCmd cmd) {
    log.info("Create deposit for: {}", cmd);
    publisher.publishEvent(new OperationStartedAppEvt<>(cmd));
    Operation o = mapper.map(cmd);
    service.create(o)
        .map(operation -> new OperationCompletedAppEvt<>(cmd, operation.getCreatedAt()))
        .doOnNext(publisher::publishEvent)
        .subscribe();
  }
}

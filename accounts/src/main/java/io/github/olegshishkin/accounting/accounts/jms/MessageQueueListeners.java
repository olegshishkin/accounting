package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.OperationCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationFailedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.OperationStartedAppEvt;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.operation.messages.Command;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateWithdrawalCmd;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageQueueListeners {

  private final OperationService service;
  private final OperationMapper mapper;
  private final ApplicationEventPublisher publisher;

  @JmsListener(destination = "${message.queue.CreateDepositCmd}")
  public void consumeCreateDepositQueue(CreateDepositCmd cmd) {
    log.debug("Create deposit for {}", cmd);
    handle(mapper::map, service::deposit, cmd)
        .subscribe(o -> log.debug("Deposit completed for {}", cmd));
  }

  @JmsListener(destination = "${message.queue.CreateWithdrawalCmd}")
  public void consumeCreateWithdrawalQueue(CreateWithdrawalCmd cmd) {
    log.debug("Create withdrawal for {}", cmd);
    handle(mapper::map, service::withdraw, cmd)
        .subscribe(o -> log.debug("Withdrawal completed for {}", cmd));
  }

  /**
   * Handle specified command.
   *
   * @param <T>     type of command.
   * @param handler command handler.
   * @param cmd     command.
   */
  <T extends Command> Mono<Operation> handle(Function<T, Operation> mapper,
      Function<Operation, Mono<Operation>> handler,
      T cmd) {
    publisher.publishEvent(new OperationStartedAppEvt<>(cmd));
    var operation = mapper.apply(cmd);
    return handler.apply(operation)
        .doOnNext(o -> publisher.publishEvent(new OperationCompletedAppEvt<>(cmd, o)))
        .doOnError(t -> publisher.publishEvent(new OperationFailedAppEvt<>(cmd)));
  }
}

package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.ExecutionCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionFailedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionStartedAppEvt;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.messages.Command;
import io.github.olegshishkin.accounting.accounts.messages.commands.ApplyTransactionCmd;
import io.github.olegshishkin.accounting.accounts.messages.commands.CancelTransactionCmd;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
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
  private final OperationMapper operationMapper;
  private final ApplicationEventPublisher publisher;

  @JmsListener(destination = "${message.queue.ApplyTransactionCmd}")
  public void applyTransaction(ApplyTransactionCmd cmd) {
    log.debug("Transaction command received: {}", cmd);
    handle(operationMapper::map, service::execute, cmd)
        .subscribe(result -> log.debug("Transaction completed for {}", cmd));
  }

  @JmsListener(destination = "${message.queue.CancelTransactionCmd}")
  public void cancelTransaction(CancelTransactionCmd cmd) {
    log.debug("Cancel transaction command received: {}", cmd);
    handle(operationMapper::map, service::cancel, cmd)
        .subscribe(result -> log.debug("Transaction cancelled for {}", cmd));
  }

  /**
   * Handle specified command.
   *
   * @param <S>     command type.
   * @param <R>     result type.
   * @param mapper  command-transaction mapper.
   * @param handler command handler.
   * @param cmd     command.
   */
  <S extends Command, R> Mono<R> handle(Function<S, Transaction> mapper,
      Function<Transaction, Mono<R>> handler,
      S cmd) {
    publisher.publishEvent(new ExecutionStartedAppEvt<>(cmd));
    var transaction = mapper.apply(cmd);
    return handler.apply(transaction)
        .doOnNext(r -> publisher.publishEvent(new ExecutionCompletedAppEvt<>(cmd, transaction)))
        .doOnError(throwable -> publisher.publishEvent(new ExecutionFailedAppEvt<>(cmd)));
  }
}

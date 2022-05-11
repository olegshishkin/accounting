package io.github.olegshishkin.accounting.accounts.jms;

import io.github.olegshishkin.accounting.accounts.events.ExecutionCompletedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionFailedAppEvt;
import io.github.olegshishkin.accounting.accounts.events.ExecutionStartedAppEvt;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.mapper.TransferMapper;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.operation.messages.Command;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateTransferCmd;
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
  private final OperationMapper operationMapper;
  private final TransferMapper transferMapper;
  private final ApplicationEventPublisher publisher;

  @JmsListener(destination = "${message.queue.CreateDepositCmd}")
  public void consumeCreateDepositQueue(CreateDepositCmd cmd) {
    log.debug("Create deposit for {}", cmd);
    handle(operationMapper::map, service::deposit, cmd)
        .subscribe(o -> log.debug("Deposit completed for {}", cmd));
  }

  @JmsListener(destination = "${message.queue.CreateWithdrawalCmd}")
  public void consumeCreateWithdrawalQueue(CreateWithdrawalCmd cmd) {
    log.debug("Create withdrawal for {}", cmd);
    handle(operationMapper::map, service::withdraw, cmd)
        .subscribe(o -> log.debug("Withdrawal completed for {}", cmd));
  }

  @JmsListener(destination = "${message.queue.CreateTransferCmd}")
  public void consumeCreateTransferQueue(CreateTransferCmd cmd) {
    log.debug("Create transfer for {}", cmd);
    handle(transferMapper::map, service::transfer, cmd)
        .subscribe(o -> log.debug("Transfer completed for {}", cmd));
  }

  /**
   * Handle specified command.
   *
   * @param <S>     command type.
   * @param <I>     intermediate type.
   * @param <R>     result type.
   * @param handler command handler.
   * @param cmd     command.
   */
  <S extends Command, I, R> Mono<R> handle(Function<S, I> mapper,
      Function<I, Mono<R>> handler,
      S cmd) {
    publisher.publishEvent(new ExecutionStartedAppEvt<>(cmd));
    var arg = mapper.apply(cmd);
    return handler.apply(arg)
        .doOnNext(r -> publisher.publishEvent(new ExecutionCompletedAppEvt<>(cmd, r)))
        .doOnError(t -> publisher.publishEvent(new ExecutionFailedAppEvt<>(cmd)));
  }
}

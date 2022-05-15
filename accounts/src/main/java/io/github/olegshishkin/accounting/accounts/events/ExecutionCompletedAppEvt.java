package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.accounts.messages.Command;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
import java.time.Instant;

/**
 * Application context event. Emitted after the completion of the execution.
 *
 * @param command     command.
 * @param completedAt completion moment.
 * @param <T>         command type.
 */
public record ExecutionCompletedAppEvt<T extends Command>(T command, Instant completedAt) {

  public ExecutionCompletedAppEvt(T command, Transaction transaction) {
    this(command, transaction.time());
  }
}

package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.service.dto.Transfer;
import io.github.olegshishkin.accounting.operation.messages.Command;
import java.time.Instant;

/**
 * Application context event. Emitted after the completion of the execution.
 *
 * @param command     command.
 * @param completedAt completion moment.
 * @param <T>         command type.
 */
public record ExecutionCompletedAppEvt<T extends Command, R>(T command, Instant completedAt) {

  public ExecutionCompletedAppEvt(T command, R result) {
    this(command, getCompletionMoment(result));
  }

  private static <R> Instant getCompletionMoment(R r) {
    if (r instanceof Operation o) {
      return o.getCreatedAt();
    }
    if (r instanceof Transfer t) {
      return t.from().getCreatedAt();
    }
    throw new IllegalArgumentException("Unknown type: " + r.getClass());
  }
}

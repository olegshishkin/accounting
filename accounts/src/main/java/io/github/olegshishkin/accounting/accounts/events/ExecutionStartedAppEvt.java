package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.accounts.messages.Command;
import java.time.Instant;

/**
 * Application context event. Emitted when execution starts.
 *
 * @param command   command.
 * @param startedAt start moment.
 * @param <T>       command type.
 */
public record ExecutionStartedAppEvt<T extends Command>(T command, Instant startedAt) {

  public ExecutionStartedAppEvt(T command) {
    this(command, Instant.now());
  }
}

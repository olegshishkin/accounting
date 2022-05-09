package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.operation.messages.events.Command;
import java.time.Instant;

/**
 * Application context event. Emitted after the completion of the operation.
 *
 * @param command     source operation command.
 * @param completedAt moment of the operation completion.
 * @param <T>         command type.
 */
public record OperationCompletedAppEvt<T extends Command>(T command, Instant completedAt) {

}

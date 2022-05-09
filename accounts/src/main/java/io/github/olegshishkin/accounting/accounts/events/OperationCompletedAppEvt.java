package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.operation.messages.events.Command;

/**
 * Application context event. Emitted after the completion of the operation.
 *
 * @param command   source operation command.
 * @param operation completed operation.
 * @param <T>       command type.
 */
public record OperationCompletedAppEvt<T extends Command>(T command, Operation operation) {

}

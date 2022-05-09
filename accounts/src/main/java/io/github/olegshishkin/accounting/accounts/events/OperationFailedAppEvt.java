package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.operation.messages.events.Command;

/**
 * Application context event. Emitted when an operation failed due to an error.
 *
 * @param command source operation command.
 * @param <T>     command type.
 */
public record OperationFailedAppEvt<T extends Command>(T command) {

}

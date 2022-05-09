package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.operation.messages.events.Command;

/**
 * Application context event. Emitted when operation starts.
 *
 * @param command source operation command.
 * @param <T>     command type.
 */
public record OperationStartedAppEvt<T extends Command>(T command) {

}

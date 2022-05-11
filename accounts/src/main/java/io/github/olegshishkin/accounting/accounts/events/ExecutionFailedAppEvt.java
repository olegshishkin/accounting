package io.github.olegshishkin.accounting.accounts.events;

import io.github.olegshishkin.accounting.operation.messages.Command;

/**
 * Application context event. Emitted when an execution failed due to an error.
 *
 * @param command command.
 * @param <T>     command type.
 */
public record ExecutionFailedAppEvt<T extends Command>(T command) {

}

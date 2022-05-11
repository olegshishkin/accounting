package io.github.olegshishkin.accounting.accounts.service.dto;

import io.github.olegshishkin.accounting.accounts.model.Operation;

/**
 * Transfer consists of two operations.
 *
 * @param from withdrawal account.
 * @param to   deposit account.
 */
public record Transfer(Operation from, Operation to) {

}
package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationService {

  Flux<OperationDTO> find(OperationFilterDTO dto);

  /**
   * Execute transaction.
   *
   * @param tx transaction.
   * @return executed transaction with related operations.
   */
  Mono<Transaction> execute(Transaction tx);

  /**
   * Cancel all operations associated with the specified transaction identifier.
   *
   * @param tx transaction.
   */
  Mono<Void> cancel(Transaction tx);
}

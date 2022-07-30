package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import java.math.BigDecimal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

  Mono<AccountDTO> findById(Long id);

  Flux<AccountDTO> findAll();

  Mono<AccountDTO> create(AccountInputDTO dto);

  Mono<AccountDTO> update(Long id, AccountInputDTO dto);

  Mono<AccountDTO> close(Long id);

  /**
   * Deposit or withdraw from an account.
   *
   * @param id         account id.
   * @param difference amount (positive - deposit, negative - withdrawal).
   * @return saved account.
   * @throws IllegalArgumentException if the account is not found.
   */
  Mono<Void> changeBalance(Long id, BigDecimal difference);
}

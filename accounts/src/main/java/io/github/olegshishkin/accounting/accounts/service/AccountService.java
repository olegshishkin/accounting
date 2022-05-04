package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

  Mono<AccountDTO> findById(String id);

  Flux<AccountDTO> findAll();

  Mono<AccountDTO> create(AccountInputDTO dto);

  Mono<AccountDTO> update(String id, AccountInputDTO dto);

  Mono<AccountDTO> close(String id);
}

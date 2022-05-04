package io.github.olegshishkin.accounting.accounts.controller;

import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import io.github.olegshishkin.accounting.accounts.service.AccountService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AccountController {

  private final AccountService service;

  @QueryMapping
  public Mono<AccountDTO> findAccountById(@Argument String id) {
    return service.findById(id);
  }

  @QueryMapping
  public Flux<AccountDTO> getAllAccounts() {
    return service.findAll();
  }

  @MutationMapping
  public Mono<AccountDTO> createAccount(@Valid @Argument AccountInputDTO account) {
    return service.create(account);
  }

  @MutationMapping
  public Mono<AccountDTO> updateAccount(@Argument String id, @Valid @Argument AccountInputDTO account) {
    return service.update(id, account);
  }

  @MutationMapping
  public Mono<AccountDTO> closeAccount(@Argument String id) {
    return service.close(id);
  }
}

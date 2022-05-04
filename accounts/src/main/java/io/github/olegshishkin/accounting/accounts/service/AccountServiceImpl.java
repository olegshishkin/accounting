package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.mapper.AccountMapper;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository repository;
  private final AccountMapper mapper;

  @Override
  public Mono<AccountDTO> findById(String id) {
    return repository.findById(id)
        .map(mapper::map);
  }

  @Override
  public Flux<AccountDTO> findAll() {
    return repository.findAll().map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> create(AccountInputDTO dto) {
    Account account = mapper.merge(dto, Account.create());
    return repository.save(account)
        .map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> update(String id, AccountInputDTO dto) {
    return repository.findById(id)
        .map(i -> mapper.merge(dto, i))
        .flatMap(repository::save)
        .map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> close(String id) {
    return repository.findById(id)
        .map(this::close)
        .flatMap(repository::save)
        .map(mapper::map);
  }

  private Account close(Account account) {
    account.setDisabled(true);
    return account;
  }
}

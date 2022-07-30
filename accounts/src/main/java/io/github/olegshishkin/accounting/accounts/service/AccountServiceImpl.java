package io.github.olegshishkin.accounting.accounts.service;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import io.github.olegshishkin.accounting.accounts.mapper.AccountMapper;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
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
  private final R2dbcEntityOperations ops;
  private final R2dbcTransactionManager m;

  @Override
  public Mono<AccountDTO> findById(Long id) {
    return repository.findById(id)
        .map(mapper::map);
  }

  @Transactional(readOnly = true)
  @Override
  public Flux<AccountDTO> findAll() {
    return repository.findAll().map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> create(AccountInputDTO dto) {
    var account = mapper.merge(dto, Account.create());
    return repository.save(account)
        .map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> update(Long id, AccountInputDTO dto) {
    return repository.findById(id)
        .map(i -> mapper.merge(dto, i))
        .flatMap(repository::save)
        .map(mapper::map);
  }

  @Override
  public Mono<AccountDTO> close(Long id) {
    return repository.findById(id)
        .map(this::close)
        .flatMap(repository::save)
        .map(mapper::map);
  }

  @Override
  public Mono<Void> changeBalance(Long id, BigDecimal difference) {
    return repository.updateBalance(id, difference);
//    return repository.findById(id)
//        .doOnNext(a -> a.setBalance(a.getBalance().add(difference)))
//        .flatMap(repository::save)
//        .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No account " + id)))
//        .then();
  }

  private Account close(Account account) {
    account.setDisabled(true);
    return account;
  }
}

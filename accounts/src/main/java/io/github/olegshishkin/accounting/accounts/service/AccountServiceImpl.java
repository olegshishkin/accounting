package io.github.olegshishkin.accounting.accounts.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import io.github.olegshishkin.accounting.accounts.mapper.AccountMapper;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
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
  private final ReactiveMongoOperations ops;

  @Override
  public Mono<AccountDTO> findById(String id) {
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

  @Override
  public Mono<Account> changeBalance(String id, BigDecimal difference) {
    return ops.update(Account.class)
        .matching(query(where("id").is(id)))
        .apply(new Update().inc("balance", difference))
        .withOptions(FindAndModifyOptions.options().returnNew(true))
        .findAndModify()
        .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No account " + id)));
  }

  private Account close(Account account) {
    account.setDisabled(true);
    return account;
  }
}

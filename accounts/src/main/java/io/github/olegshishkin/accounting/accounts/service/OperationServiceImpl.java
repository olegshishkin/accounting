package io.github.olegshishkin.accounting.accounts.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class OperationServiceImpl implements OperationService {

  private final ReactiveMongoOperations ops;
  private final OperationRepository operationRepository;
  private final OperationMapper operationMapper;

  @Transactional(readOnly = true)
  @Override
  public Flux<OperationDTO> find(OperationFilterDTO dto) {
    var example = Example.of(operationMapper.map(dto));
    return operationRepository.findAll(example).map(operationMapper::map);
  }

  @Override
  public Mono<Operation> addPlusOperation(Operation o) {
    var accountId = o.getAccount().getId();
    return ops.update(Account.class)
        .matching(query(where("id").is(accountId)))
        .apply(new Update().inc("balance", o.getAmount()))
        .withOptions(FindAndModifyOptions.options().returnNew(true))
        .findAndModify()
        .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("No account " + accountId)))
        .doOnNext(account -> o.getAccount().setName(account.getName()))
        .then(ops.insert(o));
  }
}

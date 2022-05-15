package io.github.olegshishkin.accounting.accounts.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
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
public class OperationServiceImpl implements OperationService {

  private final ReactiveMongoOperations ops;
  private final OperationRepository repository;
  private final OperationMapper mapper;
  private final AccountService accountService;

  @Transactional(readOnly = true)
  @Override
  public Flux<OperationDTO> find(OperationFilterDTO dto) {
    var example = Example.of(mapper.map(dto));
    return repository.findAll(example).map(mapper::map);
  }

  @Override
  public Mono<Transaction> execute(Transaction tx) {
    return repository.findAllByMessageId(tx.messageId())
        .switchIfEmpty(cancel(tx).thenMany(apply(tx)))
        .collectList()
        .map(Transaction::new);
  }

  @Override
  public Mono<Void> cancel(Transaction tx) {
    var query = query(where("transactionId").is(tx.txId()).and("cancellation").isNull());
    query.fields().include("account.id", "amount");
    var update = new Update().set("cancellation", mapper.mapCancellation(tx));
    return ops.find(query, Operation.class)
        .flatMap(o -> {
          var accountId = o.getAccount().getId();
          var diff = o.getAmount().negate();
          return accountService.changeBalance(accountId, diff);
        })
        .then(ops.updateMulti(query, update, Operation.class))
        .then();
  }

  private Flux<Operation> apply(Transaction tx) {
    return Flux.fromIterable(tx.ops())
        .flatMap(o -> {
          var accountId = o.getAccount().getId();
          return accountService.changeBalance(accountId, o.getAmount()).doOnNext(o::setAccountName);
        })
        .thenMany(ops.insertAll(tx.ops()));
  }
}

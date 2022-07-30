package io.github.olegshishkin.accounting.accounts.service;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.data.relational.core.query.Update.update;

import io.github.olegshishkin.accounting.accounts.mapper.CancellationMapper;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OperationServiceImpl implements OperationService {

  //  private final ReactiveMongoOperations ops;
  private final R2dbcEntityOperations ops;
//  private final DatabaseClient dbClient;
//  private final TransactionalOperator txOp;
  private final OperationRepository repository;
  private final OperationMapper operationMapper;
  private final CancellationMapper cancellationMapper;
  private final AccountService accountService;

  @Transactional(readOnly = true)
  @Override
  public Flux<OperationDTO> find(OperationFilterDTO dto) {
    var example = Example.of(operationMapper.map(dto));
    return repository.findAll(example).map(operationMapper::map);
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
    var query = query(where("transaction_id").is(tx.txId()).and("cancellation_id").isNull());
    return ops.select(Operation.class)
        .matching(query.columns("account_id", "amount"))
        .all()
        .flatMap(this::updateBalance)
        .then(ops.update(Operation.class)
            .matching(query)
            .apply(update("cancellation", cancellationMapper.map(tx))))
        .then();
  }

  private Flux<Operation> apply(Transaction tx) {
    return Flux.fromIterable(tx.ops())
        .flatMap(ops::insert)
        .flatMap(o -> updateBalance(o).thenReturn(o));
  }

  private Mono<Void> updateBalance(Operation o) {
    return accountService.changeBalance(o.getAccount().getId(), o.getAmount());
  }
}

package io.github.olegshishkin.accounting.accounts.repository;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OperationRepository extends R2dbcRepository<Operation, Long> {

  Flux<Operation> findAllByMessageId(String messageId);
}

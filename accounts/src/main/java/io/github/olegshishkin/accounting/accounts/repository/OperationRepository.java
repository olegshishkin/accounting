package io.github.olegshishkin.accounting.accounts.repository;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OperationRepository extends ReactiveMongoRepository<Operation, String> {

  Flux<Operation> findAllByMessageId(String messageId);
}

package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationService {

  Flux<OperationDTO> find(OperationFilterDTO dto);

  Mono<OperationDTO> create(OperationDTO dto);
}
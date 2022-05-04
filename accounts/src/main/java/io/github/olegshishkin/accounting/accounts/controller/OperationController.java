package io.github.olegshishkin.accounting.accounts.controller;

import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OperationController {

  private final OperationService service;

  @QueryMapping
  public Flux<OperationDTO> findOperations(@Valid @Argument OperationFilterDTO filter) {
    return service.find(filter);
  }
}

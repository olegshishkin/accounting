package io.github.olegshishkin.accounting.accounts.service;

import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OperationServiceImpl implements OperationService {

  private final OperationRepository repository;
  private final OperationMapper mapper;

  @Override
  public Flux<OperationDTO> find(OperationFilterDTO dto) {
    Example<Operation> example = Example.of(mapper.map(dto));
    return repository.findAll(example).map(mapper::map);
  }

  @Override
  public Mono<OperationDTO> create(OperationDTO dto) {
    Operation o = mapper.map(dto);
    return repository.save(o).map(mapper::map);
  }
}

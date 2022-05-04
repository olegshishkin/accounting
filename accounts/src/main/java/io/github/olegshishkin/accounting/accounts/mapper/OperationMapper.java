package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OperationMapper {

  Operation map(OperationDTO dto);

  OperationDTO map(Operation entity);

  @Mapping(source = "accountId", target = "account.id")
  Operation map(OperationFilterDTO dto);
}

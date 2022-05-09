package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OperationMapper {

  Operation map(OperationDTO dto);

  OperationDTO map(Operation entity);

  @Mapping(target = "account.id", source = "accountId")
  Operation map(OperationFilterDTO dto);

  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "account.id", source = "accountId")
  @Mapping(target = "createdAt", source = "cmd", qualifiedByName = "now")
  Operation map(CreateDepositCmd cmd);

  @Named("now")
  default Instant now(CreateDepositCmd cmd) {
    return Instant.now();
  }
}

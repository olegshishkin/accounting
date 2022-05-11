package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateTransferCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateWithdrawalCmd;
import java.math.BigDecimal;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface OperationMapper {

  Operation map(OperationDTO dto);

  OperationDTO map(Operation entity);

  @Mapping(target = "account.id", source = "accountId")
  Operation map(OperationFilterDTO dto);

  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "account.id", source = "accountId")
  @Mapping(target = "createdAt", source = "header.id", qualifiedByName = "now")
  Operation map(CreateDepositCmd cmd);

  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "account.id", source = "accountId")
  @Mapping(target = "createdAt", source = "header.id", qualifiedByName = "now")
  @Mapping(target = "amount", source = "amount", qualifiedByName = "invert")
  Operation map(CreateWithdrawalCmd cmd);

  @Named("mapSource")
  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "account.id", source = "fromAccountId")
  @Mapping(target = "createdAt", source = "header.id", qualifiedByName = "now")
  @Mapping(target = "amount", source = "amount", qualifiedByName = "invert")
  Operation mapSource(CreateTransferCmd cmd);

  @Named("mapDestination")
  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "account.id", source = "toAccountId")
  @Mapping(target = "createdAt", source = "header.id", qualifiedByName = "now")
  Operation mapDestination(CreateTransferCmd cmd);

  @Named("now")
  default Instant now(String unimportant) {
    return Instant.now();
  }

  @Named("invert")
  default BigDecimal invert(BigDecimal number) {
    return number.negate();
  }
}

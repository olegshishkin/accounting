package io.github.olegshishkin.accounting.accounts.mapper;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import io.github.olegshishkin.accounting.accounts.messages.commands.ApplyTransactionCmd;
import io.github.olegshishkin.accounting.accounts.messages.commands.CancelTransactionCmd;
import io.github.olegshishkin.accounting.accounts.messages.commands.Entry;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.model.Operation.Cancellation;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.OperationFilterDTO;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
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

  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "date", source = "cmd.date")
  @Mapping(target = "transactionId", source = "cmd.transactionId")
  @Mapping(target = "messageId", source = "cmd.header.id")
  @Mapping(target = "account.id", source = "entry.accountId")
  @Mapping(target = "amount", source = "entry", qualifiedByName = "amount")
  Operation map(Entry entry, ApplyTransactionCmd cmd, Instant createdAt);

  Cancellation mapCancellation(Transaction tx);

  default Transaction map(ApplyTransactionCmd cmd) {
    Instant now = now(null);
    return cmd.getEntries()
        .stream()
        .map(entry -> this.map(entry, cmd, now))
        .collect(collectingAndThen(toList(), Transaction::new));
  }

  @Mapping(target = "txId", source = "transactionId")
  @Mapping(target = "messageId", source = "header.id")
  @Mapping(target = "time", source = "transactionId", qualifiedByName = "now")
  @Mapping(target = "ops", ignore = true)
  Transaction map(CancelTransactionCmd cmd);

  @Named("amount")
  default BigDecimal amount(Entry entry) {
    final BigDecimal amount = entry.getAmount();
    return switch (entry.getType()) {
      case DEPOSIT -> amount;
      case WITHDRAWAL -> amount.negate();
    };
  }

  @Named("now")
  default Instant now(String unimportant) {
    return Instant.now();
  }
}

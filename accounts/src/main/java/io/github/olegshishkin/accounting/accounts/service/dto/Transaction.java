package io.github.olegshishkin.accounting.accounts.service.dto;

import io.github.olegshishkin.accounting.accounts.model.Operation;
import java.time.Instant;
import java.util.List;

public record Transaction(String txId, String messageId, Instant time, List<Operation> ops) {

  public Transaction(List<Operation> ops) {
    this(getTxId(ops), getMessageId(ops), getTime(ops), ops);
  }

  private static String getTxId(List<Operation> operations) {
    return operations.iterator().next().getTransactionId();
  }

  private static String getMessageId(List<Operation> operations) {
    return operations.iterator().next().getMessageId();
  }

  private static Instant getTime(List<Operation> operations) {
    return operations.iterator().next().getCreatedAt();
  }
}

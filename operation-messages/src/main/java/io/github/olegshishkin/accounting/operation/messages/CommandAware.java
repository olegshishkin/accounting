package io.github.olegshishkin.accounting.operation.messages;

public interface CommandAware {

  Object getCommand();

  default String getTransactionId() {
    return ((TransactionIdAware) getCommand()).getTransactionId();
  }
}

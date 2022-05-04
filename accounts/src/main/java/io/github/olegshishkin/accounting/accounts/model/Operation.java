package io.github.olegshishkin.accounting.accounts.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@Data
@Document("operations")
public class Operation {

  @Id
  private String id;
  @Indexed
  private String transactionId;
  private OperationAccount account;
  private BigDecimal amount;

  @ToString
  @Data
  public static class OperationAccount {

    @Indexed
    String id;
    String name;
  }
}

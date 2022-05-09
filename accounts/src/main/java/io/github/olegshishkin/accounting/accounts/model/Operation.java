package io.github.olegshishkin.accounting.accounts.model;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ToString
@Data
@Document("operations")
public class Operation {

  @Id
  private String id;
  private Instant createdAt;
  @Indexed
  private String transactionId;
  private String messageId;
  private OperationAccount account;
  @Field(targetType = DECIMAL128)
  private BigDecimal amount;
  @Version
  private Long version;

  @ToString
  @Data
  public static class OperationAccount {

    @Indexed
    String id;
    String name;
  }
}

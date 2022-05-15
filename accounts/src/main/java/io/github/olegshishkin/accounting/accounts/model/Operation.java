package io.github.olegshishkin.accounting.accounts.model;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

  /**
   * Actual creation time of this entry.
   */
  private Instant createdAt;

  /**
   * Operation date.
   */
  private LocalDate date;

  /**
   * An identifier of related operations that are one transaction.
   */
  @Indexed
  private String transactionId;

  /**
   * The message identifier that is the cause of this operation.
   */
  @Indexed
  private String messageId;

  /**
   * Affected account.
   */
  private OperationAccount account;

  /**
   * Operation amount.
   */
  @Field(targetType = DECIMAL128)
  private BigDecimal amount;

  /**
   * If this field is not null, this operation has been cancelled.
   */
  private Cancellation cancellation;

  @Version
  private Long version;

  public void setAccountName(Account source) {
    this.getAccount().setName(source.getName());
  }

  /**
   * Account.
   */
  @ToString
  @Data
  public static class OperationAccount {

    @Indexed
    String id;
    String name;
  }

  @ToString
  @Data
  public static class Cancellation {

    /**
     * The message identifier that caused this operation to be cancelled.
     */
    private String messageId;

    /**
     * Cancellation time.
     */
    private Instant time;
  }
}

package io.github.olegshishkin.accounting.accounts.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class Operation {

  @Id
  private String id;

  /**
   * Actual creation time of this entry.
   */
  @CreatedDate
  @Column("created_at")
  private Instant createdAt;

  /**
   * Operation date.
   */
  private LocalDate date;

  /**
   * An identifier of related operations that are one transaction.
   */
  @Column("transaction_id")
  private String transactionId;

  /**
   * The message identifier that is the cause of this operation.
   */
  @Column("message_id")
  private String messageId;

  /**
   * Affected account.
   */
  @Transient
  @Column("account_id")
  private Account account;

  /**
   * Operation amount.
   */
  private BigDecimal amount;

  /**
   * If this field is not null, this operation has been cancelled.
   */
  @Transient
  @Column("cancellation_id")
  private Long cancellation;

  @Version
  private Long version;
}

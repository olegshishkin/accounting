package io.github.olegshishkin.accounting.accounts.model;

import java.time.Instant;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class Cancellation {

  @Id
  private Long id;

  /**
   * The message identifier that caused this operation to be cancelled.
   */
  @Column("message_id")
  private String messageId;

  /**
   * Cancellation time.
   */
  private Instant time;

  @Version
  private Long version;
}

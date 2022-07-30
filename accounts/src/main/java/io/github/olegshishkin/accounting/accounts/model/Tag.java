package io.github.olegshishkin.accounting.accounts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class Tag {

  @Id
  private Long id;
  @Transient
  @Column("account_id")
  private Account account;
  private String tag;
  @Version
  private Long version;
}

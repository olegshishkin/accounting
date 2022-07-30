package io.github.olegshishkin.accounting.accounts.model;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class Account {

  @Id
  private Long id;
  private String name;
  @Transient
  private Set<String> tags = new HashSet<>();
  private BigDecimal balance;
  private String comment;
  private Boolean disabled;
  @Version
  private Long version;

  public static Account create() {
    Account a = new Account();
    a.setBalance(ZERO);
    a.setDisabled(false);
    return a;
  }
}

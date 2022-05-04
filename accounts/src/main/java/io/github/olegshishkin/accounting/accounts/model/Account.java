package io.github.olegshishkin.accounting.accounts.model;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@Data
@Document("accounts")
public class Account {

  @Id
  private String id;
  private String name;
  private Set<String> tags = new HashSet<>();
  private BigDecimal balance;
  private String comment;
  private Boolean disabled;

  public static Account create() {
    Account a = new Account();
    a.setBalance(ZERO);
    return a;
  }
}

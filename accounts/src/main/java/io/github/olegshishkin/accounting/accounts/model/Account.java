package io.github.olegshishkin.accounting.accounts.model;

import static java.math.BigDecimal.ZERO;
import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ToString
@Data
@Document("accounts")
public class Account {

  @Id
  private String id;
  private String name;
  private Set<String> tags = new HashSet<>();
  @Field(targetType = DECIMAL128)
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

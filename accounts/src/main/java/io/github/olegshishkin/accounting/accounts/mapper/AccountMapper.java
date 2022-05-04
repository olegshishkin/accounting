package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

  @Mapping(source = "disabled", target = "disabled", qualifiedByName = "disabled")
  Account merge(AccountInputDTO source, @MappingTarget Account destination);

  Account map(AccountDTO dto);

  AccountDTO map(Account entity);

  @Named("disabled")
  default Boolean disabled(Boolean disabled) {
    if (disabled == null) {
      return false;
    }
    return disabled;
  }
}

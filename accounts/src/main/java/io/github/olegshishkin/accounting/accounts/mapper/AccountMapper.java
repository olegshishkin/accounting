package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {

  Account merge(AccountInputDTO source, @MappingTarget Account destination);

  Account map(AccountDTO dto);

  AccountDTO map(Account entity);
}

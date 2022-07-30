package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.model.Cancellation;
import io.github.olegshishkin.accounting.accounts.service.dto.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CancellationMapper {

  Cancellation map(Transaction tx);
}

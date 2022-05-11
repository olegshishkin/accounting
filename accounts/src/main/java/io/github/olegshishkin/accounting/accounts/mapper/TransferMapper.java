package io.github.olegshishkin.accounting.accounts.mapper;

import io.github.olegshishkin.accounting.accounts.service.dto.Transfer;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateTransferCmd;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
    uses = {OperationMapper.class},
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TransferMapper {

  @Mapping(target = "from", source = "cmd", qualifiedByName = "mapSource")
  @Mapping(target = "to", source = "cmd", qualifiedByName = "mapDestination")
  Transfer map(CreateTransferCmd cmd);
}

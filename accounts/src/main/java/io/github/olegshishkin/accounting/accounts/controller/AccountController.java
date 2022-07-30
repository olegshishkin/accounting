package io.github.olegshishkin.accounting.accounts.controller;

import io.github.olegshishkin.accounting.accounts.jms.MessageQueueListeners;
import io.github.olegshishkin.accounting.accounts.messages.commands.ApplyTransactionCmd.ApplyTransactionCmdBuilder;
import io.github.olegshishkin.accounting.accounts.messages.commands.Entry;
import io.github.olegshishkin.accounting.accounts.messages.commands.Entry.Type;
import io.github.olegshishkin.accounting.accounts.messages.commands.Header;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountDTO;
import io.github.olegshishkin.accounting.accounts.model.graphql.AccountInputDTO;
import io.github.olegshishkin.accounting.accounts.service.AccountService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AccountController {

  private final AccountService service;

  private final JmsTemplate jmsTemplate;

  private final Map<String, String> queueNames;

  private final MessageQueueListeners listeners;

  @QueryMapping
  public Mono<AccountDTO> findAccountById(@Argument Long id) {
    return service.findById(id);
  }

  @QueryMapping
  public Flux<AccountDTO> getAllAccounts() {
    IntStream.range(0, 1)
        .forEach(i -> execute());
    return null;
  }

  private void execute() {
    var cmd = new ApplyTransactionCmdBuilder()
        .withHeader(new Header.HeaderBuilder().withId(UUID.randomUUID().toString()).withTime(Instant.now()).build())
        .withDate(LocalDate.now())
        .withTransactionId(UUID.randomUUID().toString())
        .build();
    cmd.getEntries().add(new Entry.EntryBuilder()
        .withAccountId("1")
        .withType(Type.DEPOSIT)
        .withAmount(BigDecimal.valueOf(23.4))
        .build());
    cmd.getEntries().add(new Entry.EntryBuilder()
        .withAccountId("2")
        .withType(Type.WITHDRAWAL)
        .withAmount(BigDecimal.valueOf(-23.4))
        .build());
//    listeners.applyTransaction(cmd);
    jmsTemplate.convertAndSend(queueNames.get(cmd.getClass().getSimpleName()), cmd);
  }

  @MutationMapping
  public Mono<AccountDTO> createAccount(@Valid @Argument AccountInputDTO account) {
    return service.create(account);
  }

  @MutationMapping
  public Mono<AccountDTO> updateAccount(@Argument Long id, @Valid @Argument AccountInputDTO account) {
    return service.update(id, account);
  }

  @MutationMapping
  public Mono<AccountDTO> closeAccount(@Argument Long id) {
    return service.close(id);
  }
}

package io.github.olegshishkin.accounting.accounts.jms;

import static java.util.function.Function.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.github.olegshishkin.accounting.accounts.jms.MessageQueueListenersTest.MessageQueueListenersTestConfig;
import io.github.olegshishkin.accounting.accounts.mapper.AccountMapper;
import io.github.olegshishkin.accounting.accounts.mapper.AccountMapperImpl;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapperImpl;
import io.github.olegshishkin.accounting.accounts.messages.commands.ApplyTransactionCmd.ApplyTransactionCmdBuilder;
import io.github.olegshishkin.accounting.accounts.messages.commands.Entry.EntryBuilder;
import io.github.olegshishkin.accounting.accounts.messages.commands.Entry.Type;
import io.github.olegshishkin.accounting.accounts.messages.commands.Header.HeaderBuilder;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import io.github.olegshishkin.accounting.accounts.service.AccountService;
import io.github.olegshishkin.accounting.accounts.service.AccountServiceImpl;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.accounts.service.OperationServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers
@Import(MessageQueueListenersTestConfig.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class MessageQueueListenersTest {

  @Container
  private static final MongoDBContainer DB_CONTAINER = new MongoDBContainer("mongo:5.0.8");

  @Autowired
  private OperationService operationService;

  @Autowired
  private ReactiveMongoOperations ops;

  @Autowired
  private OperationMapper operationMapper;

  @InjectMocks
  private MessageQueueListeners listeners;

  @DynamicPropertySource
  private static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", DB_CONTAINER::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    var publisher = Mockito.mock(ApplicationEventPublisher.class);
    doNothing().when(publisher).publishEvent(any());

    listeners = new MessageQueueListeners(operationService, operationMapper, publisher);
  }

  @AfterEach
  void tearDown() {
    ops.dropCollection(Account.class).zipWith(ops.dropCollection(Operation.class)).block();
  }

  @Test
  void createDeposit() {
    // given
    var account = Account.create();
    account.setName("accName1");
    account.setBalance(account.getBalance().add(BigDecimal.valueOf(4.5)));
    var savedAccount = ops.insert(account).block();
    assert savedAccount != null;

    var cmd = new ApplyTransactionCmdBuilder()
        .withHeader(
            new HeaderBuilder()
                .withId("id1")
                .withCorrelationId("corr1")
                .withTime(Instant.now())
                .build())
        .withDate(LocalDate.now())
        .withTransactionId("transId1")
        .build();
    cmd.getEntries().add(new EntryBuilder()
        .withAccountId(savedAccount.getId())
        .withType(Type.DEPOSIT)
        .withAmount(BigDecimal.valueOf(2.3))
        .build());

    // when
    listeners.handle(operationMapper::map, operationService::execute, cmd).block();

    // then
    StepVerifier.create(ops.query(Operation.class).all())
        .expectNextMatches(o ->
            o.getTransactionId().equals(cmd.getTransactionId()) &&
                o.getAccount().getName().equals(account.getName()) &&
                o.getAmount().equals(BigDecimal.valueOf(2.3)) &&
                o.getCreatedAt() != null)
        .verifyComplete();

    StepVerifier.create(ops.query(Account.class).all())
        .expectNextMatches(a -> a.getBalance().equals(BigDecimal.valueOf(6.8)))
        .verifyComplete();
  }

  @Test
  void createWithdrawal() {
    // given
    var account = Account.create();
    account.setName("accName1");
    account.setBalance(account.getBalance().add(BigDecimal.valueOf(4.6)));
    var savedAccount = ops.insert(account).block();
    assert savedAccount != null;

    var cmd = new ApplyTransactionCmdBuilder()
        .withHeader(
            new HeaderBuilder()
                .withId("id1")
                .withCorrelationId("corr1")
                .withTime(Instant.now())
                .build())
        .withDate(LocalDate.now())
        .withTransactionId("transId1")
        .build();
    cmd.getEntries().add(new EntryBuilder()
        .withAccountId(savedAccount.getId())
        .withType(Type.WITHDRAWAL)
        .withAmount(BigDecimal.valueOf(2.2))
        .build());

    // when
    listeners.handle(operationMapper::map, operationService::execute, cmd).block();

    // then
    StepVerifier.create(ops.query(Operation.class).all())
        .expectNextMatches(o ->
            o.getTransactionId().equals(cmd.getTransactionId()) &&
                o.getAccount().getName().equals(account.getName()) &&
                o.getAmount().equals(BigDecimal.valueOf(-2.2)) &&
                o.getCreatedAt() != null)
        .verifyComplete();

    StepVerifier.create(ops.query(Account.class).all())
        .expectNextMatches(a -> a.getBalance().equals(BigDecimal.valueOf(2.4)))
        .verifyComplete();
  }

  @Test
  void createTransfer() {
    // given
    var source = Account.create();
    source.setName("accName1");
    source.setBalance(source.getBalance().add(BigDecimal.valueOf(2.5)));

    var dest = Account.create();
    dest.setName("accName2");
    dest.setBalance(dest.getBalance().add(BigDecimal.valueOf(1.5)));

    var accounts = ops.insertAll(List.of(source, dest)).toStream().toList();
    assert accounts.size() == 2;

    var cmd = new ApplyTransactionCmdBuilder()
        .withHeader(
            new HeaderBuilder()
                .withId("id1")
                .withCorrelationId("corr1")
                .withTime(Instant.now())
                .build())
        .withDate(LocalDate.now())
        .withTransactionId("transId1")
        .build();
    cmd.getEntries().add(new EntryBuilder()
        .withAccountId(source.getId())
        .withType(Type.WITHDRAWAL)
        .withAmount(BigDecimal.valueOf(0.52))
        .build());
    cmd.getEntries().add(new EntryBuilder()
        .withAccountId(dest.getId())
        .withType(Type.DEPOSIT)
        .withAmount(BigDecimal.valueOf(0.52))
        .build());

    // when
    listeners.handle(operationMapper::map, operationService::execute, cmd).block();

    // then
    var operations = ops.query(Operation.class)
        .all()
        .toStream()
        .collect(Collectors.toMap(o -> o.getAccount().getId(), identity()));

    assertEquals(2, operations.size());

    var withdrawal = operations.get(source.getId());
    assertEquals(cmd.getTransactionId(), withdrawal.getTransactionId());
    assertEquals(source.getName(), withdrawal.getAccount().getName());
    assertEquals(BigDecimal.valueOf(-0.52), withdrawal.getAmount());
    assertNotNull(withdrawal.getCreatedAt());

    var deposit = operations.get(dest.getId());
    assertEquals(cmd.getTransactionId(), deposit.getTransactionId());
    assertEquals(dest.getName(), deposit.getAccount().getName());
    assertEquals(BigDecimal.valueOf(0.52), deposit.getAmount());
    assertNotNull(deposit.getCreatedAt());

    var balances = ops.query(Account.class)
        .all()
        .toStream()
        .map(Account::getBalance)
        .toList();

    assertEquals(2, balances.size());
    assertThat(balances, containsInAnyOrder(BigDecimal.valueOf(1.98), BigDecimal.valueOf(2.02)));
  }

  @TestConfiguration
  static class MessageQueueListenersTestConfig {

    @Bean
    public OperationMapper operationMapper() {
      return new OperationMapperImpl();
    }

    @Bean
    public AccountMapper accountMapper() {
      return new AccountMapperImpl();
    }

    @Bean
    public AccountService accountService(ReactiveMongoOperations ops,
        AccountRepository repository,
        AccountMapper accountMapper) {
      return new AccountServiceImpl(repository, accountMapper, ops);
    }

    @Bean
    public OperationService operationService(ReactiveMongoOperations ops,
        OperationRepository operationRepository,
        OperationMapper mapper,
        AccountService accountService) {
      return new OperationServiceImpl(ops, operationRepository, mapper, accountService);
    }
  }
}
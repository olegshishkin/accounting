package io.github.olegshishkin.accounting.accounts.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.github.olegshishkin.accounting.accounts.jms.MessageQueueListenersTest.MessageQueueListenersTestConfig;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapperImpl;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.model.Operation;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import io.github.olegshishkin.accounting.accounts.service.OperationService;
import io.github.olegshishkin.accounting.accounts.service.OperationServiceImpl;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd.CreateDepositCmdBuilder;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateWithdrawalCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateWithdrawalCmd.CreateWithdrawalCmdBuilder;
import io.github.olegshishkin.accounting.operation.messages.commands.Header.HeaderBuilder;
import java.math.BigDecimal;
import java.time.Instant;
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
import org.springframework.context.annotation.Primary;
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
  private OperationService service;

  @Autowired
  private ReactiveMongoOperations ops;

  @Autowired
  private OperationMapper mapper;

  @InjectMocks
  private MessageQueueListeners listeners;

  @DynamicPropertySource
  private static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", DB_CONTAINER::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    doNothing().when(publisher).publishEvent(any());

    listeners = new MessageQueueListeners(service, mapper, publisher);
  }

  @AfterEach
  void tearDown() {
    ops.dropCollection(Account.class).zipWith(ops.dropCollection(Operation.class)).block();
  }

  @Test
  void createDeposit() {
    // given
    Account acc = Account.create();
    acc.setName("accName1");
    acc.setBalance(acc.getBalance().add(BigDecimal.valueOf(4.5)));
    Account savedAcc = ops.insert(acc).block();
    assert savedAcc != null;

    CreateDepositCmd cmd = new CreateDepositCmdBuilder()
        .withHeader(new HeaderBuilder().withId("id1").withTime(Instant.now()).build())
        .withTransactionId("transId1")
        .withAccountId(savedAcc.getId())
        .withAmount(BigDecimal.valueOf(2.3))
        .build();

    // when
    listeners.handle(mapper::map, service::addPlusOperation, cmd).block();

    // then
    StepVerifier.create(ops.query(Operation.class).all())
        .expectNextMatches(o ->
            o.getTransactionId().equals(cmd.getTransactionId()) &&
                o.getAccount().getName().equals(acc.getName()) &&
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
    Account acc = Account.create();
    acc.setName("accName2");
    acc.setBalance(acc.getBalance().add(BigDecimal.valueOf(4.6)));
    Account savedAcc = ops.insert(acc).block();
    assert savedAcc != null;

    CreateWithdrawalCmd cmd = new CreateWithdrawalCmdBuilder()
        .withHeader(new HeaderBuilder().withId("id2").withTime(Instant.now()).build())
        .withTransactionId("transId2")
        .withAccountId(savedAcc.getId())
        .withAmount(BigDecimal.valueOf(2.2))
        .build();

    // when
    listeners.handle(mapper::map, service::addPlusOperation, cmd).block();

    // then
    StepVerifier.create(ops.query(Operation.class).all())
        .expectNextMatches(o ->
            o.getTransactionId().equals(cmd.getTransactionId()) &&
                o.getAccount().getName().equals(acc.getName()) &&
                o.getAmount().equals(BigDecimal.valueOf(-2.2)) &&
                o.getCreatedAt() != null)
        .verifyComplete();

    StepVerifier.create(ops.query(Account.class).all())
        .expectNextMatches(a -> a.getBalance().equals(BigDecimal.valueOf(2.4)))
        .verifyComplete();
  }

  @TestConfiguration
  static class MessageQueueListenersTestConfig {

    @Primary
    @Bean
    public OperationMapper operationMapper() {
      return new OperationMapperImpl();
    }

    @Primary
    @Bean
    public OperationService operationService(ReactiveMongoOperations ops,
        OperationRepository operationRepository,
        OperationMapper mapper) {
      return new OperationServiceImpl(ops, operationRepository, mapper);
    }
  }
}
package io.github.olegshishkin.accounting.accounts.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.github.olegshishkin.accounting.accounts.mapper.OperationMapper;
import io.github.olegshishkin.accounting.accounts.mapper.OperationMapperImpl;
import io.github.olegshishkin.accounting.accounts.model.Account;
import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import io.github.olegshishkin.accounting.accounts.repository.OperationRepository;
import io.github.olegshishkin.accounting.accounts.service.OperationServiceImpl;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd;
import io.github.olegshishkin.accounting.operation.messages.commands.CreateDepositCmd.CreateDepositCmdBuilder;
import io.github.olegshishkin.accounting.operation.messages.commands.Header.HeaderBuilder;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class MessageQueueListenersTest {

  @Container
  private static final MongoDBContainer DB_CONTAINER = new MongoDBContainer("mongo:5.0.8");

  @Autowired
  private OperationRepository operationRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ReactiveMongoOperations ops;

  @Mock
  private ApplicationEventPublisher publisher;

  @Spy
  private OperationMapper mapper = new OperationMapperImpl();

  @InjectMocks
  private MessageQueueListeners listeners;

  @DynamicPropertySource
  private static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", DB_CONTAINER::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    doNothing().when(publisher).publishEvent(any());
    OperationServiceImpl service = new OperationServiceImpl(ops, operationRepository, mapper);
    listeners = new MessageQueueListeners(service, mapper, publisher);
  }

  @AfterEach
  void tearDown() {
    operationRepository.deleteAll().subscribe();
    accountRepository.deleteAll().subscribe();
  }

  @Test
  void consumeCreateDepositQueue() {
    // given
    Account acc = Account.create();
    acc.setName("accName1");
    acc.setBalance(acc.getBalance().add(BigDecimal.valueOf(4.5)));
    Account savedAcc = accountRepository.save(acc).block();
    assert savedAcc != null;

    CreateDepositCmd cmd = new CreateDepositCmdBuilder()
        .withHeader(new HeaderBuilder().withId("id1").withTime(Instant.now()).build())
        .withTransactionId("transId1")
        .withAccountId(savedAcc.getId())
        .withAmount(BigDecimal.valueOf(2.3))
        .build();

    // when
    listeners.consumeCreateDepositQueue(cmd);

    // then
    StepVerifier.create(operationRepository.findAll())
        .expectNextMatches(o ->
            o.getTransactionId().equals(cmd.getTransactionId()) &&
                o.getAccount().getName().equals(acc.getName()) &&
                o.getAmount().equals(BigDecimal.valueOf(2.3)))
        .verifyComplete();

    StepVerifier.create(accountRepository.findById(savedAcc.getId()))
        .expectNextMatches(a -> a.getBalance().equals(BigDecimal.valueOf(6.8)))
        .verifyComplete();
  }
}
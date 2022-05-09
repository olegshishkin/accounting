package io.github.olegshishkin.accounting.accounts.config;

import io.github.olegshishkin.accounting.accounts.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.reactive.TransactionalOperator;

@EnableReactiveMongoRepositories(basePackageClasses = AccountRepository.class)
@Configuration
public class DataStorageConfig {

  @Bean
  public ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory dbFactory) {
    return new ReactiveMongoTransactionManager(dbFactory);
  }

  @Bean
  public TransactionalOperator transactionalOperator(ReactiveMongoTransactionManager tm) {
    return TransactionalOperator.create(tm);
  }
}

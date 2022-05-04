package io.github.olegshishkin.accounting.accounts.repository;

import io.github.olegshishkin.accounting.accounts.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

}

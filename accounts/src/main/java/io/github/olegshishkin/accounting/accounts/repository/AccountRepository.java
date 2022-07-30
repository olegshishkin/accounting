package io.github.olegshishkin.accounting.accounts.repository;

import io.github.olegshishkin.accounting.accounts.model.Account;
import java.math.BigDecimal;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {

  @Modifying
  @Query("update Account set balance = balance + :difference where id = :id")
  Mono<Void> updateBalance(@Param("id") Long id, @Param("difference") BigDecimal difference);
}

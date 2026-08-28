package com.rewardsprogram.repository;

import com.rewardsprogram.domain.model.CustomerPointsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerPointsAccountRepository extends JpaRepository<CustomerPointsAccount, Long> {

    Optional<CustomerPointsAccount> findByCustomerId(String customerId);
}

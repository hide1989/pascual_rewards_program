package com.rewardsprogram.repository;

import com.rewardsprogram.domain.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}

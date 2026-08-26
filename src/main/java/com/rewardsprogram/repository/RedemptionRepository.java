package com.rewardsprogram.repository;

import com.rewardsprogram.domain.model.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
}

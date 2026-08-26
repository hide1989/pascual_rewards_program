package com.rewardsprogram.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "redemption")
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private long pointsRedeemed;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant redemptionDate;

    protected Redemption() {
    }

    public Redemption(String customerId, long pointsRedeemed, BigDecimal amountValue) {
        this.customerId = customerId;
        this.pointsRedeemed = pointsRedeemed;
        this.amountValue = amountValue;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public long getPointsRedeemed() {
        return pointsRedeemed;
    }

    public BigDecimal getAmountValue() {
        return amountValue;
    }

    public Instant getRedemptionDate() {
        return redemptionDate;
    }
}

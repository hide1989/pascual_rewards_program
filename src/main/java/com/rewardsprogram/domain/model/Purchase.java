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
@Table(name = "purchase")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private long pointsEarned;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmountAfterPurchase;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant purchaseDate;

    protected Purchase() {
    }

    public Purchase(String customerId, BigDecimal amount, long pointsEarned, BigDecimal totalAmountAfterPurchase) {
        this.customerId = customerId;
        this.amount = amount;
        this.pointsEarned = pointsEarned;
        this.totalAmountAfterPurchase = totalAmountAfterPurchase;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getPointsEarned() {
        return pointsEarned;
    }

    public BigDecimal getTotalAmountAfterPurchase() {
        return totalAmountAfterPurchase;
    }

    public Instant getPurchaseDate() {
        return purchaseDate;
    }
}

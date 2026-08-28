package com.rewardsprogram.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_points_account")
public class CustomerPointsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmountPurchased = BigDecimal.ZERO;

    @Column(nullable = false)
    private long totalPointsRedeemed = 0L;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    protected CustomerPointsAccount() {
    }

    public CustomerPointsAccount(String customerId) {
        this.customerId = customerId;
        this.totalAmountPurchased = BigDecimal.ZERO;
        this.totalPointsRedeemed = 0L;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmountPurchased() {
        return totalAmountPurchased;
    }

    public void setTotalAmountPurchased(BigDecimal totalAmountPurchased) {
        this.totalAmountPurchased = totalAmountPurchased;
    }

    public long getTotalPointsRedeemed() {
        return totalPointsRedeemed;
    }

    public void setTotalPointsRedeemed(long totalPointsRedeemed) {
        this.totalPointsRedeemed = totalPointsRedeemed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

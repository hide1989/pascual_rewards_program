package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.command.RegisterPurchaseCommand;
import com.rewardsprogram.application.dto.response.PurchaseResponse;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.model.Purchase;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import com.rewardsprogram.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RegisterPurchaseHandler {

    private final CustomerPointsAccountRepository accountRepository;
    private final PurchaseRepository purchaseRepository;
    private final PointsCalculator pointsCalculator;

    public RegisterPurchaseHandler(CustomerPointsAccountRepository accountRepository,
                                    PurchaseRepository purchaseRepository,
                                    PointsCalculator pointsCalculator) {
        this.accountRepository = accountRepository;
        this.purchaseRepository = purchaseRepository;
        this.pointsCalculator = pointsCalculator;
    }

    @Transactional
    public PurchaseResponse handle(RegisterPurchaseCommand command) {
        CustomerPointsAccount account = accountRepository.findByCustomerId(command.customerId())
                .orElseGet(() -> new CustomerPointsAccount(command.customerId()));

        BigDecimal previousTotal = account.getTotalAmountPurchased();
        long pointsEarned = pointsCalculator.pointsEarnedForPurchase(previousTotal, command.amount());
        BigDecimal newTotal = previousTotal.add(command.amount());

        account.setTotalAmountPurchased(newTotal);
        account = accountRepository.save(account);

        Purchase purchase = new Purchase(command.customerId(), command.amount(), pointsEarned, newTotal);
        purchase = purchaseRepository.save(purchase);

        long totalAvailablePoints = pointsCalculator.totalPointsEarned(newTotal) - account.getTotalPointsRedeemed();

        return new PurchaseResponse(
                command.customerId(),
                purchase.getId(),
                command.amount(),
                pointsEarned,
                totalAvailablePoints
        );
    }
}

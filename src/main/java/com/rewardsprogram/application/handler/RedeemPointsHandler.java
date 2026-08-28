package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.command.RedeemPointsCommand;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.model.Redemption;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.exception.InsufficientPointsException;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import com.rewardsprogram.repository.RedemptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RedeemPointsHandler {

    private final CustomerPointsAccountRepository accountRepository;
    private final RedemptionRepository redemptionRepository;
    private final PointsCalculator pointsCalculator;

    public RedeemPointsHandler(CustomerPointsAccountRepository accountRepository,
                                RedemptionRepository redemptionRepository,
                                PointsCalculator pointsCalculator) {
        this.accountRepository = accountRepository;
        this.redemptionRepository = redemptionRepository;
        this.pointsCalculator = pointsCalculator;
    }

    @Transactional
    public RedemptionResponse handle(RedeemPointsCommand command) {
        CustomerPointsAccount account = accountRepository.findByCustomerId(command.customerId())
                .orElseGet(() -> new CustomerPointsAccount(command.customerId()));

        long totalPointsEarned = pointsCalculator.totalPointsEarned(account.getTotalAmountPurchased());
        long availablePoints = totalPointsEarned - account.getTotalPointsRedeemed();

        if (command.points() > availablePoints) {
            throw new InsufficientPointsException(command.customerId(), command.points(), availablePoints);
        }

        account.setTotalPointsRedeemed(account.getTotalPointsRedeemed() + command.points());
        account = accountRepository.save(account);

        BigDecimal amountValue = pointsCalculator.pointsToPesos(command.points());
        Redemption redemption = new Redemption(command.customerId(), command.points(), amountValue);
        redemption = redemptionRepository.save(redemption);

        long remainingPoints = totalPointsEarned - account.getTotalPointsRedeemed();

        return new RedemptionResponse(
                command.customerId(),
                redemption.getId(),
                command.points(),
                amountValue,
                remainingPoints
        );
    }
}

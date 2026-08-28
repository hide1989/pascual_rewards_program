package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.query.GetCustomerPointsBalanceQuery;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class GetCustomerPointsBalanceHandler {

    private final CustomerPointsAccountRepository accountRepository;
    private final PointsCalculator pointsCalculator;

    public GetCustomerPointsBalanceHandler(CustomerPointsAccountRepository accountRepository,
                                            PointsCalculator pointsCalculator) {
        this.accountRepository = accountRepository;
        this.pointsCalculator = pointsCalculator;
    }

    @Transactional(readOnly = true)
    public CustomerPointsBalanceResponse handle(GetCustomerPointsBalanceQuery query) {
        BigDecimal totalAmountPurchased = BigDecimal.ZERO;
        long totalPointsRedeemed = 0L;

        var accountOpt = accountRepository.findByCustomerId(query.customerId());
        if (accountOpt.isPresent()) {
            CustomerPointsAccount account = accountOpt.get();
            totalAmountPurchased = account.getTotalAmountPurchased();
            totalPointsRedeemed = account.getTotalPointsRedeemed();
        }

        long totalPointsEarned = pointsCalculator.totalPointsEarned(totalAmountPurchased);
        long availablePoints = totalPointsEarned - totalPointsRedeemed;

        return new CustomerPointsBalanceResponse(
                query.customerId(),
                totalPointsEarned,
                totalPointsRedeemed,
                availablePoints
        );
    }
}

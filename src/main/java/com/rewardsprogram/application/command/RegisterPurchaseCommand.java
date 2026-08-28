package com.rewardsprogram.application.command;

import java.math.BigDecimal;

public record RegisterPurchaseCommand(String customerId, BigDecimal amount) {
}

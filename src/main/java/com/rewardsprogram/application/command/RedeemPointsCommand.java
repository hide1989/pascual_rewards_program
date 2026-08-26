package com.rewardsprogram.application.command;

public record RedeemPointsCommand(String customerId, long points) {
}

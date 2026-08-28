package com.rewardsprogram.controller;

import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.handler.GetCustomerPointsBalanceHandler;
import com.rewardsprogram.application.query.GetCustomerPointsBalanceQuery;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Validated
public class CustomerPointsController {

    private final GetCustomerPointsBalanceHandler getCustomerPointsBalanceHandler;

    public CustomerPointsController(GetCustomerPointsBalanceHandler getCustomerPointsBalanceHandler) {
        this.getCustomerPointsBalanceHandler = getCustomerPointsBalanceHandler;
    }

    @GetMapping("/{customerId}/points")
    public CustomerPointsBalanceResponse getPointsBalance(
            @PathVariable @NotBlank(message = "customerId no puede estar vacío") String customerId) {
        GetCustomerPointsBalanceQuery query = new GetCustomerPointsBalanceQuery(customerId);
        return getCustomerPointsBalanceHandler.handle(query);
    }
}

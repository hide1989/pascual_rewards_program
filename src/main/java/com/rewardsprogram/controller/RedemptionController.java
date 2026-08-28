package com.rewardsprogram.controller;

import com.rewardsprogram.application.command.RedeemPointsCommand;
import com.rewardsprogram.application.dto.request.RedeemPointsRequest;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
import com.rewardsprogram.application.handler.RedeemPointsHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/redemptions")
public class RedemptionController {

    private final RedeemPointsHandler redeemPointsHandler;

    public RedemptionController(RedeemPointsHandler redeemPointsHandler) {
        this.redeemPointsHandler = redeemPointsHandler;
    }

    @PostMapping
    public ResponseEntity<RedemptionResponse> redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        RedeemPointsCommand command = new RedeemPointsCommand(request.customerId(), request.points());
        RedemptionResponse response = redeemPointsHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

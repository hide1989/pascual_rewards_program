package com.rewardsprogram.controller;

import com.rewardsprogram.application.command.RegisterPurchaseCommand;
import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.PurchaseResponse;
import com.rewardsprogram.application.handler.RegisterPurchaseHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final RegisterPurchaseHandler registerPurchaseHandler;

    public PurchaseController(RegisterPurchaseHandler registerPurchaseHandler) {
        this.registerPurchaseHandler = registerPurchaseHandler;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> registerPurchase(@Valid @RequestBody RegisterPurchaseRequest request) {
        RegisterPurchaseCommand command = new RegisterPurchaseCommand(request.customerId(), request.amount());
        PurchaseResponse response = registerPurchaseHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

package com.rewardsprogram.exception;

public class InsufficientPointsException extends RuntimeException {

    public InsufficientPointsException(String customerId, long requestedPoints, long availablePoints) {
        super("El cliente %s solicitó redimir %d puntos pero solo tiene %d disponibles."
                .formatted(customerId, requestedPoints, availablePoints));
    }
}

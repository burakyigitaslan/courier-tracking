package com.casestudy.couriertracking.api.exception;

/**
 * Thrown when a courier with the given ID is not found
 */
public class CourierNotFoundException extends RuntimeException {

    public CourierNotFoundException(Long courierId) {
        super("Courier not found with id: " + courierId);
    }
}

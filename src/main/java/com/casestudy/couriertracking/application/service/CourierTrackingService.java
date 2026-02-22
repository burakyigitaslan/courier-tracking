package com.casestudy.couriertracking.application.service;

import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.domain.model.CourierLocation;

/**
 * Service interface for courier tracking operations
 */
public interface CourierTrackingService {
    /**
     * Saves a new courier location and publishes a domain event
     *
     * @param locationRequestDTO (time, courierId, lat, lng)
     * @return CourierLocation (id, time, courierId, lat, lng)
     */
    CourierLocation logLocation(LocationRequestDTO locationRequestDTO);

    /**
     * Returns the total travel distance for a courier
     *
     * @param courierId
     * @return Total distance in meters
     */
    double getTotalTravelDistance(Long courierId);
}

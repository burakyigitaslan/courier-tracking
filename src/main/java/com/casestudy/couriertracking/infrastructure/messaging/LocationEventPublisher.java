package com.casestudy.couriertracking.infrastructure.messaging;

import com.casestudy.couriertracking.domain.model.CourierLocation;

/**
 * Interface for publishing location events
 */
public interface LocationEventPublisher {
    /**
     * Publishes a location event
     *
     * @param courierLocation (id, courier, lat, lng, time)
     */
    void publish(CourierLocation courierLocation);
}

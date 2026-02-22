package com.casestudy.couriertracking.infrastructure.config;

import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "courier.tracking")
public class CourierTrackingProperties {
    /**
     * Radius in meters to consider a courier entered a store area.
     */
    private double storeRadiusMeters = 100.0;

    /**
     * Minimum time in minutes before logging another entry for the same store.
     */
    private long debounceMinutes = 1;

    /**
     * Strategy to use for distance calculation.
     */
    private DistanceStrategyType distanceStrategy = DistanceStrategyType.HAVERSINE;
}

package com.casestudy.couriertracking.domain.strategy;

import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;

/**
 * Interface for distance calculation between two geolocation points.
 * Allows swapping distance algorithms without modifying the code.
 */
public interface DistanceCalculationStrategy {
    /**
     * Returns the strategy type.
     * 
     * @return DistanceStrategyType
     */
    DistanceStrategyType getType();

    /**
     * Calculates the distance in meters between two geolocation points
     *
     * @param lat1 Latitude of point 1
     * @param lng1 Longitude of point 1
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in meters
     */
    double calculate(double lat1, double lon1, double lat2, double lon2);
}

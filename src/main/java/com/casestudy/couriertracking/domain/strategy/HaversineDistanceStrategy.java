package com.casestudy.couriertracking.domain.strategy;

import org.springframework.stereotype.Component;

import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;

/**
 * Haversine implementation for calculating the
 * distance between two geolocation points
 */
@Component
public class HaversineDistanceStrategy implements DistanceCalculationStrategy {
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * Returns the strategy type.
     * 
     * @return DistanceStrategyType.HAVERSINE
     */
    @Override
    public DistanceStrategyType getType() {
        return DistanceStrategyType.HAVERSINE;
    }

    /**
     * Calculates the distance in meters between two geolocation points
     * 
     * @param lat1 Latitude of point 1
     * @param lng1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lng2 Longitude of point 2
     * @return Distance in meters
     */
    @Override
    public double calculate(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}

package com.casestudy.couriertracking.domain.strategy;

import org.springframework.stereotype.Component;

import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;

/**
 * Equirectangular implementation for calculating the
 * distance between two geolocation points
 */
@Component
public class EquirectangularDistanceStrategy implements DistanceCalculationStrategy {
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * Returns the strategy type.
     * 
     * @return DistanceStrategyType.EQUIRECTANGULAR
     */
    @Override
    public DistanceStrategyType getType() {
        return DistanceStrategyType.EQUIRECTANGULAR;
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
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lng1Rad = Math.toRadians(lng1);
        double lng2Rad = Math.toRadians(lng2);

        double x = (lng2Rad - lng1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);

        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }
}

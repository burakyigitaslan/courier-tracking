package com.casestudy.couriertracking.strategy;

import com.casestudy.couriertracking.domain.strategy.HaversineDistanceStrategy;
import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineDistanceStrategyTest {

    private final HaversineDistanceStrategy strategy = new HaversineDistanceStrategy();

    @Test
    void shouldReturnCorrectStrategyType() {
        assertEquals(DistanceStrategyType.HAVERSINE, strategy.getType());
    }

    @Test
    void shouldReturnZeroForSamePoint() {
        double distance = strategy.calculate(40.9923307, 29.1244229, 40.9923307, 29.1244229);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void shouldCalculateCorrectDistance() {
        double distance = strategy.calculate(40.9923307, 29.1244229, 40.986106, 29.1161293);
        assertTrue(distance > 0);
        assertTrue(distance < 2000.0);
    }

    @Test
    void shouldHandleNegativeCoordinates() {
        double distance = strategy.calculate(-33.8688, 151.2093, -37.8136, 144.9631);
        assertTrue(distance > 0);
    }

    @Test
    void shouldCalculateKnownDistanceWithinTolerance() {
        double distance = strategy.calculate(40.9923307, 29.1244229, 40.986106, 29.1161293);
        assertTrue(distance > 700 && distance < 1100);
    }
}

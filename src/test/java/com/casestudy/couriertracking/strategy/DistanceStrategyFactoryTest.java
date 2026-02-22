package com.casestudy.couriertracking.strategy;

import com.casestudy.couriertracking.domain.strategy.DistanceStrategyFactory;
import com.casestudy.couriertracking.domain.strategy.DistanceCalculationStrategy;
import com.casestudy.couriertracking.domain.strategy.HaversineDistanceStrategy;
import com.casestudy.couriertracking.domain.strategy.EquirectangularDistanceStrategy;
import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DistanceStrategyFactoryTest {

    @Mock
    private HaversineDistanceStrategy haversineDistanceStrategy;

    @Mock
    private EquirectangularDistanceStrategy equirectangularDistanceStrategy;

    private DistanceStrategyFactory factory;

    @BeforeEach
    void setUp() {
        when(haversineDistanceStrategy.getType()).thenReturn(DistanceStrategyType.HAVERSINE);
        when(equirectangularDistanceStrategy.getType()).thenReturn(DistanceStrategyType.EQUIRECTANGULAR);
        factory = new DistanceStrategyFactory(List.of(haversineDistanceStrategy, equirectangularDistanceStrategy));
    }

    @Test
    void getStrategy_ShouldReturnHaversine_WhenTypeIsHaversine() {
        DistanceCalculationStrategy strategy = factory.getStrategy(DistanceStrategyType.HAVERSINE);
        assertEquals(haversineDistanceStrategy, strategy);
    }

    @Test
    void getStrategy_ShouldReturnEquirectangular_WhenTypeIsEquirectangular() {
        DistanceCalculationStrategy strategy = factory.getStrategy(DistanceStrategyType.EQUIRECTANGULAR);
        assertEquals(equirectangularDistanceStrategy, strategy);
    }

    @Test
    void getStrategy_ShouldThrowException_WhenTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }

    @Test
    void getStrategy_ShouldThrowException_WhenStrategyNotRegistered() {
        DistanceStrategyFactory emptyFactory = new DistanceStrategyFactory(List.of());
        assertThrows(IllegalArgumentException.class,
                () -> emptyFactory.getStrategy(DistanceStrategyType.HAVERSINE));
    }
}

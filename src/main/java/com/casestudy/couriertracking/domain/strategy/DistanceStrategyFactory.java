package com.casestudy.couriertracking.domain.strategy;

import org.springframework.stereotype.Component;

import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory class to get DistanceCalculationStrategy based on type
 */
@Component
public class DistanceStrategyFactory {
    private final Map<DistanceStrategyType, DistanceCalculationStrategy> strategyMap;

    public DistanceStrategyFactory(List<DistanceCalculationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DistanceCalculationStrategy::getType, Function.identity()));
    }

    /**
     * Get the strategy implementation based on the enum type
     *
     * @param distanceStrategyType
     * @return DistanceCalculationStrategy implementation
     */
    public DistanceCalculationStrategy getStrategy(DistanceStrategyType distanceStrategyType) {
        if (distanceStrategyType == null) {
            throw new IllegalArgumentException("Strategy type cannot be null");
        }

        DistanceCalculationStrategy strategy = strategyMap.get(distanceStrategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("No implementation found for strategy: " + distanceStrategyType);
        }
        return strategy;
    }
}

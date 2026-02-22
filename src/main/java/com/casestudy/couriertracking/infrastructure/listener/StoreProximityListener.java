package com.casestudy.couriertracking.infrastructure.listener;

import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.model.Store;
import com.casestudy.couriertracking.domain.model.StoreEntryLog;
import com.casestudy.couriertracking.domain.repository.StoreEntryLogRepository;
import com.casestudy.couriertracking.domain.strategy.DistanceCalculationStrategy;
import com.casestudy.couriertracking.domain.strategy.DistanceStrategyFactory;
import com.casestudy.couriertracking.infrastructure.config.CourierTrackingProperties;
import com.casestudy.couriertracking.infrastructure.config.StoreDataLoader;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Logs store entry with a 1-minute debounce. Listens for LocationCreatedEvent 
 * and checks if the courier is within 100 meters of any store.
 */
@Component
@RequiredArgsConstructor
public class StoreProximityListener {
    private final StoreDataLoader storeDataLoader;
    private final StoreEntryLogRepository storeEntryLogRepository;
    private final DistanceStrategyFactory distanceStrategyFactory;
    private final CourierTrackingProperties properties;

    /**
     * Handles location updates and logs store entries with debounce
     * 
     * @param location (id, courier, lat, lng, time)
     */
    public void onLocationCreated(CourierLocation location) {
        DistanceCalculationStrategy strategy = distanceStrategyFactory.getStrategy(properties.getDistanceStrategy());
        for (Store store : storeDataLoader.getStores()) {
            double distance = strategy.calculate(
                    location.getLat(), location.getLng(),
                    store.getLat(), store.getLng());

            if (distance <= properties.getStoreRadiusMeters() && !isDebouncedEntry(location, store.getName(), location.getTime())) {
                logStoreEntry(location, store.getName(), location.getTime());
            }
        }
    }

    /**
     * Checks if the entry should be debounced based on the last entry time
     * 
     * @param location (id, courier, lat, lng, time)
     * @param storeName
     * @param currentTime
     * @return boolean
     */
    private boolean isDebouncedEntry(CourierLocation location, String storeName, LocalDateTime currentTime) {
        Optional<StoreEntryLog> lastEntry = storeEntryLogRepository
                .findTopByCourierAndStoreNameOrderByEntryTimeDesc(location.getCourier(), storeName);

        if (lastEntry.isPresent()) {
            Duration timeSinceLastEntry = Duration.between(lastEntry.get().getEntryTime(), currentTime);
            if (timeSinceLastEntry.toMinutes() < properties.getDebounceMinutes()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs the store entry to the database
     * 
     * @param location (id, courier, lat, lng, time)
     * @param storeName
     * @param entryTime
     */
    private void logStoreEntry(CourierLocation location, String storeName, LocalDateTime entryTime) {
        StoreEntryLog entryLog = StoreEntryLog.builder()
                .courier(location.getCourier())
                .storeName(storeName)
                .entryTime(entryTime)
                .build();

        storeEntryLogRepository.save(entryLog);
    }
}

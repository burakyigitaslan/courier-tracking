package com.casestudy.couriertracking.infrastructure.listener;

import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.model.Store;
import com.casestudy.couriertracking.domain.model.StoreEntryLog;
import com.casestudy.couriertracking.domain.repository.StoreEntryLogRepository;
import com.casestudy.couriertracking.domain.strategy.DistanceCalculationStrategy;
import com.casestudy.couriertracking.domain.strategy.DistanceStrategyFactory;
import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import com.casestudy.couriertracking.infrastructure.config.CourierTrackingProperties;
import com.casestudy.couriertracking.infrastructure.config.StoreDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreProximityListenerTest {

        @Mock
        private StoreDataLoader storeDataLoader;

        @Mock
        private StoreEntryLogRepository storeEntryLogRepository;

        @Mock
        private DistanceStrategyFactory distanceStrategyFactory;

        @Mock
        private DistanceCalculationStrategy distanceCalculationStrategy;

        @Mock
        private CourierTrackingProperties properties;

        @InjectMocks
        private StoreProximityListener storeProximityListener;

        private Courier testCourier;

        @BeforeEach
        void setUp() {
                testCourier = Courier.builder()
                                .id(1L)
                                .firstName("Test")
                                .lastName("Courier")
                                .phoneNumber("+905551234567")
                                .build();

                lenient().when(properties.getDistanceStrategy()).thenReturn(DistanceStrategyType.HAVERSINE);
                lenient().when(properties.getStoreRadiusMeters()).thenReturn(100.0);
                lenient().when(properties.getDebounceMinutes()).thenReturn(1L);
        }

        @Test
        void shouldLogStoreEntryWhenCourierIsWithin100Meters() {
                Store store = new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229);
                when(storeDataLoader.getStores()).thenReturn(List.of(store));
                when(storeEntryLogRepository.findTopByCourierAndStoreNameOrderByEntryTimeDesc(testCourier,
                                "Ataşehir MMM Migros"))
                                .thenReturn(Optional.empty());
                when(storeEntryLogRepository.save(any(StoreEntryLog.class)))
                                .thenReturn(StoreEntryLog.builder().id(1L).courier(testCourier)
                                                .storeName("Ataşehir MMM Migros").entryTime(LocalDateTime.now())
                                                .build());

                when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                                .thenReturn(distanceCalculationStrategy);
                when(distanceCalculationStrategy.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(50.0);

                CourierLocation location = CourierLocation.builder()
                                .courier(testCourier)
                                .lat(40.9923307)
                                .lng(29.1244229)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .build();

                storeProximityListener.onLocationCreated(location);

                verify(storeEntryLogRepository).save(any(StoreEntryLog.class));
        }

        @Test
        void shouldNotLogStoreEntryWhenCourierIsFarAway() {
                Store store = new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229);
                when(storeDataLoader.getStores()).thenReturn(List.of(store));

                when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                                .thenReturn(distanceCalculationStrategy);
                when(distanceCalculationStrategy.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(500.0);

                CourierLocation location = CourierLocation.builder()
                                .courier(testCourier)
                                .lat(41.0)
                                .lng(29.0)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .build();

                storeProximityListener.onLocationCreated(location);

                verify(storeEntryLogRepository, never()).save(any());
        }

        @Test
        void shouldDebounceReEntryWithinOneMinute() {
                Store store = new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229);
                when(storeDataLoader.getStores()).thenReturn(List.of(store));

                StoreEntryLog recentEntry = StoreEntryLog.builder()
                                .id(1L)
                                .courier(testCourier)
                                .storeName("Ataşehir MMM Migros")
                                .entryTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .build();

                when(storeEntryLogRepository.findTopByCourierAndStoreNameOrderByEntryTimeDesc(testCourier,
                                "Ataşehir MMM Migros"))
                                .thenReturn(Optional.of(recentEntry));

                when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                                .thenReturn(distanceCalculationStrategy);
                when(distanceCalculationStrategy.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(10.0);

                CourierLocation location = CourierLocation.builder()
                                .courier(testCourier)
                                .lat(40.9923307)
                                .lng(29.1244229)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 0, 30))
                                .build();

                storeProximityListener.onLocationCreated(location);

                verify(storeEntryLogRepository, never()).save(any());
        }

        @Test
        void shouldAllowReEntryAfterOneMinute() {
                Store store = new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229);
                when(storeDataLoader.getStores()).thenReturn(List.of(store));

                StoreEntryLog oldEntry = StoreEntryLog.builder()
                                .id(1L)
                                .courier(testCourier)
                                .storeName("Ataşehir MMM Migros")
                                .entryTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .build();

                when(storeEntryLogRepository.findTopByCourierAndStoreNameOrderByEntryTimeDesc(testCourier,
                                "Ataşehir MMM Migros"))
                                .thenReturn(Optional.of(oldEntry));
                when(storeEntryLogRepository.save(any(StoreEntryLog.class)))
                                .thenReturn(StoreEntryLog.builder().id(2L).courier(testCourier)
                                                .storeName("Ataşehir MMM Migros").entryTime(LocalDateTime.now())
                                                .build());

                when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                                .thenReturn(distanceCalculationStrategy);
                when(distanceCalculationStrategy.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(10.0);

                CourierLocation location = CourierLocation.builder()
                                .courier(testCourier)
                                .lat(40.9923307)
                                .lng(29.1244229)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 2))
                                .build();

                storeProximityListener.onLocationCreated(location);

                verify(storeEntryLogRepository).save(any(StoreEntryLog.class));
        }

        @Test
        void shouldHandleMultipleCouriersIndependently() {
                Store store = new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229);
                when(storeDataLoader.getStores()).thenReturn(List.of(store));

                Courier courier2 = Courier.builder()
                                .id(2L)
                                .firstName("Second")
                                .lastName("Courier")
                                .phoneNumber("+905559876543")
                                .build();

                StoreEntryLog recentEntry = StoreEntryLog.builder()
                                .id(1L)
                                .courier(testCourier)
                                .storeName("Ataşehir MMM Migros")
                                .entryTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .build();

                when(storeEntryLogRepository.findTopByCourierAndStoreNameOrderByEntryTimeDesc(testCourier,
                                "Ataşehir MMM Migros"))
                                .thenReturn(Optional.of(recentEntry));
                when(storeEntryLogRepository.findTopByCourierAndStoreNameOrderByEntryTimeDesc(courier2,
                                "Ataşehir MMM Migros"))
                                .thenReturn(Optional.empty());
                when(storeEntryLogRepository.save(any(StoreEntryLog.class)))
                                .thenReturn(StoreEntryLog.builder().id(2L).courier(courier2)
                                                .storeName("Ataşehir MMM Migros").entryTime(LocalDateTime.now())
                                                .build());

                when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                                .thenReturn(distanceCalculationStrategy);
                when(distanceCalculationStrategy.calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(10.0);

                CourierLocation location1 = CourierLocation.builder()
                                .courier(testCourier)
                                .lat(40.9923307)
                                .lng(29.1244229)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 0, 30))
                                .build();

                CourierLocation location2 = CourierLocation.builder()
                                .courier(courier2)
                                .lat(40.9923307)
                                .lng(29.1244229)
                                .time(LocalDateTime.of(2024, 1, 1, 10, 0, 30))
                                .build();

                storeProximityListener.onLocationCreated(location1);
                storeProximityListener.onLocationCreated(location2);

                verify(storeEntryLogRepository, times(1)).save(any(StoreEntryLog.class));
        }
}

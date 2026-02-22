package com.casestudy.couriertracking.application.service;

import com.casestudy.couriertracking.api.exception.CourierNotFoundException;
import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.mapper.CourierLocationMapper;
import com.casestudy.couriertracking.application.service.implementation.CourierTrackingServiceImpl;
import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.repository.CourierLocationRepository;
import com.casestudy.couriertracking.domain.repository.CourierRepository;
import com.casestudy.couriertracking.domain.strategy.DistanceCalculationStrategy;
import com.casestudy.couriertracking.domain.strategy.DistanceStrategyFactory;
import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import com.casestudy.couriertracking.infrastructure.messaging.LocationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierTrackingServiceImplTest {

    @Mock
    private CourierLocationRepository courierLocationRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private LocationEventPublisher locationEventPublisher;

    @Mock
    private DistanceCalculationStrategy distanceCalculationStrategy;

    @Mock
    private CourierLocationMapper courierLocationMapper;

    @Mock
    private DistanceStrategyFactory distanceStrategyFactory;

    @InjectMocks
    private CourierTrackingServiceImpl courierTrackingService;

    private Courier testCourier;
    private LocationRequestDTO locationRequest;
    private CourierLocation courierLocation;

    @BeforeEach
    void setUp() {
        testCourier = Courier.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Courier")
                .totalDistance(100.0)
                .build();

        locationRequest = new LocationRequestDTO(
                LocalDateTime.now(),
                1L,
                40.0,
                29.0);

        courierLocation = CourierLocation.builder()
                .courier(testCourier)
                .lat(40.0)
                .lng(29.0)
                .time(locationRequest.getTime())
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(
                courierTrackingService, "distanceStrategyType", DistanceStrategyType.HAVERSINE);
    }

    @Test
    void logLocation_ShouldLogLocationAndPublishEvent_WhenCourierExists() {
        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCourier));
        when(courierLocationMapper.toEntity(locationRequest)).thenReturn(courierLocation);
        when(courierLocationRepository.existsByCourierAndTime(testCourier, locationRequest.getTime())).thenReturn(false);
        when(courierLocationRepository.save(any(CourierLocation.class))).thenReturn(courierLocation);
        when(courierLocationRepository.findTopByCourierAndTimeLessThanOrderByTimeDesc(testCourier, locationRequest.getTime())).thenReturn(Optional.empty());
        when(courierLocationRepository.findTopByCourierAndTimeGreaterThanOrderByTimeAsc(testCourier, locationRequest.getTime())).thenReturn(Optional.empty());

        CourierLocation result = courierTrackingService.logLocation(locationRequest);

        assertNotNull(result);
        assertEquals(testCourier, result.getCourier());
        verify(courierLocationRepository).save(any(CourierLocation.class));
        verify(locationEventPublisher).publish(courierLocation);

        verify(distanceStrategyFactory, never()).getStrategy(any());
        verify(distanceCalculationStrategy, never()).calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(courierRepository, never()).save(testCourier);
    }

    @Test
    void logLocation_ShouldCalculateDistance_WhenPreviousLocationExists() {
        CourierLocation previousLocation = CourierLocation.builder()
                .courier(testCourier)
                .lat(41.0)
                .lng(29.0)
                .time(LocalDateTime.now().minusMinutes(5))
                .build();

        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCourier));
        when(courierLocationMapper.toEntity(locationRequest)).thenReturn(courierLocation);
        when(courierLocationRepository.existsByCourierAndTime(testCourier, locationRequest.getTime())).thenReturn(false);
        when(courierLocationRepository.save(any(CourierLocation.class))).thenReturn(courierLocation);
        when(courierLocationRepository.findTopByCourierAndTimeLessThanOrderByTimeDesc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.of(previousLocation));
        when(courierLocationRepository.findTopByCourierAndTimeGreaterThanOrderByTimeAsc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.empty());
        when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                .thenReturn(distanceCalculationStrategy);
        when(distanceCalculationStrategy.calculate(41.0, 29.0, 40.0, 29.0)).thenReturn(111.0);

        courierTrackingService.logLocation(locationRequest);

        verify(distanceCalculationStrategy).calculate(41.0, 29.0, 40.0, 29.0);
        assertEquals(211.0, testCourier.getTotalDistance());
        verify(courierRepository).save(testCourier);
    }

    @Test
    void logLocation_ShouldSkipProcessing_WhenLocationAlreadyExists() {
        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCourier));
        when(courierLocationMapper.toEntity(locationRequest)).thenReturn(courierLocation);
        when(courierLocationRepository.existsByCourierAndTime(testCourier, locationRequest.getTime())).thenReturn(true);
        when(courierLocationRepository.findTopByCourierOrderByTimeDesc(testCourier)).thenReturn(Optional.of(courierLocation));

        CourierLocation result = courierTrackingService.logLocation(locationRequest);

        assertNotNull(result);
        verify(courierLocationRepository, never()).save(any());
        verify(locationEventPublisher, never()).publish(any());
        verify(distanceStrategyFactory, never()).getStrategy(any());
        verify(courierRepository, never()).save(testCourier);
    }

    @Test
    void logLocation_ShouldCalculateDistance_WhenOutOfOrderLocationArrives() {
        CourierLocation prevLocation = CourierLocation.builder()
                .courier(testCourier)
                .lat(41.0)
                .lng(29.0)
                .time(locationRequest.getTime().minusMinutes(5))
                .build();
        CourierLocation nextLocation = CourierLocation.builder()
                .courier(testCourier)
                .lat(39.0)
                .lng(29.0)
                .time(locationRequest.getTime().plusMinutes(5))
                .build();

        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCourier));
        when(courierLocationMapper.toEntity(locationRequest)).thenReturn(courierLocation);
        when(courierLocationRepository.existsByCourierAndTime(testCourier, locationRequest.getTime())).thenReturn(false);
        when(courierLocationRepository.save(any(CourierLocation.class))).thenReturn(courierLocation);
        when(courierLocationRepository.findTopByCourierAndTimeLessThanOrderByTimeDesc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.of(prevLocation));
        when(courierLocationRepository.findTopByCourierAndTimeGreaterThanOrderByTimeAsc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.of(nextLocation));
        
        when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                .thenReturn(distanceCalculationStrategy);
        
        when(distanceCalculationStrategy.calculate(41.0, 29.0, 39.0, 29.0)).thenReturn(200.0);
        when(distanceCalculationStrategy.calculate(41.0, 29.0, 40.0, 29.0)).thenReturn(111.0);
        when(distanceCalculationStrategy.calculate(40.0, 29.0, 39.0, 29.0)).thenReturn(90.0);

        testCourier.setTotalDistance(300.0);
        
        courierTrackingService.logLocation(locationRequest);

        assertEquals(301.0, testCourier.getTotalDistance()); 
        verify(courierRepository).save(testCourier);
    }

    @Test
    void logLocation_ShouldCalculateDistance_WhenOnlyNextLocationExists() {
        CourierLocation nextLocation = CourierLocation.builder()
                .courier(testCourier)
                .lat(39.0)
                .lng(29.0)
                .time(locationRequest.getTime().plusMinutes(5))
                .build();

        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCourier));
        when(courierLocationMapper.toEntity(locationRequest)).thenReturn(courierLocation);
        when(courierLocationRepository.existsByCourierAndTime(testCourier, locationRequest.getTime())).thenReturn(false);
        when(courierLocationRepository.save(any(CourierLocation.class))).thenReturn(courierLocation);
        when(courierLocationRepository.findTopByCourierAndTimeLessThanOrderByTimeDesc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.empty());
        when(courierLocationRepository.findTopByCourierAndTimeGreaterThanOrderByTimeAsc(testCourier, locationRequest.getTime()))
                .thenReturn(Optional.of(nextLocation));
        
        when(distanceStrategyFactory.getStrategy(DistanceStrategyType.HAVERSINE))
                .thenReturn(distanceCalculationStrategy);
        
        when(distanceCalculationStrategy.calculate(40.0, 29.0, 39.0, 29.0)).thenReturn(90.0);

        testCourier.setTotalDistance(300.0);
        
        courierTrackingService.logLocation(locationRequest);

        assertEquals(390.0, testCourier.getTotalDistance()); 
        verify(courierRepository).save(testCourier);
    }

    @Test
    void logLocation_ShouldThrowException_WhenCourierNotFound() {
        when(courierRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> courierTrackingService.logLocation(locationRequest));
        verify(courierLocationRepository, never()).save(any());
        verify(locationEventPublisher, never()).publish(any());
    }

    @Test
    void getTotalTravelDistance_ShouldReturnDistance_WhenCourierExists() {
        when(courierRepository.findById(1L)).thenReturn(Optional.of(testCourier));

        double distance = courierTrackingService.getTotalTravelDistance(1L);

        assertEquals(100.0, distance);
    }
    
    @Test
    void getTotalTravelDistance_ShouldThrowException_WhenCourierNotFound() {
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> courierTrackingService.getTotalTravelDistance(1L));
    }
}

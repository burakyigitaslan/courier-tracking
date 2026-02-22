package com.casestudy.couriertracking.application.service.implementation;

import com.casestudy.couriertracking.api.exception.CourierNotFoundException;
import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.mapper.CourierLocationMapper;
import com.casestudy.couriertracking.application.service.CourierTrackingService;
import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.repository.CourierLocationRepository;
import com.casestudy.couriertracking.domain.repository.CourierRepository;
import com.casestudy.couriertracking.domain.strategy.DistanceCalculationStrategy;
import com.casestudy.couriertracking.domain.strategy.DistanceStrategyFactory;
import com.casestudy.couriertracking.domain.strategy.enumeration.DistanceStrategyType;
import com.casestudy.couriertracking.infrastructure.messaging.LocationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for courier tracking operations:
 * - Logging courier locations, updating totalDistance, and publishing events
 * - Returning total distance from the Courier entity
 */
@Service
@RequiredArgsConstructor
public class CourierTrackingServiceImpl implements CourierTrackingService {
    private final CourierLocationRepository courierLocationRepository;
    private final CourierRepository courierRepository;
    private final LocationEventPublisher locationEventPublisher;
    private final CourierLocationMapper courierLocationMapper;
    private final DistanceStrategyFactory distanceStrategyFactory;

    @Value("${courier.tracking.distance-strategy:HAVERSINE}")
    private DistanceStrategyType distanceStrategyType;

    /**
     * Saves a new courier location, incrementally updates the courier's totalDistance
     * and publishes a location event
     * 
     * @param locationRequestDTO (time, courierId, lat, lng)
     * @return CourierLocation (id, courierId, lat, lng, time)
     * @throws CourierNotFoundException if courier not found
     */
    @Override
    @Transactional
    public CourierLocation logLocation(LocationRequestDTO locationRequestDTO) {
        Courier courier = courierRepository.findByIdWithLock(locationRequestDTO.getCourierId())
                .orElseThrow(() -> new CourierNotFoundException(locationRequestDTO.getCourierId()));

        CourierLocation location = courierLocationMapper.toEntity(locationRequestDTO);
        location.setCourier(courier);

        if (isDuplicateLocation(courier, location)) {
            return courierLocationRepository.findTopByCourierOrderByTimeDesc(courier).orElse(null); 
        }

        CourierLocation saved = courierLocationRepository.save(location);

        processLocationDistanceUpdates(courier, saved);

        locationEventPublisher.publish(saved);

        return saved;
    }

    private boolean isDuplicateLocation(Courier courier, CourierLocation location) {
        return courierLocationRepository.existsByCourierAndTime(courier, location.getTime());
    }

    private void processLocationDistanceUpdates(Courier courier, CourierLocation saved) {
        Optional<CourierLocation> previousLocation = courierLocationRepository
                .findTopByCourierAndTimeLessThanOrderByTimeDesc(courier, saved.getTime());
        Optional<CourierLocation> nextLocation = courierLocationRepository
                .findTopByCourierAndTimeGreaterThanOrderByTimeAsc(courier, saved.getTime());

        if (previousLocation.isPresent() || nextLocation.isPresent()) {
            updateCourierDistance(courier, saved, previousLocation, nextLocation);
        }
    }

    /**
     * Updates the courier's total distance based on the new location.
     * Out of order locations are handled.
     * 
     * @param courier (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @param saved (id, courier, lat, lng, time)
     * @param previousLocation (id, courier, lat, lng, time)
     * @param nextLocation (id, courier, lat, lng, time)
     */
    private void updateCourierDistance(Courier courier, CourierLocation saved, 
                                       Optional<CourierLocation> previousLocation, 
                                       Optional<CourierLocation> nextLocation) {
        DistanceCalculationStrategy strategy = distanceStrategyFactory.getStrategy(distanceStrategyType);
        double currentDistance = courier.getTotalDistance();
        
        if (previousLocation.isPresent() && nextLocation.isPresent()) {
            CourierLocation prev = previousLocation.get();
            CourierLocation next = nextLocation.get();
            double oldDistance = strategy.calculate(prev.getLat(), prev.getLng(), next.getLat(), next.getLng());
            double newDistance1 = strategy.calculate(prev.getLat(), prev.getLng(), saved.getLat(), saved.getLng());
            double newDistance2 = strategy.calculate(saved.getLat(), saved.getLng(), next.getLat(), next.getLng());
            courier.setTotalDistance(currentDistance - oldDistance + newDistance1 + newDistance2);
        } else if (previousLocation.isPresent()) {
            CourierLocation prev = previousLocation.get();
            double distance = strategy.calculate(prev.getLat(), prev.getLng(), saved.getLat(), saved.getLng());
            courier.setTotalDistance(currentDistance + distance);
        } else if (nextLocation.isPresent()) {
            CourierLocation next = nextLocation.get();
            double distance = strategy.calculate(saved.getLat(), saved.getLng(), next.getLat(), next.getLng());
            courier.setTotalDistance(currentDistance + distance);
        }
        courierRepository.save(courier);
    }

    /**
     * Returns the total travel distance for a courier
     *
     * @param courierId
     * @return Total distance in meters
     */
    @Override
    public double getTotalTravelDistance(Long courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        return courier.getTotalDistance();
    }
}

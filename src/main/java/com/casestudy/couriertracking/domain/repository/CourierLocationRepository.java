package com.casestudy.couriertracking.domain.repository;

import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for courier location data points
 */
@Repository
public interface CourierLocationRepository extends JpaRepository<CourierLocation, Long> {
    /**
     * Finds the latest location for a courier, ordered by time descending
     *
     * @param courier (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @return Optional<CourierLocation> (id, courier, lat, lng, time)
     */
    Optional<CourierLocation> findTopByCourierOrderByTimeDesc(Courier courier);

    /**
     * Finds the latest location before a specific time
     * 
     * @param courier (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @param time
     * @return Optional<CourierLocation> (id, courier, lat, lng, time)
     */
    Optional<CourierLocation> findTopByCourierAndTimeLessThanOrderByTimeDesc(Courier courier, LocalDateTime time);

    /**
     * Finds the earliest location after a specific time
     * 
     * @param courier (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @param time
     * @return Optional<CourierLocation> (id, courier, lat, lng, time)
     */
    Optional<CourierLocation> findTopByCourierAndTimeGreaterThanOrderByTimeAsc(Courier courier, LocalDateTime time);

    /**
     * Checks if a location already exists for a given timestamp
     * 
     * @param courier (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @param time
     * @return boolean
     */
    boolean existsByCourierAndTime(Courier courier, LocalDateTime time);
}

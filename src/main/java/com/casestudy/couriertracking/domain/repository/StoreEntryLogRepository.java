package com.casestudy.couriertracking.domain.repository;

import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.StoreEntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for store entry log events
 */
@Repository
public interface StoreEntryLogRepository extends JpaRepository<StoreEntryLog, Long> {
    /**
     * Finds the most recent store entry for a given courier and store,
     * ordered by entry time descending. Used for the 1-minute debounce check
     * 
     * @param courier   (id, firstName, lastName, phoneNumber, version, totalDistance)
     * @param storeName
     * @return Optional<StoreEntryLog> (id, courier, storeName, entryTime)
     */
    Optional<StoreEntryLog> findTopByCourierAndStoreNameOrderByEntryTimeDesc(
            Courier courier, String storeName);
}

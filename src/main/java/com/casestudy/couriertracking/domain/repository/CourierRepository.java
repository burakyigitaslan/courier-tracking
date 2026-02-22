package com.casestudy.couriertracking.domain.repository;

import com.casestudy.couriertracking.domain.model.Courier;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Courier domain entities
 */
@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    /**
     * Get a courier by id with a PESSIMISTIC_WRITE lock.
     * 
     * @param courierId
     * @return Optional<Courier>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Courier c WHERE c.id = :courierId")
    java.util.Optional<Courier> findByIdWithLock(@Param("courierId") Long courierId);
}

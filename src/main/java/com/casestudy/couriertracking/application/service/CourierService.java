package com.casestudy.couriertracking.application.service;

import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;

import java.util.List;

/**
 * Service interface for courier operations
 */
public interface CourierService {
    /**
     * Creates a new courier
     * 
     * @param courierRequestDTO (firstName, lastName, phoneNumber)
     * @return CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     */
    CourierResponseDTO createCourier(CourierRequestDTO courierRequestDTO);

    /**
     * Get all couriers
     * 
     * @return List<CourierResponseDTO> (id, firstName, lastName, phoneNumber, totalDistance)
     */
    List<CourierResponseDTO> getAllCouriers();

    /**
     * Get a courier by id
     * 
     * @param courierId
     * @return CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     */
    CourierResponseDTO getCourierById(Long courierId);
}

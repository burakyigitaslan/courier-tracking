package com.casestudy.couriertracking.application.service.implementation;

import com.casestudy.couriertracking.api.exception.CourierNotFoundException;
import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.application.mapper.CourierMapper;
import com.casestudy.couriertracking.application.service.CourierService;
import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for courier operations
 */
@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {
    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;

    /**
     * Creates a new courier
     * 
     * @param courierRequestDTO (firstName, lastName, phoneNumber)
     * @return CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     */
    @Override
    @Transactional
    public CourierResponseDTO createCourier(CourierRequestDTO courierRequestDTO) {
        Courier courier = courierMapper.toEntity(courierRequestDTO);
        Courier saved = courierRepository.save(courier);
        return courierMapper.toResponse(saved);
    }

    /**
     * Get all couriers
     * 
     * @return List<CourierResponseDTO> (id, firstName, lastName, phoneNumber, totalDistance)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CourierResponseDTO> getAllCouriers() {
        return courierRepository.findAll().stream()
                .map(courierMapper::toResponse)
                .toList();
    }

    /**
     * Get a courier by id
     * 
     * @param courierId
     * @return CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     */
    @Override
    @Transactional(readOnly = true)
    public CourierResponseDTO getCourierById(Long courierId) {
        return courierRepository.findById(courierId)
                .map(courierMapper::toResponse)
                .orElseThrow(() -> new CourierNotFoundException(courierId));
    }
}

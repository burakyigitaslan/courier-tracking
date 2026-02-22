package com.casestudy.couriertracking.api.controller;

import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.application.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for courier CRUD operations.
 * Couriers must be registered before streaming locations.
 */
@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
public class CourierController {
    private final CourierService courierService;

    /**
     * @endpoint POST /api/v1/courier
     * @description Create a new courier
     * @requestBody CourierRequestDTO (firstName, lastName, phoneNumber)
     * @responseBody CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     * @responseStatus 201
     */
    @PostMapping
    public ResponseEntity<CourierResponseDTO> createCourier(@Valid @RequestBody CourierRequestDTO courierRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courierService.createCourier(courierRequestDTO));
    }

    /**
     * @endpoint GET /api/v1/courier
     * @description Get all couriers
     * @responseBody List<CourierResponseDTO> (id, firstName, lastName, phoneNumber, totalDistance)
     * @responseStatus 200
     */
    @GetMapping
    public ResponseEntity<List<CourierResponseDTO>> getAllCouriers() {
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    /**
     * @endpoint GET /api/v1/courier/{courierId}
     * @description Get a courier by id
     * @pathVariable courierId
     * @responseBody CourierResponseDTO (id, firstName, lastName, phoneNumber, totalDistance)
     * @responseStatus 200
     */
    @GetMapping("/{courierId}")
    public ResponseEntity<CourierResponseDTO> getCourierById(@PathVariable Long courierId) {
        return ResponseEntity.ok(courierService.getCourierById(courierId));
    }
}

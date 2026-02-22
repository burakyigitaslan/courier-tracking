package com.casestudy.couriertracking.api.controller;

import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierLocationResponseDTO;
import com.casestudy.couriertracking.application.mapper.CourierLocationMapper;
import com.casestudy.couriertracking.application.service.CourierTrackingService;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for courier tracking operations.
 * Endpoints for logging locations and querying travel distance.
 */
@RestController
@RequestMapping("/api/v1/courier-tracking")
@RequiredArgsConstructor
public class CourierTrackingController {
    private final CourierTrackingService courierTrackingService;
    private final CourierLocationMapper courierLocationMapper;

    /**
     * @endpoint POST /api/v1/courier-tracking/location
     * @description Log a new courier location
     * @requestBody LocationRequestDTO (time, courierId, lat, lng)
     * @responseBody CourierLocationResponseDTO (id, time, courierId, lat, lng)
     * @responseStatus 201
     */
    @PostMapping("/location")
    public ResponseEntity<CourierLocationResponseDTO> logLocation(@Valid @RequestBody LocationRequestDTO request) {
        CourierLocation saved = courierTrackingService.logLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courierLocationMapper.toResponse(saved));
    }

    /**
     * @endpoint GET /api/v1/courier-tracking/travel-distance/{courierId}
     * @description Get total travel distance
     * @pathVariable courierId
     * @responseBody Total distance in meters
     * @responseStatus 200
     */
    @GetMapping("/travel-distance/{courierId}")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable Long courierId) {
        return ResponseEntity.ok(courierTrackingService.getTotalTravelDistance(courierId));
    }
}

package com.casestudy.couriertracking.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for the location logging endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {
    @NotNull(message = "Time is required")
    private LocalDateTime time;

    @NotNull(message = "CourierId is required")
    private Long courierId;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    private Double lat;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    private Double lng;
}

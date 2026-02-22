package com.casestudy.couriertracking.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for courier location data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierLocationResponseDTO {
    private Long courierId;
    private double lat;
    private double lng;
    private LocalDateTime time;
}

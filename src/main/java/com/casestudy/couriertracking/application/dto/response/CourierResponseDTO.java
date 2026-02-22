package com.casestudy.couriertracking.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for courier data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private double totalDistance;
}

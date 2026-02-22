package com.casestudy.couriertracking.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message DTO for the RabbitMQ queue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage implements Serializable {
    private Long locationId;
}

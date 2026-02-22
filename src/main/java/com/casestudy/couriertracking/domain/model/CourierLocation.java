package com.casestudy.couriertracking.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity representing a single courier geolocation data point
 */
@Entity
@Table(name = "courier_location", indexes = {
        @Index(name = "idx_courier_time", columnList = "courier_id, time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private LocalDateTime time;
}

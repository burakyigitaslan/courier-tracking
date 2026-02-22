package com.casestudy.couriertracking.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity that logs when a courier enters a store's radius
 */
@Entity
@Table(name = "store_entry_log", indexes = {
        @Index(name = "idx_courier_store_time", columnList = "courier_id, storeName, entryTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private LocalDateTime entryTime;
}

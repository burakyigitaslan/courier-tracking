package com.casestudy.couriertracking.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a Migros store location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    private String name;
    private double lat;
    private double lng;
}

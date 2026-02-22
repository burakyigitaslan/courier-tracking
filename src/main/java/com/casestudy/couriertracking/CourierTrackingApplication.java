package com.casestudy.couriertracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CourierTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourierTrackingApplication.class, args);
    }
}

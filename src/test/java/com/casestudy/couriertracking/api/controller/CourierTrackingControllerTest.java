package com.casestudy.couriertracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierLocationResponseDTO;
import com.casestudy.couriertracking.application.mapper.CourierLocationMapper;
import com.casestudy.couriertracking.application.service.CourierTrackingService;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierTrackingController.class)
class CourierTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourierTrackingService courierTrackingService;

    @MockitoBean
    private CourierLocationMapper courierLocationMapper;

    private LocationRequestDTO locationRequest;
    private CourierLocation courierLocation;
    private CourierLocationResponseDTO locationResponse;

    @BeforeEach
    void setUp() {
        locationRequest = new LocationRequestDTO(LocalDateTime.now(), 1L, 40.0, 29.0);
        courierLocation = CourierLocation.builder()
                .lat(40.0)
                .lng(29.0)
                .time(locationRequest.getTime())
                .build();
        locationResponse = new CourierLocationResponseDTO(1L, 40.0, 29.0, locationRequest.getTime());
    }

    @Test
    void logLocation_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        when(courierTrackingService.logLocation(any(LocationRequestDTO.class))).thenReturn(courierLocation);
        when(courierLocationMapper.toResponse(any(CourierLocation.class))).thenReturn(locationResponse);

        mockMvc.perform(post("/api/v1/courier-tracking/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lat").value(40.0));
    }

    @Test
    void logLocation_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        LocationRequestDTO invalidRequest = new LocationRequestDTO(null, null, 1000.0, 29.0);

        mockMvc.perform(post("/api/v1/courier-tracking/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalTravelDistance_ShouldReturnDistance_WhenCourierExists() throws Exception {
        when(courierTrackingService.getTotalTravelDistance(anyLong())).thenReturn(100.0);

        mockMvc.perform(get("/api/v1/courier-tracking/travel-distance/{courierId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }
}

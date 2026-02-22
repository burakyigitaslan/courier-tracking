package com.casestudy.couriertracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casestudy.couriertracking.api.exception.CourierNotFoundException;
import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.application.service.CourierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourierController.class)
class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourierService courierService;

    private CourierResponseDTO courierResponse;
    private CourierRequestDTO courierRequest;

    @BeforeEach
    void setUp() {
        courierResponse = new CourierResponseDTO(1L, "Test", "Courier", "+905551234567", 0.0);
        courierRequest = new CourierRequestDTO("Test", "Courier", "+905551234567");
    }

    @Test
    void createCourier_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        when(courierService.createCourier(any(CourierRequestDTO.class))).thenReturn(courierResponse);

        mockMvc.perform(post("/api/v1/courier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courierRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void createCourier_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        CourierRequestDTO invalidRequest = new CourierRequestDTO("", "", "");

        mockMvc.perform(post("/api/v1/courier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCouriers_ShouldReturnList_WhenCouriersExist() throws Exception {
        when(courierService.getAllCouriers()).thenReturn(List.of(courierResponse));

        mockMvc.perform(get("/api/v1/courier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Test"));
    }

    @Test
    void getCourierById_ShouldReturnCourier_WhenExists() throws Exception {
        when(courierService.getCourierById(1L)).thenReturn(courierResponse);

        mockMvc.perform(get("/api/v1/courier/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getCourierById_ShouldReturnNotFound_WhenCourierDoesNotExist() throws Exception {
        when(courierService.getCourierById(1L)).thenThrow(new CourierNotFoundException(1L));

        mockMvc.perform(get("/api/v1/courier/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}

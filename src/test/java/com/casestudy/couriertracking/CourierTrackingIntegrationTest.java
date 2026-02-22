package com.casestudy.couriertracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.casestudy.couriertracking.domain.repository.CourierLocationRepository;
import com.casestudy.couriertracking.infrastructure.listener.StoreProximityListener;
import com.casestudy.couriertracking.infrastructure.messaging.LocationMessage;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourierTrackingIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Autowired
        private CourierLocationRepository courierLocationRepository;

        @Autowired
        private StoreProximityListener storeProximityListener;

        @BeforeEach
        void setUp() {
                reset(rabbitTemplate);
                doAnswer(invocation -> {
                        Object arg = invocation.getArgument(2);
                        if (arg instanceof LocationMessage msg) {
                                courierLocationRepository.findById(msg.getLocationId())
                                                .ifPresent(storeProximityListener::onLocationCreated);
                        }
                        return null;
                }).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        private static Long testCourierId;

        @Test
        @Order(1)
        void shouldCreateCourier() throws Exception {
                CourierRequestDTO request = new CourierRequestDTO("Test", "Courier", "+905551234567");

                String response = mockMvc.perform(post("/api/v1/courier")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.firstName").value("Test"))
                                .andExpect(jsonPath("$.lastName").value("Courier"))
                                .andExpect(jsonPath("$.phoneNumber").value("+905551234567"))
                                .andExpect(jsonPath("$.totalDistance").value(0.0))
                                .andReturn().getResponse().getContentAsString();

                testCourierId = objectMapper.readTree(response).get("id").asLong();
        }

        @Test
        @Order(2)
        void shouldReturnZeroDistanceForNewCourier() throws Exception {
                mockMvc.perform(get("/api/v1/courier-tracking/travel-distance/{courierId}", testCourierId))
                                .andExpect(status().isOk())
                                .andExpect(content().string("0.0"));
        }

        @Test
        @Order(3)
        void shouldLogLocationAndReturn201() throws Exception {
                LocationRequestDTO request = new LocationRequestDTO(
                                LocalDateTime.of(2024, 1, 1, 10, 0),
                                testCourierId, 40.9923307, 29.1244229);

                mockMvc.perform(post("/api/v1/courier-tracking/location")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.courierId").value(testCourierId))
                                .andExpect(jsonPath("$.lat").value(40.9923307))
                                .andExpect(jsonPath("$.lng").value(29.1244229));
        }

        @Test
        @Order(4)
        void shouldCalculateTravelDistanceAfterMultipleLocations() throws Exception {
                CourierRequestDTO courierReq = new CourierRequestDTO("Distance", "Tester", "+905559999999");
                String courierResponse = mockMvc.perform(post("/api/v1/courier")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(courierReq)))
                                .andReturn().getResponse().getContentAsString();
                Long courierId = objectMapper.readTree(courierResponse).get("id").asLong();

                LocationRequestDTO loc1 = new LocationRequestDTO(
                                LocalDateTime.of(2024, 1, 1, 10, 0),
                                courierId, 40.9923307, 29.1244229);
                LocationRequestDTO loc2 = new LocationRequestDTO(
                                LocalDateTime.of(2024, 1, 1, 10, 5),
                                courierId, 40.986106, 29.1161293);
                LocationRequestDTO loc3 = new LocationRequestDTO(
                                LocalDateTime.of(2024, 1, 1, 10, 10),
                                courierId, 40.9632463, 29.0630908);

                for (LocationRequestDTO loc : new LocationRequestDTO[] { loc1, loc2, loc3 }) {
                        mockMvc.perform(post("/api/v1/courier-tracking/location")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loc)))
                                        .andExpect(status().isCreated());
                }

                mockMvc.perform(get("/api/v1/courier-tracking/travel-distance/{courierId}", courierId))
                                .andExpect(status().isOk())
                                .andExpect(result -> {
                                        double distance = Double.parseDouble(result.getResponse().getContentAsString());
                                        assert distance > 1000 : "Expected distance > 1000m, got: " + distance;
                                });
        }

        @Test
        @Order(5)
        void shouldReturn404ForNonExistentCourier() throws Exception {
                mockMvc.perform(get("/api/v1/courier-tracking/travel-distance/{courierId}", 999999L))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(6)
        void shouldReturnBadRequestForInvalidLocationRequest() throws Exception {
                String invalidJson = "{\"time\": null, \"courierId\": null}";

                mockMvc.perform(post("/api/v1/courier-tracking/location")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        void shouldReturn404WhenLoggingLocationForNonExistentCourier() throws Exception {
                LocationRequestDTO request = new LocationRequestDTO(
                                LocalDateTime.of(2024, 1, 1, 10, 0),
                                999999L, 40.9923307, 29.1244229);

                mockMvc.perform(post("/api/v1/courier-tracking/location")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(8)
        void shouldHandleConcurrentLocationUpdates() throws Exception {
                CourierRequestDTO courierReq = new CourierRequestDTO("Concurrent", "Tester", "+905558888888");
                String courierResponse = mockMvc.perform(post("/api/v1/courier")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(courierReq)))
                                .andReturn().getResponse().getContentAsString();
                Long courierId = objectMapper.readTree(courierResponse).get("id").asLong();

                int numRequests = 20;
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(numRequests);
                java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();

                for (int i = 0; i < numRequests; i++) {
                        final int index = i;
                        futures.add(executor.submit(() -> {
                                try {
                                        LocationRequestDTO loc = new LocationRequestDTO(
                                                        LocalDateTime.of(2024, 1, 2, 10, index % 60, index / 60), 
                                                        courierId, 40.0 + (index * 0.001), 29.0);
                                        mockMvc.perform(post("/api/v1/courier-tracking/location")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(loc)))
                                                        .andExpect(status().isCreated());
                                } catch (Exception e) {
                                        throw new RuntimeException(e);
                                } finally {
                                        latch.countDown();
                                }
                        }));
                }

                latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
                
                for (java.util.concurrent.Future<?> f : futures) {
                        f.get();
                }

                mockMvc.perform(get("/api/v1/courier-tracking/travel-distance/{courierId}", courierId))
                                .andExpect(status().isOk())
                                .andExpect(result -> {
                                        double distance = Double.parseDouble(result.getResponse().getContentAsString());
                                        assert distance > 0 : "Expected distance > 0m, got: " + distance;
                                });
                executor.shutdown();
        }

        @TestConfiguration
        static class TestConfig {

                @Bean
                @Primary
                public ConnectionFactory connectionFactory() {
                        return mock(ConnectionFactory.class);
                }

                @Bean
                @Primary
                public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
                        return mock(RabbitTemplate.class);
                }
        }
}

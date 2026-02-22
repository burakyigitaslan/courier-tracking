package com.casestudy.couriertracking.application.mapper;

import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.domain.model.Courier;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CourierMapperTest {

    private final CourierMapper mapper = Mappers.getMapper(CourierMapper.class);

    @Test
    void toEntity_ShouldMapCorrectly() {
        CourierRequestDTO request = new CourierRequestDTO("John", "Doe", "555-1234");
        Courier courier = mapper.toEntity(request);

        assertNotNull(courier);
        assertEquals("John", courier.getFirstName());
        assertEquals("Doe", courier.getLastName());
        assertEquals("555-1234", courier.getPhoneNumber());
    }

    @Test
    void toResponse_ShouldMapCorrectly() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setFirstName("John");
        courier.setLastName("Doe");
        courier.setPhoneNumber("555-1234");
        courier.setTotalDistance(100.5);

        CourierResponseDTO response = mapper.toResponse(courier);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("555-1234", response.getPhoneNumber());
        assertEquals(100.5, response.getTotalDistance());
    }
}

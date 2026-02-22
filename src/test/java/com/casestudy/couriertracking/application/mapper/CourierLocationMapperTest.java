package com.casestudy.couriertracking.application.mapper;

import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierLocationResponseDTO;
import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CourierLocationMapperTest {

    private final CourierLocationMapper mapper = Mappers.getMapper(CourierLocationMapper.class);

    @Test
    void toEntity_ShouldMapCorrectly() {
        LocationRequestDTO request = new LocationRequestDTO();
        request.setLat(40.0);
        request.setLng(29.0);

        CourierLocation location = mapper.toEntity(request);

        assertNotNull(location);
        assertEquals(40.0, location.getLat());
        assertEquals(29.0, location.getLng());
    }

    @Test
    void toResponse_ShouldMapCorrectly() {
        Courier courier = new Courier();
        courier.setId(1L);
        CourierLocation location = new CourierLocation();
        location.setId(100L);
        location.setCourier(courier);
        location.setLat(40.0);
        location.setLng(29.0);

        CourierLocationResponseDTO response = mapper.toResponse(location);

        assertNotNull(response);
        assertEquals(1L, response.getCourierId());
        assertEquals(40.0, response.getLat());
        assertEquals(29.0, response.getLng());
    }
}

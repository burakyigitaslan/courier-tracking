package com.casestudy.couriertracking.application.mapper;

import com.casestudy.couriertracking.application.dto.request.LocationRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierLocationResponseDTO;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between CourierLocation entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface CourierLocationMapper {
    /**
     * Maps a LocationRequestDTO to a CourierLocation entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courier", ignore = true)
    CourierLocation toEntity(LocationRequestDTO request);

    /**
     * Maps a CourierLocation entity to a CourierLocationResponseDTO
     */
    @Mapping(source = "courier.id", target = "courierId")
    CourierLocationResponseDTO toResponse(CourierLocation entity);
}

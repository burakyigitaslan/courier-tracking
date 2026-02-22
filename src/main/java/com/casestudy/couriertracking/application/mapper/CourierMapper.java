package com.casestudy.couriertracking.application.mapper;

import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.domain.model.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Courier entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface CourierMapper {
    /**
     * Maps a CourierRequestDTO to a Courier entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalDistance", ignore = true)
    Courier toEntity(CourierRequestDTO request);

    /**
     * Maps a Courier entity to a CourierResponseDTO
     */
    CourierResponseDTO toResponse(Courier entity);
}

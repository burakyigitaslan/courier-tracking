package com.casestudy.couriertracking.application.service.implementation;

import com.casestudy.couriertracking.api.exception.CourierNotFoundException;
import com.casestudy.couriertracking.application.dto.request.CourierRequestDTO;
import com.casestudy.couriertracking.application.dto.response.CourierResponseDTO;
import com.casestudy.couriertracking.application.mapper.CourierMapper;
import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceImplTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private CourierMapper courierMapper;

    @InjectMocks
    private CourierServiceImpl courierService;

    @Test
    void createCourier_ShouldSaveAndReturnCourier() {
        CourierRequestDTO request = new CourierRequestDTO("John", "Doe", "555-1234");
        Courier courier = new Courier();
        Courier savedCourier = new Courier();
        savedCourier.setId(1L);
        CourierResponseDTO response = new CourierResponseDTO(1L, "John", "Doe", "555-1234", 0.0);

        when(courierMapper.toEntity(request)).thenReturn(courier);
        when(courierRepository.save(courier)).thenReturn(savedCourier);
        when(courierMapper.toResponse(savedCourier)).thenReturn(response);

        CourierResponseDTO result = courierService.createCourier(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(courierRepository).save(courier);
    }

    @Test
    void getAllCouriers_ShouldReturnList() {
        Courier courier = new Courier();
        CourierResponseDTO response = new CourierResponseDTO(1L, "John", "Doe", "555-1234", 0.0);

        when(courierRepository.findAll()).thenReturn(List.of(courier));
        when(courierMapper.toResponse(courier)).thenReturn(response);

        List<CourierResponseDTO> result = courierService.getAllCouriers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courierRepository).findAll();
    }

    @Test
    void getCourierById_ShouldReturnCourier_WhenExists() {
        Long id = 1L;
        Courier courier = new Courier();
        CourierResponseDTO response = new CourierResponseDTO(id, "John", "Doe", "555-1234", 0.0);

        when(courierRepository.findById(id)).thenReturn(Optional.of(courier));
        when(courierMapper.toResponse(courier)).thenReturn(response);

        CourierResponseDTO result = courierService.getCourierById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getCourierById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(courierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> courierService.getCourierById(id));
    }
}

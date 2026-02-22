package com.casestudy.couriertracking.infrastructure.messaging;

import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.repository.CourierLocationRepository;
import com.casestudy.couriertracking.infrastructure.listener.StoreProximityListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationMessageConsumerTest {

    @Mock
    private CourierLocationRepository courierLocationRepository;

    @Mock
    private StoreProximityListener storeProximityListener;

    @InjectMocks
    private LocationMessageConsumer consumer;

    private CourierLocation buildLocation(Long id) {
        Courier courier = Courier.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Courier")
                .phoneNumber("+905551234567")
                .build();
        return CourierLocation.builder()
                .id(id)
                .courier(courier)
                .lat(40.9923307)
                .lng(29.1244229)
                .time(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();
    }

    @Test
    void consume_ShouldCallStoreProximityListener_WhenLocationFound() {
        CourierLocation location = buildLocation(100L);
        when(courierLocationRepository.findById(100L)).thenReturn(Optional.of(location));

        consumer.consume(new LocationMessage(100L));

        verify(storeProximityListener).onLocationCreated(location);
    }

    @Test
    void consume_ShouldSkipProcessing_WhenLocationNotFound() {
        when(courierLocationRepository.findById(999L)).thenReturn(Optional.empty());

        consumer.consume(new LocationMessage(999L));

        verify(storeProximityListener, never()).onLocationCreated(any());
    }

    @Test
    void consume_ShouldCallRepositoryWithCorrectId() {
        CourierLocation location = buildLocation(55L);
        when(courierLocationRepository.findById(55L)).thenReturn(Optional.of(location));

        consumer.consume(new LocationMessage(55L));

        verify(courierLocationRepository).findById(55L);
    }

    @Test
    void consume_ShouldProcessEachMessageIndependently() {
        CourierLocation loc1 = buildLocation(1L);
        CourierLocation loc2 = buildLocation(2L);

        when(courierLocationRepository.findById(1L)).thenReturn(Optional.of(loc1));
        when(courierLocationRepository.findById(2L)).thenReturn(Optional.of(loc2));

        consumer.consume(new LocationMessage(1L));
        consumer.consume(new LocationMessage(2L));

        verify(storeProximityListener).onLocationCreated(loc1);
        verify(storeProximityListener).onLocationCreated(loc2);
    }

    @Test
    void consume_ShouldNotCallProximityListener_WhenMessageIdIsNull() {
        when(courierLocationRepository.findById(null)).thenReturn(Optional.empty());

        consumer.consume(new LocationMessage(null));

        verify(storeProximityListener, never()).onLocationCreated(any());
    }

    @Test
    void consume_ShouldThrowAmqpRejectAndDontRequeue_WhenExceptionOccurs() {
        when(courierLocationRepository.findById(100L)).thenThrow(new RuntimeException("DB error"));

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.consume(new LocationMessage(100L)));

        verify(storeProximityListener, never()).onLocationCreated(any());
    }
}


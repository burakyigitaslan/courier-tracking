package com.casestudy.couriertracking.infrastructure.messaging;

import com.casestudy.couriertracking.domain.model.Courier;
import com.casestudy.couriertracking.domain.model.CourierLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQLocationEventPublisherImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQLocationEventPublisherImpl publisher;

    private CourierLocation buildLocation(Long locationId, Long courierId) {
        Courier courier = new Courier();
        courier.setId(courierId);
        CourierLocation location = new CourierLocation();
        location.setId(locationId);
        location.setCourier(courier);
        location.setLat(40.0);
        location.setLng(29.0);
        return location;
    }

    @Test
    void publish_ShouldSendToRabbitMQ() {
        CourierLocation location = buildLocation(100L, 1L);

        publisher.publish(location);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQLocationEventPublisherImpl.EXCHANGE_NAME),
                eq(RabbitMQLocationEventPublisherImpl.ROUTING_KEY),
                org.mockito.ArgumentMatchers.any(LocationMessage.class));
    }

    @Test
    void publish_ShouldSendCorrectLocationId() {
        CourierLocation location = buildLocation(42L, 7L);

        publisher.publish(location);

        ArgumentCaptor<LocationMessage> captor = ArgumentCaptor.forClass(LocationMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQLocationEventPublisherImpl.EXCHANGE_NAME),
                eq(RabbitMQLocationEventPublisherImpl.ROUTING_KEY),
                captor.capture());

        assertEquals(42L, captor.getValue().getLocationId());
    }

    @Test
    void publish_ShouldUseCorrectExchangeAndRoutingKey() {
        CourierLocation location = buildLocation(1L, 1L);

        publisher.publish(location);

        verify(rabbitTemplate).convertAndSend(
                eq("courier.location.exchange"),
                eq("courier.location.routing-key"),
                org.mockito.ArgumentMatchers.any(LocationMessage.class));
    }
}

package com.casestudy.couriertracking.infrastructure.messaging;

import com.casestudy.couriertracking.domain.model.CourierLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ location event publisher
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQLocationEventPublisherImpl implements LocationEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE_NAME = "courier.location.exchange";
    public static final String ROUTING_KEY = "courier.location.routing-key";

    /**
     * Publishes a location event
     *
     * @param courierLocation (id, courier, lat, lng, time)
     */
    @Override
    public void publish(CourierLocation courierLocation) {
        LocationMessage message = new LocationMessage(courierLocation.getId());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
        log.info("Published location {} to RabbitMQ for courier {}",
                courierLocation.getId(), courierLocation.getCourier().getId());
    }
}

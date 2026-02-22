package com.casestudy.couriertracking.infrastructure.messaging;

import com.casestudy.couriertracking.domain.model.CourierLocation;
import com.casestudy.couriertracking.domain.repository.CourierLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.casestudy.couriertracking.infrastructure.listener.StoreProximityListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for location messages
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationMessageConsumer {
    private final CourierLocationRepository courierLocationRepository;
    private final StoreProximityListener storeProximityListener;

    public static final String QUEUE_NAME = "courier.location.queue";

    @RabbitListener(queues = QUEUE_NAME)
    public void consume(LocationMessage message) {
        log.info("Received location message from RabbitMQ: locationId={}", message.getLocationId());

        try {
            courierLocationRepository.findById(message.getLocationId())
                    .ifPresentOrElse(
                            this::processLocation,
                            () -> log.warn("Location not found for id={}, skipping", message.getLocationId()));
        } catch (Exception e) {
            log.error("Failed to process location message locationId={}: {}",
                    message.getLocationId(), e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(
                    "Permanent failure for locationId=" + message.getLocationId(), e);
        }
    }

    private void processLocation(CourierLocation location) {
        storeProximityListener.onLocationCreated(location);
    }
}

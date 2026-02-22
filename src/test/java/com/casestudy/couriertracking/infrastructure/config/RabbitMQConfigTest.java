package com.casestudy.couriertracking.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(RabbitMQConfig.class)
class RabbitMQConfigTest {

    @Autowired
    private Queue locationQueue;

    @Autowired
    private DirectExchange locationExchange;

    @Autowired
    private Binding locationBinding;

    @Autowired
    private MessageConverter messageConverter;

    @Test
    void queue_shouldHaveCorrectName() {
        assertThat(locationQueue.getName())
                .isEqualTo(RabbitMQConfig.QUEUE_NAME);
    }

    @Test
    void queue_shouldBeDurable() {
        assertThat(locationQueue.isDurable()).isTrue();
    }

    @Test
    void queue_shouldNotBeExclusiveOrAutoDelete() {
        assertThat(locationQueue.isExclusive()).isFalse();
        assertThat(locationQueue.isAutoDelete()).isFalse();
    }

    @Test
    void exchange_shouldHaveCorrectName() {
        assertThat(locationExchange.getName())
                .isEqualTo(RabbitMQConfig.EXCHANGE_NAME);
    }

    @Test
    void exchange_shouldBeDirectExchange() {
        assertThat(locationExchange).isInstanceOf(DirectExchange.class);
    }

    @Test
    void binding_shouldHaveCorrectRoutingKey() {
        assertThat(locationBinding.getRoutingKey())
                .isEqualTo(RabbitMQConfig.ROUTING_KEY);
    }

    @Test
    void binding_shouldPointToCorrectExchange() {
        assertThat(locationBinding.getExchange())
                .isEqualTo(RabbitMQConfig.EXCHANGE_NAME);
    }

    @Test
    void binding_shouldBindCorrectQueue() {
        assertThat(locationBinding.getDestination())
                .isEqualTo(RabbitMQConfig.QUEUE_NAME);
    }

    @Test
    void binding_destinationTypeShouldBeQueue() {
        assertThat(locationBinding.getDestinationType())
                .isEqualTo(Binding.DestinationType.QUEUE);
    }

    @Test
    void messageConverter_shouldBeJackson2JsonMessageConverter() {
        assertThat(messageConverter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    void constants_shouldNotBeBlank() {
        assertThat(RabbitMQConfig.QUEUE_NAME).isNotBlank();
        assertThat(RabbitMQConfig.EXCHANGE_NAME).isNotBlank();
        assertThat(RabbitMQConfig.ROUTING_KEY).isNotBlank();
    }

    @Test
    void constants_shouldAllBeDifferent() {
        assertThat(RabbitMQConfig.QUEUE_NAME)
                .isNotEqualTo(RabbitMQConfig.EXCHANGE_NAME)
                .isNotEqualTo(RabbitMQConfig.ROUTING_KEY);
        assertThat(RabbitMQConfig.EXCHANGE_NAME)
                .isNotEqualTo(RabbitMQConfig.ROUTING_KEY);
    }

    @Autowired
    @Qualifier("deadLetterQueue")
    private Queue deadLetterQueue;

    @Autowired
    @Qualifier("deadLetterExchange")
    private DirectExchange deadLetterExchange;

    @Autowired
    @Qualifier("deadLetterBinding")
    private Binding deadLetterBinding;

    @Test
    void locationQueue_shouldHaveDLQArguments() {
        assertThat(locationQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DLX_NAME)
                .containsEntry("x-dead-letter-routing-key", RabbitMQConfig.DLQ_ROUTING_KEY);
    }

    @Test
    void dlq_shouldHaveCorrectName() {
        assertThat(deadLetterQueue.getName())
                .isEqualTo(RabbitMQConfig.DLQ_NAME);
    }

    @Test
    void dlq_shouldBeDurable() {
        assertThat(deadLetterQueue.isDurable()).isTrue();
    }

    @Test
    void dlx_shouldHaveCorrectName() {
        assertThat(deadLetterExchange.getName())
                .isEqualTo(RabbitMQConfig.DLX_NAME);
    }

    @Test
    void dlx_shouldBeDirectExchange() {
        assertThat(deadLetterExchange).isInstanceOf(DirectExchange.class);
    }

    @Test
    void dlqBinding_shouldHaveCorrectRoutingKey() {
        assertThat(deadLetterBinding.getRoutingKey())
                .isEqualTo(RabbitMQConfig.DLQ_ROUTING_KEY);
    }

    @Test
    void dlqBinding_shouldPointToCorrectExchange() {
        assertThat(deadLetterBinding.getExchange())
                .isEqualTo(RabbitMQConfig.DLX_NAME);
    }

    @Test
    void dlqBinding_shouldBindCorrectQueue() {
        assertThat(deadLetterBinding.getDestination())
                .isEqualTo(RabbitMQConfig.DLQ_NAME);
    }
}


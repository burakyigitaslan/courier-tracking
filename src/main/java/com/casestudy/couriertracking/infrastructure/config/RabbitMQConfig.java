package com.casestudy.couriertracking.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration
 */
@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "courier.location.queue";
    public static final String EXCHANGE_NAME = "courier.location.exchange";
    public static final String ROUTING_KEY = "courier.location.routing-key";
    public static final String DLQ_NAME = "courier.location.dlq";
    public static final String DLX_NAME = "courier.location.dlx";
    public static final String DLQ_ROUTING_KEY = "courier.location.dlq.routing-key";

    @Bean
    public Queue locationQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange locationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding locationBinding(@Qualifier("locationQueue") Queue locationQueue, DirectExchange locationExchange) {
        return BindingBuilder.bind(locationQueue).to(locationExchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    @Bean
    public Binding deadLetterBinding(@Qualifier("deadLetterQueue") Queue deadLetterQueue, @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}

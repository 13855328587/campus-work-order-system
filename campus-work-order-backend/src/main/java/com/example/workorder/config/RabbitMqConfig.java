package com.example.workorder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMqConfig {
    public static final String WORK_ORDER_EXCHANGE = "campus.work-order.exchange";
    public static final String WORK_ORDER_QUEUE = "campus.work-order.events";
    public static final String WORK_ORDER_ROUTING_KEY = "work-order.changed";
    public static final String DEAD_LETTER_EXCHANGE = "campus.work-order.dlx";
    public static final String DEAD_LETTER_QUEUE = "campus.work-order.events.dead";
    public static final String DEAD_LETTER_ROUTING_KEY = "work-order.changed.dead";

    @Bean
    public DirectExchange workOrderExchange() {
        return new DirectExchange(WORK_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue workOrderQueue() {
        return QueueBuilder.durable(WORK_ORDER_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding workOrderBinding(
            @Qualifier("workOrderQueue") Queue workOrderQueue,
            @Qualifier("workOrderExchange") DirectExchange workOrderExchange
    ) {
        return BindingBuilder.bind(workOrderQueue).to(workOrderExchange).with(WORK_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(
            @Qualifier("deadLetterQueue") Queue deadLetterQueue,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

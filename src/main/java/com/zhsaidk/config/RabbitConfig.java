package com.zhsaidk.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE = "email_queue";
    public static final String EXCHANGE = "email_exchange";

    @Bean
    public Queue emailqQueue(){
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public DirectExchange emailExchange(){
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue emailQueue, DirectExchange emailExchange){
        return BindingBuilder.bind(emailQueue).to(emailExchange).with("routing.key");
    }

    @Bean
    public Jackson2JsonMessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

}
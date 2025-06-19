package com.zhsaidk.service;

import com.zhsaidk.config.RabbitConfig;
import com.zhsaidk.dto.MailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendEmailMessage(String to, String message){
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "routing.key", new MailMessage(to, message));
    }
}
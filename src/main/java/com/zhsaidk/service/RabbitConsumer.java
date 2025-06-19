package com.zhsaidk.service;

import com.zhsaidk.config.RabbitConfig;
import com.zhsaidk.dto.MailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitConsumer {
    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleEmail(MailMessage mailMessage){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("Notify");
        simpleMailMessage.setTo(mailMessage.getTo());
        simpleMailMessage.setText(mailMessage.getMessage());
        mailSender.send(simpleMailMessage);
        log.info("Message sent to: {}", mailMessage.getMessage());
    }
}

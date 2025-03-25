package com.example.addressBook.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {

    @RabbitListener(queues = "user_notifications")
    public void receiveMessage(String message) {
        System.out.println("📩 Received message: " + message);
    }
}

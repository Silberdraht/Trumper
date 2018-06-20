package de.hska.lkit.demo.redis.model.Impl;

import de.hska.lkit.demo.redis.model.MessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class RedisMessagePublisher implements MessagePublisher {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public RedisMessagePublisher() {
    }

    public RedisMessagePublisher(
            SimpMessagingTemplate redisTemplate) {
        this.messagingTemplate = redisTemplate;
    }

    @Override
    public void publish(Message message) {
        messagingTemplate.convertAndSend("own_messages" /*Todo*/, message);
        System.out.println("RedisMessagePublisher: message sent");
    }

}

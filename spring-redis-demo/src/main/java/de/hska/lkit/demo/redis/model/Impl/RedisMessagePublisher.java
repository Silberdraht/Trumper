package de.hska.lkit.demo.redis.model.Impl;

import de.hska.lkit.demo.redis.model.MessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

public class RedisMessagePublisher implements MessagePublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public RedisMessagePublisher() {
    }

    public RedisMessagePublisher(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String message) {
        redisTemplate.convertAndSend("/own_messages", message);
    }
}

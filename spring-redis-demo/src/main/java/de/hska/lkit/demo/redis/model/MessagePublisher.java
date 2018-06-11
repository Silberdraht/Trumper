package de.hska.lkit.demo.redis.model;

public interface MessagePublisher {
    void publish(String message);
}

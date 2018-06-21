package de.hska.lkit.demo.redis.model;

import de.hska.lkit.demo.redis.model.Impl.Message;

public interface MessagePublisher {
    void publish(Message message);
}

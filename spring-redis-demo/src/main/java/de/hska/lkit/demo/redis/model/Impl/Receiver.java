package de.hska.lkit.demo.redis.model.Impl;

import de.hska.lkit.demo.redis.repo.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {

    private final MessageRepository messageRepository;

    @Autowired
    public Receiver(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void receiveMessage(String data) {

        int split = data.indexOf("|");

        int userIdLength = Integer.parseInt(data.substring(0, split));
        String userId = data.substring(split + 1, split + userIdLength + 1);
        String message = data.substring(split + userIdLength + 1);

        messageRepository.insertVeetLocally(userId, message);

    }
}

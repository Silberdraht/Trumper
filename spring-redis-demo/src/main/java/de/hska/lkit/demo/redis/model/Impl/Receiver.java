package de.hska.lkit.demo.redis.model.Impl;

import de.hska.lkit.demo.redis.repo.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {

    private final MessageRepository messageRepository;

    @Autowired
    public Receiver(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void receiveMessage(String m_key) {
        System.out.println(m_key);
        String[] split = m_key.split("m");
        m_key = "m" + split[split.length - 1];
        System.out.println(m_key);
        messageRepository.post(m_key);
    }
}

package de.hska.lkit.demo.redis.repo;

import de.hska.lkit.demo.redis.model.Message;

import java.util.List;
import java.util.Map;

public interface MessageRepository {



    public Message getMessage(String id);

    public void postMessage(String text);

    public List<String> getAllMessages();

    public Map<String, Message> getMessageGlobal();

    public Map<String, Message> getMessageUser(String id);



}

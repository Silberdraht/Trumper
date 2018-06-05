package de.hska.lkit.demo.redis.repo;

import de.hska.lkit.demo.redis.model.Message;
import javafx.collections.transformation.SortedList;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MessageRepository {



    public Message getMessage(String id);


    public void postMessage(String text);

    public List<String> getMessagesAll();

    public List<Message> getMessagesGlobal();

    public List<Message> getMessageFollow(String user);

    public List<String> getMessageUser(String id);

}

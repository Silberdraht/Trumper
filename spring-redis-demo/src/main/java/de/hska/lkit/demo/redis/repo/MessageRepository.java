package de.hska.lkit.demo.redis.repo;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.User;

import java.util.List;
import java.util.Map;

public interface MessageRepository {

    public Message getMessage(String id);

    public Message postMessage(String text, Map<String, User> followers);

    public List<String> getMessageIsDsAll();

    public List<Message> getMessagesGlobal();

    public List<String> getMessageIDsUser(String user);

    List<Message> getMessagesTimeline(String userID);

    /*public void followMessagesFromUser(String uid, String followedUserID);

    public void unfollowMessagesFromUser(String userID, String followedUserID);*/

    List<String> getMessageIDsTimeline(String userID);

}

package de.hska.lkit.demo.redis.repo;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.User;

import java.util.List;
import java.util.Map;

public interface MessageRepository {

    public Message getMessage(String id);

    public void postMessage(String text, Map<String, User> followers);

    public List<String> getMessageIsDsAll();

    List<String> getMessageIDsInRange(int start, int end);

    List<String> getMessageIDsInRange(String userID, int start, int end);

    List<Message> getMessagesInRange(int start, int end, MessageRepository messageRepository);

    List<Message> getMessagesInRange(String userID, int start, int end, MessageRepository messageRepository);

    public List<Message> getMessagesGlobal();

    long countGlobalMessages();

    long countTimelineMessages(String u_id);

    public List<String> getMessageIDsUser(String user);

    List<Message> getMessagesTimeline(String userID);

    /*public void followMessagesFromUser(String uid, String followedUserID);

    public void unfollowMessagesFromUser(String userID, String followedUserID);*/

    List<String> getMessageIDsTimeline(String userID);

}

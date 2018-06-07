package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.SimpleSecurity;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author Silberdraht
 *
 */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

	private static final String KEY_SET_ALL_USERNAMES 	= "all:usernames";

	private static final String KEY_ZSET_ALL_USERNAMES 	= "all:usernames:sorted";

	private static final String KEY_HASH_ALL_USERS 		= "all:user";

	private static final String KEY_HASH_MESSAGE = "m:";

	private static final String KEY_LIST_MESSAGE_GLOBAL = "m_global";

	private static final String KEY_LIST_MESSAGE_USER = "m:user";

	private static final String KEY_FOLLOWING_USER = "m:following:";

	/**
	 * to generate unique ids for message
	 */
	private RedisAtomicLong m_id;

	/**
	 * to save data in String format
	 */
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * to save message data as object
	 */
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * for simple value Operations
	 */

	private ValueOperations<String,String> srt_simpleOps;

	/**
	 * hash operations for stringRedisTemplate
	 */
	private HashOperations<String, String, String> srt_hashOps;

	/**
	 * set operations for stringRedisTemplate
	 */
	private SetOperations<String, String> srt_setOps;

	/**
	 * zset operations for stringRedisTemplate
	 */
	private ZSetOperations<String, String> srt_zSetOps;

    /**
     * list operations for stringRedisTemplate
     */
	private ListOperations<String, String> srt_listOps;

	/**
	 * hash operations for redisTemplate
	 */
	@Resource(name="redisTemplate")
	private HashOperations<String, String, Message> rt_hashOps;


	@Autowired
	public MessageRepositoryImpl(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.m_id = new RedisAtomicLong("m_id", stringRedisTemplate.getConnectionFactory());
	}
	
	@PostConstruct
	private void init() {
		srt_hashOps = stringRedisTemplate.opsForHash();
		srt_setOps = stringRedisTemplate.opsForSet();
		srt_zSetOps = stringRedisTemplate.opsForZSet();
		srt_simpleOps = stringRedisTemplate.opsForValue();
		srt_listOps = stringRedisTemplate.opsForList();
	}

    /**
     * Gets the message object for messageID.
     * @param messageID
     * @return
     */
	@Override
	public Message getMessage(String messageID) {
	    if(!messageID.startsWith(KEY_HASH_MESSAGE))
            messageID = KEY_HASH_MESSAGE + messageID;

		Message message = rt_hashOps.get(KEY_LIST_MESSAGE_GLOBAL, messageID);
		return message;
	}

	@Override
	public void postMessage(String text, Map<String, User> followers) {

		String m_id = String.valueOf(this.m_id.incrementAndGet());
        String u_id = SimpleSecurity.getUid();
        String username = SimpleSecurity.getName();

        String timestamp;
        Object timeObject = redisTemplate.execute(RedisServerCommands::time);
        Date time = new Date((long)timeObject);
        timestamp = time.toString();

		Message message = new Message(m_id, username, timestamp, text);

		String key = KEY_HASH_MESSAGE + m_id;

		srt_hashOps.put(key, "Zeitstempel", message.getTimestamp());
		srt_hashOps.put(key, "Autor", message.getAutor());
		srt_hashOps.put(key, "Inhalt", message.getText());

		//global timeline message ID's (all)
		srt_listOps.leftPush(KEY_LIST_MESSAGE_GLOBAL, key);
        //own timeline messages (own + following)
        srt_listOps.leftPush(KEY_LIST_MESSAGE_USER + u_id, key);
        //only own message ID's
		srt_listOps.leftPush(KEY_LIST_MESSAGE_USER + u_id, key);

		//global timeline messages (all)
		rt_hashOps.put(KEY_LIST_MESSAGE_GLOBAL, key, message);
        //own timeline messages (own + following)
        rt_hashOps.put(KEY_LIST_MESSAGE_USER + u_id, key, message);
        //only own messages
        rt_hashOps.put(KEY_FOLLOWING_USER + u_id, key, message);

        /*//add message to all follower lists
        //here is the Redis.publish to use for signaling incoming message in followers session
        for (User follower : followers.values()) {
            rt_hashOps.put(KEY_LIST_MESSAGE_USER + getIDByKey(follower.getId()), key, message);
        }*/
        //add message to all follower lists
        //here is the Redis.publish to use for signaling incoming message in followers session
        for (User follower : followers.values()) {
            srt_listOps.leftPush(KEY_LIST_MESSAGE_USER + getIDByKey(follower.getId()), key);
        }
    }

    public String getIDByKey(String key) {
        String[] split = key.split(":");
        return split[split.length - 1];
    }

    /**
     * Gets all message ID's from last to first (n -> 1).
     * @return
     */
    @Override
    public List<String> getMessageIsDsAll() {
		return getMessageIDsInRange(0, -1);
	}

    /**
     * Gets all message ID's from end to start (n -> 1).
     * @param start
     * @param end
     * @return
     */
	public List<String> getMessageIDsInRange(int start, int end) {
        return srt_listOps.range(KEY_LIST_MESSAGE_GLOBAL, start, end);
    }

    /**
     * Gets all messages from first to last (1 -> n).
     * @return
     */
	@Override
	public List<Message> getMessagesGlobal() {
		return rt_hashOps.values(KEY_LIST_MESSAGE_GLOBAL);
	}

    /**
     * Gets all message ID's of user from userID from last to first (n -> 1).
     * @param userID
     * @return
     */
	@Override
    public List<String> getMessageIDsUser(String userID) {
		return srt_listOps.range(KEY_LIST_MESSAGE_USER + userID, 0, -1);
	}

	/**
	 * Gets unsorted list of messages of user from userID.
	 * @param userID "user:" + u_id
	 * @return
	 */
	@Override
	public List<Message> getMessagesTimeline(String userID) {
		return rt_hashOps.values(KEY_LIST_MESSAGE_USER + userID);
	}

	@Override
	public List<String> getMessageIDsTimeline(String userID) { return srt_listOps.range(KEY_LIST_MESSAGE_USER + userID, 0, -1); }

	/*
    @Override
    public void followMessagesFromUser(String userID, String followedUserID) {
        String key = KEY_LIST_MESSAGE_USER + userID;
        String valueKey = KEY_FOLLOWING_USER + followedUserID;
        rt_hashOps.putAll(key, rt_hashOps.entries(valueKey));
	}

	@Override
	public void unfollowMessagesFromUser(String userID, String followedUserID) {
		String key = KEY_LIST_MESSAGE_USER + userID;
		String valueKey = KEY_FOLLOWING_USER + followedUserID;
		rt_hashOps.delete(key, rt_hashOps.entries(valueKey));
	}
    */

}
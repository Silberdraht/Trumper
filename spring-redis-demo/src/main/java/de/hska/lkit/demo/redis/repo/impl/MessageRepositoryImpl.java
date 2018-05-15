package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @author Silberdraht
 *
 */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

	/**
	 *
	 */
	private static final String KEY_SET_ALL_USERNAMES 	= "all:usernames";

	private static final String KEY_ZSET_ALL_USERNAMES 	= "all:usernames:sorted";

	private static final String KEY_HASH_ALL_USERS 		= "all:user";

	private static final String KEY_PREFIX_USER 	= "user:";

	private static final String KEY_HASH_MESSAGE = "m:";

	private static final String KEY_LIST_MESSAGE_GLOBAL = "m_global";

	private static final String KEY_LIST_MESSAGE_USER = "m:user:";

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
	 * hash operations for redisTemplate
	 */
	@Resource(name="redisTemplate")
	private HashOperations<String, String, User> rt_hashOps;


	/*
	 *
	 */
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
		//srt_listOps = stringRedisTemplate.opsForList();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hska.iwi.vslab.repo.UserRepository#saveUser(hska.iwi.vslab.model.User)
	 */
	//@Override
	public void saveUser(User user) {
	}


	public MessageRepositoryImpl(String id) {

	}

	@Override
	public Message getMessage(String id) {
		return null;
	}

	@Override
	public void postMessage(String text) {
		Message message = new Message();

		message.setText(text);

		//TO DO
		message.setAutor("Default");

		//TO DO
		message.setTimestamp("202815052018");

		//TO DO
		message.setId("m:1");


		message.setDeleted("0");

		//TO DO Generate unique id
		String id = message.getId();


		// to show how objects can be saved
		// be careful, if username already exists it's not added another time

		//Matze
		String key = message.getId();

		srt_hashOps.put(key, "id", id);
		srt_hashOps.put(key, "Zeitstempel", message.getText());
		srt_hashOps.put(key, "Autor", message.getAutor());
		srt_hashOps.put(key, "Gelöscht", message.getDeleted());
		srt_hashOps.put(key, "Inhalt", message.getText());

		// the key for a new user is added to the set for all usernames
		//srt_setOps.add(KEY_SET_ALL_USERNAMES, user.getUsername());

		// the key for a new user is added to the sorted set for all usernames
		//srt_zSetOps.add(KEY_ZSET_ALL_USERNAMES, user.getUsername(), 0);

		// to show how objects can be saved
		//rt_hashOps.put(KEY_HASH_ALL_USERS, key, user);

		//srt_simpleOps.set("user_count", "1");
		//


	}

	@Override
	public Map<String, Message> getAllMessages() {


		return null;
	}

	@Override
	public Map<String, Message> getMessageGlobal() {


		return null;
	}

	@Override
	public Map<String, Message> getMessageUser(String id) {
		return null;
	}
}
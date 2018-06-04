package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.SimpleSecurity;
import de.hska.lkit.demo.redis.model.User;

import de.hska.lkit.demo.redis.repo.MessageRepository;

import de.hska.lkit.demo.redis.repo.UserRepository;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

	private static final String KEY_LIST_MESSAGE_USER = "m:";


	private static final String KEY_FOLLOWING_USER = "following:";


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

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hska.iwi.vslab.repo.UserRepository#saveUser(hska.iwi.vslab.model.User)
	 */
	//@Override


	public MessageRepositoryImpl(String id) {

	}

	@Override
	public Message getMessage(String id) {
		Message message = new Message();

<<<<<<< HEAD
			//System.out.println("getMessage IF");
            message.setId(id);
=======
>>>>>>> master
			message.setTimestamp(srt_hashOps.get(id, "Zeitstempel"));
			message.setAutor(srt_hashOps.get(id, "Autor"));
			message.setText(srt_hashOps.get(id, "Inhalt"));
			message.setDeleted(srt_hashOps.get(id, "Geloescht"));


		return message;
	}

	@Override
	public void postMessage(String text) {

		//unique id
		String id = String.valueOf(m_id.incrementAndGet());

		Message message = new Message();

		message.setText(text);

		message.setAutor(SimpleSecurity.getName());



		message.setId(id);

		message.setDeleted("0");


		Object timeObject = redisTemplate.execute(RedisServerCommands::time);
		message.setTimestamp(timeObject.toString());

		Date time = new Date((long)timeObject);

		message.setTimestamp(time.toString());


		String key = KEY_HASH_MESSAGE + id;

		srt_hashOps.put(key, "Zeitstempel", message.getTimestamp());
		srt_hashOps.put(key, "Autor", message.getAutor());
		srt_hashOps.put(key, "Geloescht", message.getDeleted());
		srt_hashOps.put(key, "Inhalt", message.getText());

		srt_listOps.leftPush(KEY_LIST_MESSAGE_GLOBAL, key);

		System.out.println("Key für Liste: " + KEY_LIST_MESSAGE_USER + SimpleSecurity.getUid());
		srt_listOps.rightPush(KEY_LIST_MESSAGE_USER + SimpleSecurity.getUid(), key);


	}

	@Override
	public List<String> getAllMessages() {


		return getMessagesInRange(0, -1);
	}

	public List<String> getMessagesInRange(int start, int end) {
        return srt_listOps.range(KEY_LIST_MESSAGE_GLOBAL, start, end);
    }

	@Override
	public Map<String, Message> getMessageGlobal() {

		Map<String, Message> mapResult = new HashMap<>();

		for (String s: getAllMessages()) {

			mapResult.put(s, getMessage(s));


		}

		return mapResult;
	}

	@Override

	public Map<String, Message> getMessageFollow(String user) {

		Map<String, Message> mapMassages = new HashMap<>();

		Set<Object> setUser;
		List<String> listMessage = new ArrayList<>();

		System.out.println("Set Key: "+ KEY_FOLLOWING_USER + user);
		setUser = redisTemplate.opsForSet().members(KEY_FOLLOWING_USER + user);

		System.out.println("getMessageFollow pre for setUser");
		for (Object id : setUser) {


			listMessage.addAll(getMessageUser(id.toString()));
		}
		//Füge eigene Tweets zur persönlichen Timeline hinzu.
		listMessage.addAll(getMessageUser(user));

		System.out.println("getMessageFollow pre for listMessage");
		for (String s: listMessage) {
			mapMassages.put(s, getMessage(s));

		}





		return mapMassages;
	}



	@Override
	public List<String> getMessageUser(String id) {


			System.out.println("SET Key " + KEY_HASH_MESSAGE + id);

			System.out.println(srt_listOps.range(KEY_HASH_MESSAGE + id, 0, -1));

			List<String> messages = srt_listOps.range(KEY_HASH_MESSAGE + id, 0, -1);
		return messages;
	}
}
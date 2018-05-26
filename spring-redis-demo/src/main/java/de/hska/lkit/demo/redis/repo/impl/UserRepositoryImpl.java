package de.hska.lkit.demo.redis.repo.impl;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.UserRepository;

/**
 * @author knad0001
 *
 */
/**
 * @author knad0001
 *
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

	/**
	 * 
	 */
	private static final String KEY_SET_ALL_USERNAMES 	= "all:usernames";

	private static final String KEY_ZSET_ALL_USERNAMES 	= "all:usernames:sorted";
	
	private static final String KEY_HASH_ALL_USERS 		= "all:user";
	
	private static final String KEY_PREFIX_USER 	= "user:";

	/**
	 * to generate unique ids for user
	 */
	private RedisAtomicLong u_id;
	
	

	/**
	 * to save data in String format
	 */
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * to save user data as object
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
	public UserRepositoryImpl(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.u_id = new RedisAtomicLong("u_id", stringRedisTemplate.getConnectionFactory());
	}

	
	@PostConstruct
	private void init() {
		srt_hashOps = stringRedisTemplate.opsForHash();
		srt_setOps = stringRedisTemplate.opsForSet();
		srt_zSetOps = stringRedisTemplate.opsForZSet();
		srt_simpleOps = stringRedisTemplate.opsForValue();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hska.iwi.vslab.repo.UserRepository#saveUser(hska.iwi.vslab.model.User)
	 */
	@Override
	public void saveUser(User user) {
		// generate a unique id
		String id = String.valueOf(u_id.incrementAndGet());

		user.setId(id);

		// to show how objects can be saved
		// be careful, if username already exists it's not added another time

        //Matze
        srt_simpleOps.increment("user_count", 1);
		String key = KEY_PREFIX_USER + user.getId();
		srt_hashOps.put(key, "u_id", id);
		//srt_hashOps.put(key, "firstName", user.getFirstname());
		//srt_hashOps.put(key, "lastName", user.getLastname());
		srt_hashOps.put(key, "username", user.getUsername());
		srt_hashOps.put(key, "password", user.getPassword());

		//Generiere einen Authenfikiations Key und verbinde ihn mit dem User
		srt_hashOps.put(key, "auth", generateAuth());

		// the key for a new user is added to the set for all usernames
		srt_setOps.add(KEY_SET_ALL_USERNAMES, user.getUsername());
		
		// the key for a new user is added to the sorted set for all usernames
		srt_zSetOps.add(KEY_ZSET_ALL_USERNAMES, user.getUsername(), 0);

		// to show how objects can be saved
		rt_hashOps.put(KEY_HASH_ALL_USERS, key, user);

        //srt_simpleOps.set("user_count", "1");

		//Verbindet Username und Id
		srt_simpleOps.set(user.getUsername(), user.getId());
	}

	@Override
	public Map<String, User> getAllUsers() {
		return rt_hashOps.entries(KEY_HASH_ALL_USERS);
	}

	
	@Override
	public User getUser(String username) {
		User user = new User();

		// if username is in set for all usernames, 
		if (srt_setOps.isMember(KEY_SET_ALL_USERNAMES, username)) {
			
			// get the user data out of the hash object with key "'user:' + username"
			String key = "user:" + username;
			user.setId(srt_hashOps.get(key, "id"));
			user.setUsername(srt_hashOps.get(key, "username"));
			user.setPassword(srt_hashOps.get(key, "password"));
		} else
			user = null;
		return user;
	}


	@Override
	public Map<String, User> findUsersWith(String pattern) {

		System.out.println("Searching for pattern  " + pattern);

		Set<byte[]> result = null;
		Map<String, User> mapResult = new HashMap<String, User>();

		if (pattern.equals("")){
			
			// get all user
			mapResult = rt_hashOps.entries(KEY_HASH_ALL_USERS);
	
		} else {
			// search for user with pattern
			
			char[] chars = pattern.toCharArray();
			chars[pattern.length() - 1] = (char) (chars[pattern.length() - 1] + 1);
			String searchto = new String(chars);

			Set <String> sresult = srt_zSetOps.rangeByLex(KEY_ZSET_ALL_USERNAMES, Range.range().gte(pattern).lt(searchto));
			for (Iterator iterator = sresult.iterator(); iterator.hasNext();) {
				String username = (String) iterator.next();
				System.out.println("key found: "+ username);
				User user = (User) rt_hashOps.get(KEY_HASH_ALL_USERS, KEY_PREFIX_USER + username);
	
				mapResult.put(user.getUsername(), user);
			}

		}
		
		return mapResult;

	}

	@Override
	public String getUserCount() {
		return srt_simpleOps.get("user_count");
	}

	@Override
	public String getIdByName(String name) {

		return srt_simpleOps.get(name);
	}

	@Override
	public String getPassword(String u_id) {
		return srt_hashOps.get(KEY_PREFIX_USER + u_id, "password");
	}

	@Override
	public boolean auth(String uname, String pass) {

		String uid = stringRedisTemplate.opsForValue().get(KEY_PREFIX_USER + getIdByName(uname));
		BoundHashOperations<String, String, String> userOps = stringRedisTemplate.boundHashOps(uid);
		return userOps.get("password").equals(pass);

	}

	@Override
	public String addAuth(String uname, long timeout, TimeUnit tUnit) {
		String uid = stringRedisTemplate.opsForValue().get(KEY_PREFIX_USER + getIdByName(uname));
		String auth = UUID.randomUUID().toString();
		stringRedisTemplate.boundHashOps("uid:" + uid + ":auth").put("auth", auth);
		stringRedisTemplate.expire("uid:" + uid + ":auth", timeout, tUnit);
		stringRedisTemplate.opsForValue().set("auth:" + auth + ":uid", uid, timeout, tUnit);
		return auth;

	}

	@Override
	public void deleteAuth(String uname) {
		String uid = stringRedisTemplate.opsForValue().get(KEY_PREFIX_USER + getIdByName(uname));
		String authKey = "uid:" + uid + ":auth";
		String auth = (String) stringRedisTemplate.boundHashOps(authKey).get("auth");
		List<String> keysToDelete = Arrays.asList(authKey, "auth:"+auth+":uid");
		stringRedisTemplate.delete(keysToDelete);
	}

	@Override
	public void setCookie() {



	}


	private String generateAuth() {

		String rnd = "K";

		for (int i = 0; i <= 15; i++) {

			double random = Math.random()*10;

			rnd += (int)random;
		}
		//int random = (int)Math.random()*10;
		//System.out.println(random);


		System.out.println(rnd);



		return rnd;
	}

}
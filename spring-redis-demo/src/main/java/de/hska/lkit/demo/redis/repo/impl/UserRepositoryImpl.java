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
import org.springframework.stereotype.Service;



@Repository
public class UserRepositoryImpl implements UserRepository {

	private static final String KEY_ZSET_ALL_USERNAMES 	= "user:all:usernames";

	private static final String KEY_SET_ALL_USERNAMES 	= "all:usernames";

	private static final String KEY_HASH_ALL_USERS 		= "all:user";

	private static final String KEY_PREFIX_USER 	= "user:";

	private static final String KEY_FOLLOWING_USER = "following:";

	private static final String KEY_FOLLOWERS_USER = "followers:";

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

        srt_simpleOps.increment("user_count", 1);
		String key = KEY_PREFIX_USER + user.getId();
		srt_hashOps.put(key, "u_id", id);

        String username = user.getUsername();
        srt_hashOps.put(key, "username", username);
		srt_hashOps.put(key, "password", user.getPassword());

        // the key for a new user is added to the set for all usernames
        srt_setOps.add(KEY_SET_ALL_USERNAMES, username);

		// the key for a new user is added to the sorted set for all usernames
		srt_zSetOps.add(KEY_ZSET_ALL_USERNAMES, username, 0);

		// to show how objects can be saved
		rt_hashOps.put(KEY_HASH_ALL_USERS, KEY_PREFIX_USER + username, user);

		//Verbindet Username und Id
		srt_simpleOps.set(username, user.getId());
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

			System.out.println("isMember wird aufgerufen");
			// get the user data out of the hash object with key "'user:' + username"
			String key = username;

			System.out.println("isMember wird aufgerufen");

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
		if(srt_simpleOps.get(name) != null) {
		    return srt_simpleOps.get(name);

		} else
			return null;
	}

	@Override
	public String getPassword(String u_id) {
		return srt_hashOps.get(KEY_PREFIX_USER + u_id, "password");
	}

	@Override

	public Map<String, User> getFollowing(String id) {

		System.out.println("getFollowing wird auf gerufen mit " + id);
		Set<Object> user = redisTemplate.opsForSet().members(KEY_FOLLOWING_USER + id);

		Map<String, User> mapUser = new HashMap<>();

		for (Object s : user) {
			System.out.println("In der For-Schleife " + s.toString() + " getuser " + getUser(s.toString()));
			mapUser.put(s.toString(), getUser(s.toString()));
		}

		return mapUser;
	}

	@Override
	public Map<String, User> getFollowers(String id) {

		System.out.println("getFollowers wird aufgerufen mit " + id);
		System.out.println(redisTemplate.opsForSet().members(KEY_FOLLOWERS_USER + id));
		Set<Object> user = redisTemplate.opsForSet().members(KEY_FOLLOWERS_USER + id);

		Map<String, User> mapUser = new HashMap<>();

		for (Object s : user) {
			System.out.println("In der For-Schleife " + s.toString() + " getuser " + getUser(s.toString()));
			mapUser.put(s.toString(), getUser(s.toString()));
		}

		return mapUser;
	}

	@Override

	public boolean auth(String uname, String pass) {


		String uid = getIdByName(uname);

		if (uid == null) {
			return false;
		}
		System.out.println("uid: " + uid);

		BoundHashOperations<String, String, String> userOps = stringRedisTemplate.boundHashOps(KEY_PREFIX_USER + uid);

		return userOps.get("password").equals(pass);

	}

	@Override
	public String addAuth(String uname, long timeout, TimeUnit tUnit) {

		String uid = getIdByName(uname);

		String auth = UUID.randomUUID().toString();
		stringRedisTemplate.boundHashOps("uid:" + KEY_PREFIX_USER + uid + ":auth").put("auth", auth);
		stringRedisTemplate.expire("uid:" + KEY_PREFIX_USER + uid + ":auth", timeout, tUnit);
		System.out.println("uid:" + KEY_PREFIX_USER + uid + ":auth");
		System.out.println("auth:" + auth + ":uid");
		stringRedisTemplate.opsForValue().set("auth:" + auth + ":uid", KEY_PREFIX_USER + uid, timeout, tUnit);

		System.out.println("Pass addAuth");
		System.out.println("");
		return auth;

	}

	@Override
	public void deleteAuth(String uname) {
		System.out.println("Gelöscht wird id " + getIdByName(uname));
		String uid = (KEY_PREFIX_USER + getIdByName(uname));
		String authKey = "uid:" + uid + ":auth";
		String auth = (String) stringRedisTemplate.boundHashOps(authKey).get("auth");
		System.out.println(auth);
		List<String> keysToDelete = Arrays.asList(authKey, "auth:"+auth+":uid");
		stringRedisTemplate.delete(keysToDelete);
		System.out.println("Delete done");
	}

	@Override
	public void followUser(String u_id, String u_id2) {

		System.out.println(KEY_FOLLOWING_USER + u_id + " " +KEY_PREFIX_USER + u_id2);
		String key = KEY_FOLLOWING_USER + u_id;
		String value = KEY_PREFIX_USER + u_id2;

		redisTemplate.opsForSet().add(key, value);

		System.out.println("Add Followers " + KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2 + " " + u_id);
		redisTemplate.opsForSet().add(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2, u_id);

		System.out.println(redisTemplate.opsForSet().members(key));
		System.out.println(redisTemplate.opsForSet().members(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2));

	}

	@Override
	public void unfollowUser(String u_id, String u_id2) {
		System.out.println("unfollowUser() " + KEY_FOLLOWING_USER + u_id + " " +KEY_PREFIX_USER + u_id2);
		String key = KEY_FOLLOWING_USER + u_id;
		String value = KEY_PREFIX_USER + u_id2;

		redisTemplate.opsForSet().remove(key, value);

		System.out.println("unfollow Followers " + KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2 + " " + u_id);
		redisTemplate.opsForSet().remove(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2, u_id);


	}

}
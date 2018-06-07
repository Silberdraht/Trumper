package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;



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
		String username = user.getUsername();
        String key = KEY_PREFIX_USER + id;

        srt_simpleOps.increment("user_count", 1);

		srt_hashOps.put(key, "u_id", id);
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

		// if username is in set with all usernames
		if (srt_setOps.isMember(KEY_SET_ALL_USERNAMES, username)) {

			// get the user data out of the hash object with key "'user:' + username"
            String u_id = srt_simpleOps.get(username);
            String key = KEY_PREFIX_USER + u_id;

			user.setId(srt_hashOps.get(key, "u_id"));
			user.setUsername(srt_hashOps.get(key, "username"));
			user.setPassword(srt_hashOps.get(key, "password"));

		} else
			user = null;

		return user;
	}

	@Override
	public User getUserById(String u_id) {
        User user = new User();
        System.out.println("This is the UID given to getUserById: " + u_id);
        //u_id is 'key'
        user.setId(srt_hashOps.get(u_id, "u_id"));
        user.setUsername(srt_hashOps.get(u_id, "username"));
        user.setPassword(srt_hashOps.get(u_id, "password"));
        return user;
    }

	@Override
	public Map<String, User> findUsersWith(String pattern) {

		Set<byte[]> result = null;
		Map<String, User> mapResult = new HashMap<String, User>();

		if (pattern.equals("")){

			mapResult = rt_hashOps.entries(KEY_HASH_ALL_USERS);
	
		} else {

			char[] chars = pattern.toCharArray();
			chars[pattern.length() - 1] = (char) (chars[pattern.length() - 1] + 1);
			String searchto = new String(chars);

			Set <String> sresult = srt_zSetOps.rangeByLex(KEY_ZSET_ALL_USERNAMES, Range.range().gte(pattern).lt(searchto));
			for (Iterator iterator = sresult.iterator(); iterator.hasNext();) {
				String username = (String) iterator.next();
				User user = rt_hashOps.get(KEY_HASH_ALL_USERS, KEY_PREFIX_USER + username);
	
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

		Set<Object> user = redisTemplate.opsForSet().members(KEY_FOLLOWING_USER + id);
        System.out.println("Size of Following in UserRepoImpl  " + user.size());
        Map<String, User> mapUser = new HashMap<>();
		for (Object s : user) {
			mapUser.put(getUserById(s.toString()).getUsername(), getUserById(s.toString()));
		}

		return mapUser;
	}

	@Override
	public Map<String, User> getFollowers(String id) {

		Set<Object> user = redisTemplate.opsForSet().members(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + id);
        System.out.println("Size of Followers in UserRepoImpl  " + user.size());
		Map<String, User> mapUser = new HashMap<>();
		for (Object s : user) {
            mapUser.put(getUserById(KEY_PREFIX_USER + s.toString()).getUsername(), getUserById(KEY_PREFIX_USER + s.toString()));
		}

		return mapUser;
	}

	@Override

	public boolean auth(String uname, String pass) {
		String uid = getIdByName(uname);

		if (uid == null) {
			return false;
		}
		BoundHashOperations<String, String, String> userOps = stringRedisTemplate.boundHashOps(KEY_PREFIX_USER + uid);

		return userOps.get("password").equals(pass);
	}

	@Override
	public String addAuth(String uname, long timeout, TimeUnit tUnit) {

		String uid = getIdByName(uname);

		String auth = UUID.randomUUID().toString();
		String KEY_UID = "uid:";
		String KEY_AUTH_POST = ":auth";
		stringRedisTemplate.boundHashOps(KEY_UID + KEY_PREFIX_USER + uid + KEY_AUTH_POST).put("auth", auth);
		stringRedisTemplate.expire(KEY_UID + KEY_PREFIX_USER + uid + KEY_AUTH_POST, timeout, tUnit);
		System.out.println(KEY_UID + KEY_PREFIX_USER + uid + KEY_AUTH_POST);
		String KEY_AUTH_PRE = "auth:";
		System.out.println(KEY_AUTH_PRE + auth + ":uid");
		stringRedisTemplate.opsForValue().set(KEY_AUTH_PRE + auth + ":uid", uid, timeout, tUnit);

		System.out.println("Pass addAuth");
		return auth;

	}

	@Override
	public void deleteAuth(String uname) {
		String uid = (KEY_PREFIX_USER + getIdByName(uname));
		String authKey = "uid:" + uid + ":auth";
		String auth = (String) stringRedisTemplate.boundHashOps(authKey).get("auth");
		List<String> keysToDelete = Arrays.asList(authKey, "auth:"+auth+":uid");
		stringRedisTemplate.delete(keysToDelete);
	}

	@Override
	public void followUser(String u_id, String u_id2) {

		String key = KEY_FOLLOWING_USER + u_id;
		String value = KEY_PREFIX_USER + u_id2;

		redisTemplate.opsForSet().add(key, value);

		redisTemplate.opsForSet().add(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2, u_id);
	}

	@Override
	public void unfollowUser(String u_id, String u_id2) {

		String key = KEY_FOLLOWING_USER + u_id;
		String value = KEY_PREFIX_USER + u_id2;

		redisTemplate.opsForSet().remove(key, value);

		redisTemplate.opsForSet().remove(KEY_FOLLOWERS_USER + KEY_PREFIX_USER + u_id2, u_id);
	}

}
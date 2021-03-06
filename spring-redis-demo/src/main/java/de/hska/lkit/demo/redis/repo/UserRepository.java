package de.hska.lkit.demo.redis.repo;


import de.hska.lkit.demo.redis.model.Impl.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface UserRepository {
	
	/**
	 * save user to repository
	 * 
	 * @param user
	 */
	public void saveUser(User user);


    void setUserOnline(String userName, boolean isOnline);

	boolean isUserOnline(String u_id);

	/**
	 * returns a list of all users
	 * 
	 * @return
	 */
	public Map<String, User>  getAllUsers();
	
	
	/**
	 * find the user with username
	 * 
	 * @param username
	 * @return
	 */
	public User getUser(String username);


	/**
	 * 
	 * find all users with characters in username
	 * 
	 * @param characters
	 * @return
	 */
	public List<User> findUsersWith(String characters);

	/**
	 * return the number of all users
	 * @return
	 */


	public String getUserCount();

	public String getIdByName(String name);

    public User getUserById(String u_id);

	public String getPassword(String u_id);


	public Map<String, User> getFollowing(String id);
	public Map<String, User> getFollowers(String id);


	public void followUser(String u_id, String u_id2);
	public void unfollowUser(String u_id, String u_id2);


	public boolean auth(String uname, String pass);
	public String addAuth(String uname, long timeout, TimeUnit tUnit);
	public void deleteAuth(String uname);


}

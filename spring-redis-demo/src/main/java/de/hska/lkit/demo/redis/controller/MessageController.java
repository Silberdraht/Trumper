package de.hska.lkit.demo.redis.controller;

import de.hska.lkit.demo.redis.model.Message;

import de.hska.lkit.demo.redis.repo.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



import java.util.Map;

/**
 * @author Lord_Silberdraht
 *
 */
@Controller
public class MessageController {

	private final MessageRepository messageRepository;

	@Autowired
	public MessageController(MessageRepository messageRepository) {
		super();

		this.messageRepository = messageRepository;
	}

	/**
	 *
	 * 
	 * @param model
	 * 
	 * @return
	 */

	/**
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public String getAllUsers(Model model) {
		Map<String, User> retrievedUsers = userRepository.getAllUsers();
		model.addAttribute("users", retrievedUsers);

		return "users";
	}
*/
	/**
	 * get information for user with username
	 * 
	 * @param username
	 *            username to find
	 * @param model
	 * @return
	 */

	/**
	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	public String getOneUsers(@PathVariable("username") String username, Model model) {
		User found = userRepository.getUser(username);

		model.addAttribute("userFound", found);
		return "oneUser";
	}
*/
	/**
	 * redirect to page to add a new user
	 * 
	 * @return
	 */
	/**
	@RequestMapping(value = "/addmessage", method = RequestMethod.GET)
	public String addUser(@ModelAttribute User user) {
		return "newUser";
	}

	/**
	 * add a new user, adds a list of all users to model
	 * 
	 * @param user
	 *            User object filled in form
	 * @param model
	 * @return
	 */

	@RequestMapping(value = "/addmessage", method = RequestMethod.GET)

	public void postMessage(@ModelAttribute String message) {
	}



	@RequestMapping(value = "/addmessage", method = RequestMethod.POST)
	public void postMessage(@ModelAttribute String message, Model model) {

		messageRepository.postMessage(message);
		model.addAttribute("message", "User successfully added");

		//Map<String, User> retrievedUsers = userRepository.getAllUsers();

		//model.addAttribute("users", retrievedUsers);
	}
	
	
	/**
	 * search usernames containing the sequence of characters
	 * 
	 * @param user
	 *            User object filled in form
	 * @param model
	 * @return
	 */
	/**
	@RequestMapping(value = "/searchuser/{pattern}", method = RequestMethod.GET)
	public String searchUser(@PathVariable("pattern") String pattern, @ModelAttribute User user, Model model) {

		Map<String, User> retrievedUsers = userRepository.findUsersWith(pattern);

		model.addAttribute("users", retrievedUsers);
		return "users";
	}
	
	*/
	

}

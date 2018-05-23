/**
package de.hska.lkit.demo.redis.controller;

import java.util.Map;

import de.hska.lkit.demo.redis.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.UserRepository;

/**
 * @author knad0001
 *
 */

/**
@Controller
public class UserController {

	private final UserRepository userRepository;

	@Autowired
	public UserController(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}


	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public String getAllUsers(Model model) {
		Map<String, User> retrievedUsers = userRepository.getAllUsers();
		model.addAttribute("users", retrievedUsers);

		return "users";
	}
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getAllUsersLogin(@ModelAttribute User user) {
		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String getAllUsersLogin(User user, Model model) {

		Map<String, User> retrievedUsers = userRepository.getAllUsers();

		if(user.getPassword().equalsIgnoreCase(userRepository.getPassword(userRepository.getIdByName(user.getUsername()) ))) {
			System.out.println("Passwort if == true");
			model.addAttribute("users", retrievedUsers);

			return "messages";
		}

		model.addAttribute("users", retrievedUsers);
		return "login";
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	public String getOneUsers(@PathVariable("username") String username, Model model) {
		User found = userRepository.getUser(username);

		model.addAttribute("userFound", found);
		return "oneUser";
	}


	@RequestMapping(value = "/adduser", method = RequestMethod.GET)
	public String addUser(@ModelAttribute User user) {
		return "newUser";
	}

	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	public String saveUser(@ModelAttribute User user, Model model) {

		userRepository.saveUser(user);
		model.addAttribute("message", "User successfully added");

		Map<String, User> retrievedUsers = userRepository.getAllUsers();

		model.addAttribute("users", retrievedUsers);
		return "users";
	}
	
	

	@RequestMapping(value = "/searchuser/{pattern}", method = RequestMethod.GET)
	public String searchUser(@PathVariable("pattern") String pattern, @ModelAttribute User user, Model model) {

		Map<String, User> retrievedUsers = userRepository.findUsersWith(pattern);

		model.addAttribute("users", retrievedUsers);
		return "users";
	}
	
	
	

}
*/
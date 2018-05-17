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


import java.util.List;
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


	@RequestMapping(value = "/messages", method = RequestMethod.GET)
	public String getAllMessages(Model model) {

		Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
		model.addAttribute("messages", retrievedMessages);
		return "messages";
	}

	@RequestMapping(value = "/addmessage", method = RequestMethod.GET)

	public String postMessage(@ModelAttribute Message message) {

		return "newMessage";
	}



	@RequestMapping(value = "/addmessage", method = RequestMethod.POST)
	public String postMessage(@ModelAttribute Message message, Model model) {

		messageRepository.postMessage(message.getText());
		model.addAttribute("messages");

		Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
		model.addAttribute("messages", retrievedMessages);


		return "messages";
		//model.addAttribute("message", "Message successfully added");

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

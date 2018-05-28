package de.hska.lkit.demo.redis.controller;


import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;
import de.hska.lkit.demo.redis.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Controller
public class ControllerImpl {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    @Autowired

    public ControllerImpl(MessageRepository messageRepository,UserRepository userRepository) {
        super();

        this.messageRepository = messageRepository;
        this.userRepository = userRepository;

    }

    //", @RequestParam(defaultValue = "0") int page" was added in order to implement pageination -noah
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getAllMessages(Model model, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int pagelength) {
        System.out.println("Msg Rep wird aufgerufen");
        Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
        int i = 0;
        int offset = (page-1)*pagelength;
        Map<String,Message> pagedMessages = new HashMap<>();
        for (Map.Entry<String, Message> entry : retrievedMessages.entrySet()) {
            if (i >= offset && i < offset + pagelength) {
                pagedMessages.put(entry.getKey(),entry.getValue());
            }
            i+=1;
        }
        model.addAttribute("messages", pagedMessages);
        int pagesRequired = (int) Math.ceil((float) retrievedMessages.size() / pagelength);
        model.addAttribute("size", pagesRequired);
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
        Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();


        if(user.getPassword().equalsIgnoreCase(userRepository.getPassword(userRepository.getIdByName(user.getUsername()) ))) {
            System.out.println("Passwort if == true");
            model.addAttribute("users", retrievedUsers);
            model.addAttribute("messages", retrievedMessages);

            return "messages";
        }

        model.addAttribute("users", retrievedUsers);
        return "login";
    }

    /**
     * get information for user with username
     *
     * @param username
     *            username to find
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
    public String getOneUsers(@PathVariable("username") String username, Model model) {
        User found = userRepository.getUser(username);

        model.addAttribute("userFound", found);
        return "oneUser";
    }

    /**
     * redirect to page to add a new user
     *
     * @return
     */
    @RequestMapping(value = "/adduser", method = RequestMethod.GET)
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
    @RequestMapping(value = "/adduser", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute User user, Model model) {

        userRepository.saveUser(user);
        model.addAttribute("message", "User successfully added");

        Map<String, User> retrievedUsers = userRepository.getAllUsers();

        model.addAttribute("users", retrievedUsers);
        return "users";
    }


    /**
     * search usernames containing the sequence of characters
     *
     * @param user
     *            User object filled in form
     * @param model
     * @return
     */
    @RequestMapping(value = "/searchuser/{pattern}", method = RequestMethod.GET)
    public String searchUser(@PathVariable("pattern") String pattern, @ModelAttribute User user, Model model) {

        Map<String, User> retrievedUsers = userRepository.findUsersWith(pattern);

        model.addAttribute("users", retrievedUsers);
        return "users";
    }

}
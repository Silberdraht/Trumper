package de.hska.lkit.demo.redis.controller;


import java.time.Duration;
import java.util.Map;

import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.repo.impl.SimpleCookieInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.UserRepository;

import de.hska.lkit.demo.redis.repo.MessageRepository;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@org.springframework.stereotype.Controller
public class ControllerImpl {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    @Autowired

    private static final Duration TIMEOUT = Duration.ofMinutes(15);
    public ControllerImpl(MessageRepository messageRepository,UserRepository userRepository) {
        super();

        this.messageRepository = messageRepository;
        this.userRepository = userRepository;

    }


    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getAllMessages(Model model) {
        System.out.println("Msg Rep wird aufgerufen");
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

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAllUsers(Model model) {
        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        model.addAttribute("users", retrievedUsers);

        return "users";
    }
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getAllUsersLogin(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        SimpleCookieInterceptor simpleCookieInterceptor = new SimpleCookieInterceptor();

        boolean test = simpleCookieInterceptor.preHandle(request, response, model);

        System.out.println(test);




        return "login";
    }



    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String getAllUsersLogin(@ModelAttribute("user") @Valid User user, HttpServletResponse response, Model model) {



        System.out.println("login Post wird aufgerufen");
        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();

        System.out.println(user.getUsername());
        System.out.println(user.getPassword());

        if(userRepository.auth(user.getUsername(), user.getPassword())) {
            System.out.println("Passwort if == true");
            String auth = userRepository.addAuth(user.getUsername(), TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Cookie cookie = new Cookie("auth", auth);
            response.addCookie(cookie);
            model.addAttribute("user", user.getUsername());
            //return "users/" + user.getName(); }


             //model.addAttribute("user", new User());
            //model.addAttribute("users", retrievedUsers);
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
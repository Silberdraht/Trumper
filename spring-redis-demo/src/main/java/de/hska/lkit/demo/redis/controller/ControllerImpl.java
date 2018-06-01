package de.hska.lkit.demo.redis.controller;


import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.SimpleSecurity;
import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;
import de.hska.lkit.demo.redis.repo.UserRepository;
import de.hska.lkit.demo.redis.repo.impl.SimpleCookieInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@org.springframework.stereotype.Controller
public class ControllerImpl {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpleCookieInterceptor simpleCookieInterceptor;

    @Autowired
    private static final Duration TIMEOUT = Duration.ofMinutes(15);
    public ControllerImpl(MessageRepository messageRepository,UserRepository userRepository, SimpleCookieInterceptor simpleCookieInterceptor) {
        super();

        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.simpleCookieInterceptor = simpleCookieInterceptor;

    }

    //", @RequestParam(defaultValue = "0") int page" was added in order to implement pageination -noah
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getAllMessages(Model model, HttpServletResponse response, HttpServletRequest request,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int pagelength) throws Exception {
        //System.out.println("Msg Rep wird aufgerufen");
        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
            int i = 0;
            int offset = (page - 1) * pagelength;
            Map<String, Message> pagedMessages = new HashMap<>();
            for (Map.Entry<String, Message> entry : retrievedMessages.entrySet()) {
                if (i >= offset && i < offset + pagelength) {
                    pagedMessages.put(entry.getKey(), entry.getValue());
                }
                i += 1;
            }
            model.addAttribute("current", page);
            model.addAttribute("messages", pagedMessages);
            int pagesRequired = (int) Math.ceil((float) retrievedMessages.size() / pagelength);
            model.addAttribute("size", pagesRequired);
            return "messages";
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/messagesFollow", method = RequestMethod.GET)
    public String getAllMessagesFollowed(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)){
            Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
            model.addAttribute("messages", retrievedMessages);
            return "messages";
        }
        return "login";
    }

    @RequestMapping(value = "/addmessage", method = RequestMethod.GET)

    public String postMessage(@ModelAttribute Message message, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            return "newMessage";
        }
        return "redirect:/login";
    }



    @RequestMapping(value = "/addmessage", method = RequestMethod.POST)
    public String postMessage(@ModelAttribute Message message, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            messageRepository.postMessage(message.getText());
            model.addAttribute("messages");

            Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
            model.addAttribute("messages", retrievedMessages);


            return "redirect:/messages?page=1";
        }

        return "login";
    }

    @RequestMapping(value = "/addfollow", method = RequestMethod.GET)

    public String addFollow(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        System.out.println("GET addFollow");

        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            return "addFollow";
        }

        return "addFollow";
    }

    @RequestMapping(value = "/addfollow", method = RequestMethod.POST)
    public String addFollow(@ModelAttribute User user, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
        System.out.println("Post addFollow");
        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            userRepository.followUser(SimpleSecurity.getUid(), userRepository.getIdByName(user.getUsername()));

            /**
            messageRepository.postMessage(message.getText());
            model.addAttribute("messages");

            Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
            model.addAttribute("messages", retrievedMessages);

             */

            return "addFollow";
        }

        return "redirect:/login";
    }


    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAllUsers(Model model) {
        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        model.addAttribute("users", retrievedUsers);

        return "users";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getAllUsersLogin(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

//@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model
        boolean test = simpleCookieInterceptor.preHandle(request, response, model);
        System.out.println("login " + test);

//        if (simpleCookieInterceptor.preHandle(request, response, model)) {
  //          return "logout";
    //    }
        model.addAttribute("user", new User());
        return "login";
    }



    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String getAllUsersLogin(@ModelAttribute("user") @Valid User user, HttpServletResponse response, Model model) {



        System.out.println("login Post wird aufgerufen");
        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();


        if(userRepository.auth(user.getUsername(), user.getPassword())) {
            String auth = userRepository.addAuth(user.getUsername(), TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Cookie cookie = new Cookie("auth", auth);
            response.addCookie(cookie);
            model.addAttribute("user", user.getUsername());
            //return "users/" + user.getName(); }


            //model.addAttribute("user", new User());
            //model.addAttribute("users", retrievedUsers);
            //model.addAttribute("messages", retrievedMessages);


            return "logout";
        }

        //model.addAttribute("user", new User());
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
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutGet(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        boolean test = simpleCookieInterceptor.preHandle(request, response, model);
        System.out.println("logout " + test);


        return "logout"; }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout() {
        //System.out.println("logout wird aufgerufen");
        //System.out.println(SimpleSecurity.isSignedIn());
        //System.out.println(user.getUsername());

        if (SimpleSecurity.isSignedIn()) {
            String name = SimpleSecurity.getName();
            System.out.println("Logout prep deleteAuth für " + name);
            userRepository.deleteAuth(name);
        }
        return "logout"; }


}
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model){
        return "redirect:/login";
    }

 /*   @RequestMapping(value = "/messages", method = RequestMethod.POST)
    public String reqALLMessagesPage(Model model) {
        return "redirect:/messages?";
    }*/

    //", @RequestParam(defaultValue = "0") int page" was added in order to implement pageination -noah
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getAllMessages(Model model, HttpServletResponse response, HttpServletRequest request,
                                 @ModelAttribute Message message,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int pagelength) throws Exception {
        if (simpleCookieInterceptor.preHandle(request, response, model)) {

            List<Message> retrievedMessages = messageRepository.getMessagesGlobal();
            int offset = (page - 1) * pagelength;
            List<Message> pagedMessages = new ArrayList<>();

            int i = 0;
            for (Message currentMessage : retrievedMessages) {
                if (i >= offset) {
                    if (i < offset + pagelength) {
                        pagedMessages.add(currentMessage);
                    } else {
                        break; //for performance
                    }
                }
                i += 1;
            }
            model.addAttribute("current", page);
            model.addAttribute("messages", pagedMessages);
            int pagesRequired = (int) Math.ceil((float) retrievedMessages.size() / pagelength);
            if (pagesRequired == 0) {
                pagesRequired = 1;
            }
            model.addAttribute("size", pagesRequired);
            return "messages";
        }
        return "redirect:/login";
    }


    @RequestMapping(value = "/following", method = RequestMethod.GET)
    public String getAllFollowers(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)){
            Map<String, User> retrievedUsers = userRepository.getFollowing(SimpleSecurity.getUid());
            model.addAttribute("users", retrievedUsers);
            return "following";
        }
        return "login";
    }

    @RequestMapping(value = "/followers", method = RequestMethod.GET)
    public String getFollowedBy(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)){
            Map<String, User> retrievedUsers = userRepository.getFollowers(SimpleSecurity.getUid());
            model.addAttribute("users", retrievedUsers);
            return "followers";
        }
        return "login";
    }

    @RequestMapping(value = "/messagesfollow", method = RequestMethod.GET)
    public String getAllMessagesFollowed(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)){
            List<Message> retrievedMessages = messageRepository.getMessageFollow(SimpleSecurity.getUid());
            model.addAttribute("messages", retrievedMessages);
            return "messagesFollow";

        }
        return "login";
    }


    @RequestMapping(value = "messages/addmessage", method = RequestMethod.POST)
    public String postMessage(@ModelAttribute Message message, @RequestParam(defaultValue = "1") int page, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            messageRepository.postMessage(message.getText());
            model.addAttribute("messages");

            List<Message> retrievedMessages = messageRepository.getMessagesGlobal();
            model.addAttribute("messages", retrievedMessages);


            return "redirect:/messages?page="+page;

        }

        return "login";
    }


    @RequestMapping(value = "/addfollow", method = RequestMethod.GET)

    public String addFollow(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            return "addFollow";
        }

        return "login";
    }

    @RequestMapping(value = "/addfollow", method = RequestMethod.POST)
    public String addFollow(@ModelAttribute User user, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            userRepository.followUser(SimpleSecurity.getUid(), userRepository.getIdByName(user.getUsername()));


            return "addFollow";
        }


        return "login";

    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.GET)
    public String unfollow(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            return "unfollow";
        }

        return "login";
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public String unfollow(@ModelAttribute User user, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            userRepository.unfollowUser(SimpleSecurity.getUid(), userRepository.getIdByName(user.getUsername()));


            return "unfollow";
        }

        return "login";
    }


    @RequestMapping(value = "/userlist", method = RequestMethod.GET)
    public String getAllUsers(Model model) {
        Collection<User> retrievedUsers = userRepository.getAllUsers().values();
        model.addAttribute("users", retrievedUsers);

        return "users";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getAllUsersLogin(Model model, @ModelAttribute("user") @Valid User user, HttpServletResponse response, HttpServletRequest request) throws Exception {
        boolean test = simpleCookieInterceptor.preHandle(request, response, model);

        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            return "redirect:/messages";
        }

        return "login";
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String getAllUsersLogin(@ModelAttribute("user") @Valid User user, @RequestParam String send, HttpServletResponse response, Model model) {
        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        if (send.equals("register")) {

            for (User u : retrievedUsers.values())
                if (u.getUsername().equals(user.getUsername())) {
                    //TODO Give some form of feedback, ie popup, that registering was not successful due to already existing user
                    return "redirect:/login";
                }

            userRepository.saveUser(user);
            return "redirect:/messages?";

        } else {
            if(userRepository.auth(user.getUsername(), user.getPassword())) {
                String auth = userRepository.addAuth(user.getUsername(), TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                Cookie cookie = new Cookie("auth", auth);
                response.addCookie(cookie);
                model.addAttribute("user", user.getUsername());


                return "redirect:/messages?page=1";
            }
        }

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
        System.out.println(username);

        User found = userRepository.getUser(SimpleSecurity.getUid());
        System.out.println(found.getUsername());
        model.addAttribute("userFound", found.getUsername());
        return "oneUser";
    }


    /**
     * search usernames containing the sequence of characters
     *
     * @param username
     *            User object filled in form
     * @param model
     * @return
     */
    @RequestMapping(value = "/searchuser", method = RequestMethod.POST)
    public String searchUser(@ModelAttribute String username, HttpServletRequest req, Model model) {

        Collection<User> retrievedUsers = userRepository.findUsersWith(username).values();
        String uid = simpleCookieInterceptor.getCookieUID(req);
        Map<String, User> following = userRepository.getFollowing(uid);
        List<Boolean> isFollowing = new LinkedList<>();
        for (User user : retrievedUsers){
            if (following.containsValue(user)) {
                isFollowing.add(true);
            }
            else {
                isFollowing.add(false);
            }
        }
        System.out.println(isFollowing.size() == retrievedUsers.size());
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("users", retrievedUsers);
        return "users";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if (simpleCookieInterceptor.preHandle(request, response, model) && SimpleSecurity.isSignedIn()) {
            String name = SimpleSecurity.getName();
            userRepository.deleteAuth(name);
        }
        return "redirect:/login";
    }
}
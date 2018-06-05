package de.hska.lkit.demo.redis.controller;


import de.hska.lkit.demo.redis.model.Message;
import de.hska.lkit.demo.redis.model.SimpleSecurity;
import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;
import de.hska.lkit.demo.redis.repo.UserRepository;
import de.hska.lkit.demo.redis.repo.impl.SimpleCookieInterceptor;
import javafx.collections.transformation.SortedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.util.*;
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
        //System.out.println("Msg Rep wird aufgerufen");
        if (simpleCookieInterceptor.preHandle(request, response, model)) {
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
            Map<String, Message> retrievedMessages = messageRepository.getMessageFollow(SimpleSecurity.getUid());
            model.addAttribute("messages", retrievedMessages);
            return "messagesFollow";

        }
        return "login";
    }

//    @RequestMapping(value = "/addmessage", method = RequestMethod.GET)

//    public String postMessage(@ModelAttribute Message message, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
//        if(simpleCookieInterceptor.preHandle(request, response, model)) {

//            return "newMessage";
//      }
//        return "redirect:/login";
//    }



    @RequestMapping(value = "messages/addmessage", method = RequestMethod.POST)
    public String postMessage(@ModelAttribute Message message, @RequestParam(defaultValue = "1") int page, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if(simpleCookieInterceptor.preHandle(request, response, model)) {
            messageRepository.postMessage(message.getText());
            model.addAttribute("messages");

            Map<String, Message> retrievedMessages = messageRepository.getMessageGlobal();
            model.addAttribute("messages", retrievedMessages);


            return "redirect:/messages?page="+page;

        }

        return "login";
    }


    @RequestMapping(value = "/addfollow", method = RequestMethod.GET)
    public String addFollow(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        System.out.println("GET addFollow");

        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            return "addFollow";
        }


        return "login";

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


        return "login";

    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.GET)
    public String unfollow(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {

        System.out.println("GET unFollow");

        if(simpleCookieInterceptor.preHandle(request, response, model)) {

            return "unfollow";
        }

        return "login";
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public String unfollow(@ModelAttribute User user, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
        System.out.println("Post unfollow");
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
        System.out.println("login credential: " + test);

        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            return "redirect:/messages";
        }

        return "login";
    }



    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String getAllUsersLogin(@ModelAttribute("user") @Valid User user, @RequestParam String send, HttpServletResponse response, Model model) {

        Map<String, User> retrievedUsers = userRepository.getAllUsers();
        System.out.println(send);
        if (send.equals("register")) {

            for (User u : retrievedUsers.values())
                if (u.getUsername().equals(user.getUsername())) {
                    //TODO Give some form of feedback, ie popup, that registering was not successful due to already existing user
                    return "redirect:/login";
                }

            userRepository.saveUser(user);
            //model.addAttribute("msg", "User successfully added");
            System.out.println("New User added to DB");
            return "redirect:/messages?";
        }

        else {
            System.out.println("login Post wird aufgerufen");

            if(userRepository.auth(user.getUsername(), user.getPassword())) {
                String auth = userRepository.addAuth(user.getUsername(), TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                Cookie cookie = new Cookie("auth", auth);
                response.addCookie(cookie);
                model.addAttribute("user", user.getUsername());

                //return "users/" + user.getName(); }
                //model.addAttribute("user", new User());
                //model.addAttribute("users", retrievedUsers);
                //model.addAttribute("messages", retrievedMessages);

                return "redirect:/messages?page=1";
            }
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
/*
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String foundUsers(Model model) {

        return "users";
    }
*/

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout() {

        if (SimpleSecurity.isSignedIn()) {
            String name = SimpleSecurity.getName();
            System.out.println("Logout prep deleteAuth für " + name);
            userRepository.deleteAuth(name);
            return "redirect:/login";
        }
        return "redirect:/messages"; }


}
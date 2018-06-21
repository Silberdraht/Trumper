package de.hska.lkit.demo.redis.controller;

import de.hska.lkit.demo.redis.model.Impl.Message;
import de.hska.lkit.demo.redis.model.Impl.RedisMessagePublisher;
import de.hska.lkit.demo.redis.model.SimpleSecurity;
import de.hska.lkit.demo.redis.model.Impl.User;
import de.hska.lkit.demo.redis.repo.MessageRepository;
import de.hska.lkit.demo.redis.repo.UserRepository;
import de.hska.lkit.demo.redis.repo.impl.SimpleCookieInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@org.springframework.stereotype.Controller
public class ControllerImpl {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpleCookieInterceptor simpleCookieInterceptor;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private static final Duration TIMEOUT = Duration.ofMinutes(15);

    public ControllerImpl(MessageRepository messageRepository, UserRepository userRepository, SimpleCookieInterceptor simpleCookieInterceptor) {
        super();
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.simpleCookieInterceptor = simpleCookieInterceptor;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:/login";
    }

    //", @RequestParam(defaultValue = "0") int page" was added in order to implement pageination -noah
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getAllMessages(Model model, HttpServletResponse response, HttpServletRequest request,
                                 @ModelAttribute Message message,
                                 @ModelAttribute("querry") Message querry,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int pagelength) throws Exception {

        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            String userName = SimpleSecurity.getName();
            model.addAttribute("loggedOn", userName);
            model.addAttribute("user_id", SimpleSecurity.getUid());

            int offset = (page - 1) * pagelength;
            List<Message> pagedMessages;
            pagedMessages = messageRepository.getMessagesInRange(offset,offset+pagelength-1, messageRepository);
            model.addAttribute("current", page);
            model.addAttribute("messages", pagedMessages);
            long pagesRequired = (int) Math.ceil((float) messageRepository.countGlobalMessages() / pagelength);
            if (pagesRequired <= 0) {
                pagesRequired = 1;
            }
            model.addAttribute("size", pagesRequired);
            return "messages";
        }
        return "redirect:/login";
    }


    /*@MessageMapping("/messages/addmessage")
    public void post(Message postedMessage) {
        RedisMessagePublisher redisMessagePublisher = new RedisMessagePublisher(messagingTemplate);
        redisMessagePublisher.publish(postedMessage);
    }*/

    @RequestMapping(value = "messages/addmessage", method = RequestMethod.POST)
    public String postMessage(@ModelAttribute Message message,
                              Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (simpleCookieInterceptor.preHandle(request, response, model) && !message.getText().trim().equals("")) {
            messageRepository.postMessage(message.getText());

            return "redirect:/messages";
        }
        return "redirect:/login";
    }


    @RequestMapping(value = "/searchusers/follow", method = RequestMethod.POST)
    public String addFollow(Model model, HttpServletResponse response, HttpServletRequest request,
                            @ModelAttribute User user,
                            @RequestParam String element) throws Exception {
        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            String followedUserID = userRepository.getIdByName(element);
            userRepository.followUser(SimpleSecurity.getUid(), followedUserID);
            return "redirect:/searchusers";
        }

        return "redirect:/login";

    }

    @RequestMapping(value = "/searchusers/unfollow", method = RequestMethod.POST)
    public String unfollow(Model model, HttpServletResponse response, HttpServletRequest request,
                           @ModelAttribute User user,
                           //@ModelAttribute ArrayList<User> users,
                           //@ModelAttribute ArrayList<Boolean> isFollowing,
                           @RequestParam String element) throws Exception {

        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            userRepository.unfollowUser(SimpleSecurity.getUid(), userRepository.getIdByName(element));
            return "redirect:/searchusers";
        }

        return "redirect:/login";
    }


    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getAllUsersLogin(Model model, @ModelAttribute("user") @Valid User user, HttpServletResponse response, HttpServletRequest request) throws Exception {

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
                if (u.getUsername().toLowerCase().equals(user.getUsername().toLowerCase())) {
                    //TO-DO Give some form of feedback, ie popup, that registering was not successful due to already existing user
                    return "redirect:/login";
                }

            userRepository.saveUser(user);
            return "redirect:/messages?page=1";

        } else {
            if (userRepository.auth(user.getUsername(), user.getPassword())) {
                String auth = userRepository.addAuth(user.getUsername(), TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                Cookie cookie = new Cookie("auth", auth);
                response.addCookie(cookie);
                model.addAttribute("user", user.getUsername());
                userRepository.setUserOnline(userRepository.getIdByName(user.getUsername()), true);
                System.out.println("getAllUsersLogin: " + user.getUsername());
                return "redirect:/messages?page=1";
            }
        }

        return "login";
    }

    /**
     * get information for user with username
     *
     * @param username username to find
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
    public String getOneUser(@PathVariable("username") String username, Model model, HttpServletResponse res, HttpServletRequest req) throws Exception {
        if (simpleCookieInterceptor.preHandle(req, res, model)) {
            model.addAttribute("loggedOn", SimpleSecurity.getName());
            model.addAttribute("user_id", SimpleSecurity.getUid());
            User found = userRepository.getUser(username);
            String name = found.getUsername();
            String id = userRepository.getIdByName(name);

            model.addAttribute("userFound", name);
            model.addAttribute("followers", userRepository.getFollowers(id));
            model.addAttribute("following", userRepository.getFollowing(id));
            return "foundUser";
        }
        return "redirect:/login";

    }

    //Preventing Error-Page (Nice to have)
    @RequestMapping(value = {"/user/","/users","/user"}, method = RequestMethod.GET)
    public String getUserWithoutName(Model model, HttpServletResponse res, HttpServletRequest req) throws Exception {
        if (simpleCookieInterceptor.preHandle(req, res, model)) {
            return "redirect:/searchusers";
        }
        return "redirect:/login";
    }

    //needed for getUserWithoutName, preventing Error-Pages - also for /searchuser call
    @RequestMapping(value = "/searchusers", method = RequestMethod.GET)
    public String foundUser(HttpServletRequest req, HttpServletResponse res, Model model,
                                 @ModelAttribute("querry") Message querry) throws Exception {
        if (simpleCookieInterceptor.preHandle(req, res, model)) {
            return searchUser(new Message("","","",""),req,res,model);
        }
        return "redirect:/login";
    }

    /**
     * search usernames containing the sequence of characters
     *
     * @param querry User object filled in form
     * @param model
     * @return
     */
    @RequestMapping(value = "/searchusers", method = RequestMethod.POST)
    public String searchUser(@ModelAttribute("querry") Message querry, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception {
        if (simpleCookieInterceptor.preHandle(req, res, model)) {
            model.addAttribute("loggedOn", SimpleSecurity.getName());
            model.addAttribute("user_id", SimpleSecurity.getUid());

            String uid = SimpleSecurity.getUid();
            List<User> retrievedUsers;
            List<Boolean> isFollowing = new ArrayList<>();
            //retrievedUsers.addAll(userRepository.findUsersWith(querry.getText()).values());
            retrievedUsers = userRepository.findUsersWith(querry.getText());
            Map<String, User> following = userRepository.getFollowing(uid);
            for (User user : retrievedUsers) {
                String name = user.getUsername();
                boolean follows = false;
                for (User comp : following.values()) {
                    if (comp != null && comp.getUsername().equals(name)) {
                        follows = true;
                        break;
                    }
                }
                isFollowing.add(follows);
            }
            model.addAttribute("isFollowing", isFollowing);
            model.addAttribute("users", retrievedUsers);
            return "searchusers";
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {

        if (simpleCookieInterceptor.preHandle(request, response, model) && SimpleSecurity.isSignedIn()) {
            String name = SimpleSecurity.getName();
            userRepository.setUserOnline(SimpleSecurity.getUid(), false);
            userRepository.deleteAuth(name);
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/own_messages", method = RequestMethod.GET)
    public String getOwnTimeline(Model model, HttpServletResponse response, HttpServletRequest request,
                                 @ModelAttribute("querry") Message querry,
                                 @ModelAttribute String username,
                                 @ModelAttribute Message message,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int pagelength) throws Exception {
        if (simpleCookieInterceptor.preHandle(request, response, model)) {
            model.addAttribute("loggedOn", SimpleSecurity.getName());
            model.addAttribute("user_id", SimpleSecurity.getUid());

            int offset = (page - 1) * pagelength;
            List<Message> pagedMessages;
            pagedMessages = messageRepository.getMessagesInRange(SimpleSecurity.getUid(), offset,offset+pagelength-1, messageRepository);
            model.addAttribute("current", page);
            model.addAttribute("messages", pagedMessages);
            long pagesRequired = (long) Math.ceil((float) messageRepository.countTimelineMessages(SimpleSecurity.getUid()) / pagelength);
            if (pagesRequired == 0) {
                pagesRequired = 1;
            }
            model.addAttribute("size", pagesRequired);
            return "own_messages";
        }
        return "redirect:/login";
    }




}

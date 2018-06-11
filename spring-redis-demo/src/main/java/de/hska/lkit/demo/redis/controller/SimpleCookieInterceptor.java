/**
package de.hska.lkit.demo.redis.controller;

import de.hska.lkit.demo.redis.model.SimpleSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import de.hska.lkit.demo.redis.model.Impl.User;
import de.hska.lkit.demo.redis.repo.UserRepository;


public class SimpleCookieInterceptor extends HandlerInterceptorAdapter {



    @Autowired
    private StringRedisTemplate template;




    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {

        System.out.println("Cookie preHandle wird aufgerufen");

        Cookie[] cookies = req.getCookies();
        if (!ObjectUtils.isEmpty(cookies))
            System.out.println("first if");
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("auth")) {
                    System.out.println("if 1 CL");
                    String auth = cookie.getValue();
                    System.out.println("Das Steht in Auth Cookie: " + auth);
                    if (auth != null) {
                        //System.out.println("CL auth != null");

                        template.opsForHash().put("test", "hallo", "world");
                        //String test = (String) template.opsForHash().get("user:1", "name");

                        //System.out.println(test);
                        String uid = template.opsForValue().get("auth:" + auth + ":uid");

                        System.out.println(uid);
                        if (uid != null) {
                            System.out.println("uid != null");
                            String name = (String) template.boundHashOps("uid:" + uid + ":user").get("name");
                            SimpleSecurity.setUser(name, uid);
                        }
                    }
                }
        return true;
    }
}

 */


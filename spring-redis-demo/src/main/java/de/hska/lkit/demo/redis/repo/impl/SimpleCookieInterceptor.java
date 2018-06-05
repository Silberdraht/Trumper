
package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.SimpleSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Repository
public class SimpleCookieInterceptor extends HandlerInterceptorAdapter {



    @Autowired
    private StringRedisTemplate template;


    @Resource(name = "redisTemplate")
    private ValueOperations<String,String> srt_simpleOps;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {

        Cookie[] cookies = req.getCookies();
        if (!ObjectUtils.isEmpty(cookies))
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("auth")) {
                    String auth = cookie.getValue();
                    if (auth != null) {

                        String uid = template.opsForValue().get("auth:" + auth + ":uid");

                        if (uid != null) {
                            String name = (String) template.opsForHash().get(uid, "username");
                            System.out.println("Thread Login:" + Thread.currentThread().getId());

                            SimpleSecurity.setUser(name, uid);

                            return true;
                        }
                    }
                }

        return false; //return true;
    }
}


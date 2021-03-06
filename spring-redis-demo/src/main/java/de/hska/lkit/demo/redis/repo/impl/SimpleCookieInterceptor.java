
package de.hska.lkit.demo.redis.repo.impl;

import de.hska.lkit.demo.redis.model.Impl.User;
import de.hska.lkit.demo.redis.model.SimpleSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
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
    private ValueOperations<String, String> srt_simpleOps;

    public String getCookieUID(HttpServletRequest req) {
        for (Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals("auth")) {
                return template.opsForValue().get("auth:" + cookie.getValue() + ":uid");
            }
        }
        return "";
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {

        Cookie[] cookies = req.getCookies();
        if (cookies != null) { //if (!ObjectUtils.isEmpty(cookies))
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("auth")) {
                    String auth = cookie.getValue();
                    if (auth != null) {

                        String uid = template.opsForValue().get("auth:" + auth + ":uid");

                        if (uid != null) {
                            String name = (String) template.opsForHash().get("user:" + uid, "username");

                            SimpleSecurity.setUser(name, uid);

                            return true;
                        }
                    }
                }
        }
        return false;
    }
}
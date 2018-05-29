
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

        System.out.println("Cookie preHandle wird aufgerufen");

        Cookie[] cookies = req.getCookies();
        if (!ObjectUtils.isEmpty(cookies))
            System.out.println("first if");
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("auth")) {
                    System.out.println(cookie.getName());
                    String auth = cookie.getValue();
                    System.out.println("Dies steht in Auth Cookie: " + auth);
                    if (auth != null) {
                        System.out.println("CL auth != null");



                        //System.out.println(test);
                        String uid = template.opsForValue().get("auth:" + auth + ":uid");

                        System.out.println("uid durch cookie " + uid);
                        if (uid != null) {
                            System.out.println("uid != null");
                            //String name2 = (String) template.boundHashOps(uid).get("name");
                            String name = (String) template.opsForHash().get(uid, "username");
                            System.out.println("Füttere SimpleSec");
                            System.out.println(name);
                            System.out.println(uid);
                            SimpleSecurity.setUser(name, uid);

                            System.out.println("prehandle done");
                        }
                    }
                }
        return true;
    }
}


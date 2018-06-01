
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


        Cookie[] cookies = req.getCookies();   //falls keine cookies vorhanden sind -> NullPointerException
        if (!ObjectUtils.isEmpty(cookies))
            System.out.println("1. first if");
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("auth")) {
                    System.out.println("2. "+cookie.getName());
                    String auth = cookie.getValue();
                    System.out.println("3. Dies steht in Auth Cookie: " + auth);
                    if (auth != null) {
                        System.out.println("4. CL auth != null");



                        //System.out.println(test);
                        String uid = template.opsForValue().get("auth:" + auth + ":uid");

                        System.out.println("5. uid durch cookie " + uid);
                        if (uid != null) {
                            System.out.println("6. uid != null");
                            //String name2 = (String) template.boundHashOps(uid).get("name");
                            String name = (String) template.opsForHash().get(uid, "username");
                            System.out.println("7. Füttere SimpleSec");
                            System.out.println("8. " + name);
                            System.out.println("9. " +uid);
                            SimpleSecurity.setUser(name, uid);

                            System.out.println("10. prehandle done");
                            System.out.println();
                            return true;
                        }
                    }
                }
        return true; //return false;
    }
}


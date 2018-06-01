package de.hska.lkit.demo.redis.configuration;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SimpleInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger =
            Logger.getLogger(SimpleInterceptor.class);
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        logger.info("Inside the prehandle");
        return true;
    }

}

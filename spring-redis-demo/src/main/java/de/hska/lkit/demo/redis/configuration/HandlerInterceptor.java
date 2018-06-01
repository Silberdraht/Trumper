package de.hska.lkit.demo.redis.configuration;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerInterceptor {

    boolean preHandle(HttpServletRequest request,
                      HttpServletResponse response, Object handler);
    void postHandle(HttpServletRequest request,
                    HttpServletResponse response, Object handler,
                    ModelAndView modelAndView);
    void afterCompletion(HttpServletRequest request,
                         HttpServletResponse response, Object handler, Exception ex);
}

package com.example.user.interceptor;

import com.example.common.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否需要拦截(线程中是否有用户)
        if (UserHolder.getUserId() == null){
            //没有 拦截 设置状态码
            response.setStatus(401);
            return false;
        }
        //有，放行
        return true;
    }
}

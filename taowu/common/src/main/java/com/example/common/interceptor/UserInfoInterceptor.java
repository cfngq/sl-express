package com.example.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.common.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的用户信息
        String userId = request.getHeader("user-info");
        //判断是否为空
        if (StrUtil.isNotBlank(userId)){
            //不为空 线程保存用户信息
            UserHolder.saveUserId(Long.valueOf(userId));
        }
        //放行
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUserId();
    }
}

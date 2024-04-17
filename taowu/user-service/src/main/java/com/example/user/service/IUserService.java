package com.example.user.service;

import com.example.common.result.Result;
import com.example.user.domain.dto.UserLoginDTO;
import com.example.user.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.user.domain.vo.UserLoginVo;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-31
 */
public interface IUserService extends IService<User> {

    Result<String> sendCode(String phone);

    Result<UserLoginVo> login(UserLoginDTO loginDTO);

    Result<User> getMeById(Long userId);

    Result<String> loginOut(HttpServletRequest request);

    Result<String> deductMoney(Integer amount);
}

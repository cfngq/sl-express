package com.example.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.result.Result;
import com.example.common.utils.CacheClient;
import com.example.common.utils.RegexUtils;
import com.example.common.utils.UserHolder;
import com.example.user.domain.dto.UserLoginDTO;
import com.example.user.domain.po.User;
import com.example.user.domain.vo.UserLoginVo;
import com.example.user.mapper.UserMapper;
import com.example.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.example.common.constant.RedisConstants.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final CacheClient cacheClient;
    //发送验证码
    @Override
    public Result<String> sendCode(String phone) {
        //校验手机号合法性
        if (RegexUtils.isPhoneInvalid(phone)){
            //不合法 返回错误信息
            return Result.error("手机号错误");
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(4);
        //将验证码存入redis
        cacheClient.set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //发送验证码
        log.info("验证码为：{}",code);
        return Result.success(code);
    }

    //基于手机号与验证码登录
    @Override
    public Result<UserLoginVo> login(UserLoginDTO loginDTO) {
        //获取手机号和密码信息
        String phone = loginDTO.getPhone();
        String code = loginDTO.getCode();
        //校验手机合法性
        if (RegexUtils.isPhoneInvalid(phone)){
            //手机号错误
            return Result.error("手机号错误");
        }
        //检验验证码
        //从redis中获取验证码
        String redisCode = cacheClient.get(LOGIN_CODE_KEY + phone);
        if (redisCode == null || !redisCode.equals(code)){
            return Result.error("验证码错误");
        }
        //根据手机号查询用户
        User user = query().eq("phone", phone).one();
        //用户是否存在
        if (user == null){
            //用户不存在，创建新用户
            user = createUserWithPhone(loginDTO);
        }
        //用户状态是否正常
        if (user.getStatus() != 1){
            throw new RuntimeException("用户状态异常");
        }
        //生成token
        String token = UUID.randomUUID().toString(true);
        String tokenKey = LOGIN_USER_KEY + token;
        //将信息保存在redis中
        cacheClient.set(tokenKey,user.getId().toString(),LOGIN_USER_TTL,TimeUnit.MINUTES);
        //封装返回对象
        UserLoginVo userLoginVo = BeanUtil.copyProperties(user, UserLoginVo.class);
        userLoginVo.setToken(token);
        //返回
        return Result.success(userLoginVo);
    }

    //查询个人信息
    @Override
    public Result<User> getMeById(Long userId) {
        User user = cacheClient.queryWithPassThrough(USER_KEY, userId, User.class, this::getById, USER_TTL, TimeUnit.MINUTES);
        if (user == null){
            return Result.error("错误，个人信息不存在");
        }
        return Result.success(user);
    }

    //登出
    @Override
    public Result<String> loginOut(HttpServletRequest request) {
        //获取请求头中的token
        String token = request.getHeader("authorization");
        String tokenKey = LOGIN_USER_KEY + token;
        if (!cacheClient.delete(tokenKey)){
            return Result.error("错误,登出失败");
        }
        return Result.success("登出成功");
    }

    //用户支付
    @Override
    public Result<String> deductMoney(Integer amount) {
        //根据用户id进行余额扣减
        Long userId = UserHolder.getUserId();
        try {
            lambdaUpdate()
                    .set(getById(userId).getBalance()-amount>=0,User::getBalance,getById(userId).getBalance()-amount)
                    .eq(User::getId,userId)
                    .update();
        } catch (Exception e) {
            log.error("用户余额不足，扣款失败");
            return Result.error("支付失败，余额可能不足");
        }
        return Result.success("支付成功，已扣减余额");
    }

    //新建用户
    private User createUserWithPhone(UserLoginDTO loginDTO) {
        //新建用户
        User user = new User();
        //存储用户信息
        user.setPhone(loginDTO.getPhone());
        user.setBalance(1000);
        user.setStatus(1);
        user.setPassword("123456");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setUsername("user_"+RandomUtil.randomNumbers(5));
        //保存
        save(user);
        return user;
    }
}

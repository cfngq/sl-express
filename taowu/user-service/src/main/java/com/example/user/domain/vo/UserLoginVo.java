package com.example.user.domain.vo;

import lombok.Data;

@Data
public class UserLoginVo {
    private String token;
    private Long userId;
    private String username;
    private Integer balance;
}

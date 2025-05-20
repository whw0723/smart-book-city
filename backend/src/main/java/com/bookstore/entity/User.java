package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String address;

    // 0表示普通用户，1表示管理员
    private Integer role = 0;

    // 用户账户余额
    private BigDecimal balance = BigDecimal.ZERO;
}
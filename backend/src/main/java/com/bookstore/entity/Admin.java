package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private Date createTime = new Date();

    private Date updateTime = new Date();
}

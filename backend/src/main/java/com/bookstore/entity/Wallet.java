package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private Long id;
    
    private User user;
    
    private BigDecimal balance;
    
    private Date createTime;
    
    private Date updateTime;
}

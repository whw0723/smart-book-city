package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    private Long id;
    
    private Wallet wallet;
    
    private BigDecimal amount;
    
    // 交易类型: DEPOSIT=充值, WITHDRAW=提现, PAYMENT=支付
    private String type;
    
    private String description;
    
    private Order relatedOrder;
    
    // 交易状态: SUCCESS=成功, FAILED=失败, PENDING=处理中
    private String status;
    
    private Date createTime;
}

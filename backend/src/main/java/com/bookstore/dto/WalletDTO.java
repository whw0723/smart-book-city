package com.bookstore.dto;

import com.bookstore.entity.Wallet;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class WalletDTO {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal balance;
    private Date createTime;
    private Date updateTime;

    public WalletDTO(Wallet wallet) {
        this.id = wallet.getId();
        if (wallet.getUser() != null) {
            this.userId = wallet.getUser().getId();
            this.username = wallet.getUser().getUsername();
        }
        this.balance = wallet.getBalance();
        this.createTime = wallet.getCreateTime();
        this.updateTime = wallet.getUpdateTime();
    }
}

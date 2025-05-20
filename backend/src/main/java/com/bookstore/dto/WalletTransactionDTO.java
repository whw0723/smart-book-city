package com.bookstore.dto;

import com.bookstore.entity.WalletTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class WalletTransactionDTO {
    private Long id;
    private Long walletId;
    private Long userId;
    private String username;
    private BigDecimal amount;
    private String type;
    private String description;
    private Long relatedOrderId;
    private String orderNumber;
    private String status;
    private Date createTime;

    public WalletTransactionDTO(WalletTransaction transaction) {
        this.id = transaction.getId();
        
        if (transaction.getWallet() != null) {
            this.walletId = transaction.getWallet().getId();
            
            if (transaction.getWallet().getUser() != null) {
                this.userId = transaction.getWallet().getUser().getId();
                this.username = transaction.getWallet().getUser().getUsername();
            }
        }
        
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.description = transaction.getDescription();
        
        if (transaction.getRelatedOrder() != null) {
            this.relatedOrderId = transaction.getRelatedOrder().getId();
            this.orderNumber = transaction.getRelatedOrder().getOrderNumber();
        }
        
        this.status = transaction.getStatus();
        this.createTime = transaction.getCreateTime();
    }
}

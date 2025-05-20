package com.bookstore.mapper;

import com.bookstore.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WalletTransactionMapper {
    
    /**
     * 根据ID查找交易记录
     */
    WalletTransaction findById(Long id);
    
    /**
     * 根据钱包ID查找交易记录
     */
    List<WalletTransaction> findByWalletId(Long walletId);
    
    /**
     * 根据用户ID查找交易记录
     */
    List<WalletTransaction> findByUserId(Long userId);
    
    /**
     * 根据订单ID查找交易记录
     */
    List<WalletTransaction> findByOrderId(Long orderId);
    
    /**
     * 保存交易记录
     */
    int save(WalletTransaction transaction);
    
    /**
     * 更新交易记录
     */
    int update(WalletTransaction transaction);
    
    /**
     * 删除交易记录
     */
    int deleteById(Long id);
}

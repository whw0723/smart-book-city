package com.bookstore.mapper;

import com.bookstore.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface WalletMapper {
    
    /**
     * 根据ID查找钱包
     */
    Wallet findById(Long id);
    
    /**
     * 根据用户ID查找钱包
     */
    Wallet findByUserId(Long userId);
    
    /**
     * 创建钱包
     */
    int save(Wallet wallet);
    
    /**
     * 更新钱包
     */
    int update(Wallet wallet);
    
    /**
     * 更新钱包余额
     */
    int updateBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
    
    /**
     * 删除钱包
     */
    int deleteById(Long id);
}

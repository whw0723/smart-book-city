package com.bookstore.mapper;

import com.bookstore.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartItemMapper {

    /**
     * 查找用户的所有购物车项
     */
    List<CartItem> findByUserId(Long userId);
    
    /**
     * 根据ID查找购物车项
     */
    CartItem findById(Long id);
    
    /**
     * 查找用户的特定图书的购物车项
     */
    CartItem findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    /**
     * 保存购物车项
     */
    int save(CartItem cartItem);
    
    /**
     * 更新购物车项
     */
    int update(CartItem cartItem);
    
    /**
     * 删除购物车项
     */
    int deleteById(Long id);
    
    /**
     * 删除用户的所有购物车项
     */
    int deleteByUserId(Long userId);
    
    /**
     * 删除用户的特定图书的购物车项
     */
    int deleteByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}

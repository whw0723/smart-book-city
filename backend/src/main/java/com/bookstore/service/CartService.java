package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.User;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CartItemMapper;
import com.bookstore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    /**
     * 获取用户的购物车
     */
    public List<CartItem> getUserCart(Long userId) {
        return cartItemMapper.findByUserId(userId);
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    public CartItem addToCart(Long userId, Long bookId, Integer quantity) {
        // 验证用户和图书是否存在
        User user = userMapper.findById(userId);
        Book book = bookMapper.findById(bookId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 检查购物车中是否已存在该商品
        CartItem existingItem = cartItemMapper.findByUserIdAndBookId(userId, bookId);

        if (existingItem != null) {
            // 如果已存在，更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemMapper.update(existingItem);
            return existingItem;
        } else {
            // 如果不存在，创建新的购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setBook(book);
            cartItem.setQuantity(quantity);
            cartItemMapper.save(cartItem);
            return cartItem;
        }
    }

    /**
     * 更新购物车项数量
     */
    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemMapper.findById(cartItemId);
        if (cartItem == null) {
            throw new RuntimeException("购物车项不存在");
        }

        cartItem.setQuantity(quantity);
        cartItemMapper.update(cartItem);
        return cartItem;
    }

    /**
     * 更新用户特定图书的购物车项数量
     */
    @Transactional
    public CartItem updateCartItemQuantityByUserAndBook(Long userId, Long bookId, Integer quantity) {
        CartItem cartItem = cartItemMapper.findByUserIdAndBookId(userId, bookId);
        if (cartItem == null) {
            throw new RuntimeException("购物车项不存在");
        }

        cartItem.setQuantity(quantity);
        cartItemMapper.update(cartItem);
        return cartItem;
    }

    /**
     * 从购物车中移除商品
     */
    @Transactional
    public boolean removeFromCart(Long cartItemId) {
        return cartItemMapper.deleteById(cartItemId) > 0;
    }

    /**
     * 从购物车中移除用户的特定图书
     */
    @Transactional
    public boolean removeFromCartByUserAndBook(Long userId, Long bookId) {
        return cartItemMapper.deleteByUserIdAndBookId(userId, bookId) > 0;
    }

    /**
     * 清空用户的购物车
     */
    @Transactional
    public boolean clearCart(Long userId) {
        return cartItemMapper.deleteByUserId(userId) > 0;
    }
}

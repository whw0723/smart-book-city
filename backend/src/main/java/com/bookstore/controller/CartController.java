package com.bookstore.controller;

import com.bookstore.entity.CartItem;
import com.bookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取用户的购物车
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCart(@PathVariable Long userId) {
        try {
            List<CartItem> cartItems = cartService.getUserCart(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            Long bookId = Long.parseLong(payload.get("bookId").toString());
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());

            CartItem cartItem = cartService.addToCart(userId, bookId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "添加到购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新购物车项数量
     */
    @PutMapping("/item/{id}")
    public ResponseEntity<?> updateCartItemQuantity(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());
            CartItem cartItem = cartService.updateCartItemQuantity(id, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "更新购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新用户特定图书的购物车项数量
     */
    @PutMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<?> updateCartItemByUserAndBook(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @RequestBody Map<String, Object> payload) {
        try {
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());
            CartItem cartItem = cartService.updateCartItemQuantityByUserAndBook(userId, bookId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "更新购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从购物车中移除商品
     */
    @DeleteMapping("/item/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            boolean success = cartService.removeFromCart(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "从购物车移除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从购物车中移除用户的特定图书
     */
    @DeleteMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<?> removeFromCartByUserAndBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        try {
            boolean success = cartService.removeFromCartByUserAndBook(userId, bookId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "从购物车移除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清空用户的购物车
     */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            boolean success = cartService.clearCart(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "清空购物车失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

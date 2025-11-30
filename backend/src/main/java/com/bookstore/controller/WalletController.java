package com.bookstore.controller;

import com.bookstore.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "http://localhost:5173")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * 获取用户余额
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getUserBalance(userId));
    }

    /**
     * 充值
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        return ResponseEntity.ok(walletService.depositWithResponse(userId, payload));
    }

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        return ResponseEntity.ok(walletService.withdrawWithResponse(userId, payload));
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public ResponseEntity<?> payOrder(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        return ResponseEntity.ok(walletService.payOrderWithResponse(userId, payload));
    }

    /**
     * 退款
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        return ResponseEntity.ok(walletService.refundWithResponse(userId, payload));
    }

    /**
     * 批量支付订单
     */
    @PostMapping("/batch-pay")
    public ResponseEntity<?> batchPayOrders(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("userId")).longValue();
        return ResponseEntity.ok(walletService.batchPayOrdersWithResponse(userId, payload));
    }
}

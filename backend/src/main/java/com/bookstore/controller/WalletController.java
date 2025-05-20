package com.bookstore.controller;

import com.bookstore.entity.User;
import com.bookstore.entity.Order;
import com.bookstore.entity.Wallet;
import com.bookstore.entity.WalletTransaction;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.bookstore.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "http://localhost:5173")
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletService walletService;

    /**
     * 获取用户余额
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserBalance(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取用户钱包
            Wallet wallet = walletService.getUserWallet(userId);
            System.out.println("用户钱包信息: " + wallet);

            // 获取用户交易记录
            List<WalletTransaction> transactions = walletService.getWalletTransactions(userId);
            System.out.println("用户交易记录数量: " + (transactions != null ? transactions.size() : 0));

            if (transactions != null && !transactions.isEmpty()) {
                for (WalletTransaction transaction : transactions) {
                    System.out.println("交易记录: ID=" + transaction.getId() +
                                      ", 类型=" + transaction.getType() +
                                      ", 金额=" + transaction.getAmount() +
                                      ", 状态=" + transaction.getStatus());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("walletId", wallet != null ? wallet.getId() : null);
            response.put("walletBalance", wallet != null ? wallet.getBalance() : BigDecimal.ZERO);
            response.put("transactionCount", transactions != null ? transactions.size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("获取余额失败: " + e.getMessage());
        }
    }

    /**
     * 充值
     */
    @PostMapping("/deposit")
    @Transactional
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            // 充值并创建交易记录
            WalletTransaction transaction = walletService.deposit(userId, amount, "账户充值");

            // 获取更新后的用户信息
            User user = userService.getUserById(userId);

            System.out.println("充值成功 - 用户ID: " + userId + ", 金额: " + amount +
                              ", 交易记录ID: " + transaction.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("transactionId", transaction.getId());
            response.put("message", "充值成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("充值失败: " + e.getMessage());
        }
    }

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    @Transactional
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            // 提现并创建交易记录
            WalletTransaction transaction = walletService.withdraw(userId, amount, "账户提现");

            // 获取更新后的用户信息
            User user = userService.getUserById(userId);

            System.out.println("提现成功 - 用户ID: " + userId + ", 金额: " + amount +
                              ", 交易记录ID: " + transaction.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("transactionId", transaction.getId());
            response.put("message", "提现成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("提现失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    @Transactional
    public ResponseEntity<?> payOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            Long orderId = Long.parseLong(payload.get("orderId").toString());

            // 获取订单信息
            var order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body("订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != 0) {
                return ResponseEntity.badRequest().body("订单状态不正确，无法支付");
            }

            // 支付订单并创建交易记录
            WalletTransaction transaction = walletService.payOrder(userId, orderId);

            // 获取更新后的用户信息
            User user = userService.getUserById(userId);

            System.out.println("订单支付成功 - 用户ID: " + userId + ", 订单ID: " + orderId +
                              ", 金额: " + order.getTotalAmount() +
                              ", 交易记录ID: " + transaction.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("orderId", orderId);
            response.put("message", "支付成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("支付失败: " + e.getMessage());
        }
    }

    /**
     * 退款
     */
    @PostMapping("/refund")
    @Transactional
    public ResponseEntity<?> refund(@RequestBody Map<String, Object> payload) {
        try {
            Long orderId = Long.parseLong(payload.get("orderId").toString());

            // 获取订单信息
            var order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body("订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != 1) {
                return ResponseEntity.badRequest().body("订单状态不正确，无法退款");
            }

            // 退款并创建交易记录
            WalletTransaction transaction = walletService.refund(orderId);

            // 获取更新后的用户信息
            User user = userService.getUserById(order.getUser().getId());

            System.out.println("订单退款成功 - 用户ID: " + order.getUser().getId() + ", 订单ID: " + orderId +
                              ", 金额: " + order.getTotalAmount() +
                              ", 交易记录ID: " + transaction.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("orderId", orderId);
            response.put("message", "退款成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("退款失败: " + e.getMessage());
        }
    }

    /**
     * 批量支付订单
     */
    @PostMapping("/batch-pay")
    @Transactional
    public ResponseEntity<?> batchPayOrders(@RequestBody Map<String, Object> payload) {
        try {
            // 打印接收到的完整请求数据，便于调试
            System.out.println("接收到批量支付请求，完整数据: " + payload);

            if (!payload.containsKey("userId")) {
                return ResponseEntity.badRequest().body("缺少用户ID参数");
            }

            Long userId;
            try {
                userId = Long.parseLong(payload.get("userId").toString());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("用户ID格式不正确: " + payload.get("userId"));
            }

            // 更健壮的方式处理orderIds
            List<Integer> orderIds = new ArrayList<>();
            Object orderIdsObj = payload.get("orderIds");

            if (orderIdsObj == null) {
                return ResponseEntity.badRequest().body("缺少订单ID列表参数");
            }

            if (orderIdsObj instanceof List) {
                List<?> list = (List<?>) orderIdsObj;
                for (Object item : list) {
                    try {
                        if (item instanceof Integer) {
                            orderIds.add((Integer) item);
                        } else if (item instanceof Number) {
                            orderIds.add(((Number) item).intValue());
                        } else if (item instanceof String) {
                            orderIds.add(Integer.parseInt((String) item));
                        } else {
                            System.out.println("无法处理的订单ID类型: " + (item != null ? item.getClass().getName() : "null") + ", 值: " + item);
                        }
                    } catch (Exception e) {
                        System.out.println("处理订单ID时出错: " + e.getMessage() + ", 值: " + item);
                    }
                }
            } else {
                return ResponseEntity.badRequest().body("订单ID列表格式不正确");
            }

            System.out.println("处理后的订单IDs: " + orderIds);

            if (orderIds == null || orderIds.isEmpty()) {
                return ResponseEntity.badRequest().body("订单ID列表不能为空");
            }

            // 计算所有订单的总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Order> ordersToPay = new ArrayList<>();

            for (Integer orderId : orderIds) {
                Order order = orderService.getOrderById(orderId.longValue());
                if (order == null) {
                    return ResponseEntity.badRequest().body("订单ID " + orderId + " 不存在");
                }

                // 检查订单状态
                if (order.getStatus() != 0) {
                    return ResponseEntity.badRequest().body("订单ID " + orderId + " 状态不正确，无法支付");
                }

                totalAmount = totalAmount.add(order.getTotalAmount());
                ordersToPay.add(order);
            }

            // 检查用户余额是否足够
            User user = userService.getUserById(userId);
            if (user.getBalance().compareTo(totalAmount) < 0) {
                return ResponseEntity.badRequest().body("余额不足，请先充值");
            }

            // 支付每个订单并创建交易记录
            List<Long> successfulOrderIds = new ArrayList<>();
            List<WalletTransaction> transactions = new ArrayList<>();

            for (Order order : ordersToPay) {
                try {
                    // 为每个订单创建交易记录
                    WalletTransaction transaction = walletService.payOrder(userId, order.getId());
                    transactions.add(transaction);
                    successfulOrderIds.add(order.getId());

                    System.out.println("订单支付成功 - 用户ID: " + userId + ", 订单ID: " + order.getId() +
                                      ", 金额: " + order.getTotalAmount() +
                                      ", 交易记录ID: " + transaction.getId());
                } catch (Exception e) {
                    System.err.println("订单支付失败 - 订单ID: " + order.getId() + ", 错误: " + e.getMessage());
                }
            }

            // 获取更新后的用户信息
            user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("balance", user.getBalance());
            response.put("orderIds", successfulOrderIds);
            response.put("totalAmount", totalAmount);
            response.put("message", "批量支付成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("批量支付失败: " + e.getMessage());
        }
    }
}

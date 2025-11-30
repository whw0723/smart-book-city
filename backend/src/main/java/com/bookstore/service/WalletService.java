package com.bookstore.service;

import com.bookstore.entity.Order;
import com.bookstore.entity.User;
import com.bookstore.entity.Wallet;
import com.bookstore.entity.WalletTransaction;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.UserMapper;
import com.bookstore.mapper.WalletMapper;
import com.bookstore.mapper.WalletTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WalletService {
    
    @Autowired
    private WalletMapper walletMapper;
    
    @Autowired
    private WalletTransactionMapper walletTransactionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取用户钱包
     */
    public Wallet getUserWallet(Long userId) {
        Wallet wallet = walletMapper.findByUserId(userId);

        // 如果用户没有钱包，则创建一个
        if (wallet == null) {
            User user = userMapper.findById(userId);
            if (user != null) {
                wallet = new Wallet();
                wallet.setUser(user);
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setCreateTime(new Date());
                wallet.setUpdateTime(new Date());
                walletMapper.save(wallet);

                // 重新获取钱包信息
                wallet = walletMapper.findByUserId(userId);
            }
        }

        return wallet;
    }

    /**
     * 获取钱包交易记录
     */
    public List<WalletTransaction> getWalletTransactions(Long userId) {
        return walletTransactionMapper.findByUserId(userId);
    }

    /**
     * 充值
     */
    @Transactional
    public WalletTransaction deposit(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }

        Wallet wallet = getUserWallet(userId);

        // 更新钱包余额
        walletMapper.updateBalance(wallet.getId(), amount);
        
        // 同时更新用户余额
        userMapper.updateBalance(userId, amount);

        // 创建交易记录
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setDescription(description);
        transaction.setStatus("SUCCESS");
        transaction.setCreateTime(new Date());

        walletTransactionMapper.save(transaction);

        return transaction;
    }

    /**
     * 提现
     */
    @Transactional
    public WalletTransaction withdraw(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于0");
        }

        Wallet wallet = getUserWallet(userId);

        // 检查余额是否足够
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额不足");
        }

        // 更新钱包余额（减去提现金额）
        walletMapper.updateBalance(wallet.getId(), amount.negate());
        
        // 同时更新用户余额
        userMapper.updateBalance(userId, amount.negate());

        // 创建交易记录
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount.negate());
        transaction.setType("WITHDRAW");
        transaction.setDescription(description);
        transaction.setStatus("SUCCESS");
        transaction.setCreateTime(new Date());

        walletTransactionMapper.save(transaction);

        return transaction;
    }

    /**
     * 支付订单
     */
    @Transactional
    public WalletTransaction payOrder(Long userId, Long orderId) {
        Wallet wallet = getUserWallet(userId);
        Order order = orderMapper.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("订单状态不正确，无法支付");
        }

        // 检查余额是否足够
        if (wallet.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new IllegalArgumentException("余额不足，请先充值");
        }

        // 更新钱包余额（减去订单金额）
        walletMapper.updateBalance(wallet.getId(), order.getTotalAmount().negate());
        
        // 同时更新用户余额
        userMapper.updateBalance(userId, order.getTotalAmount().negate());

        // 创建交易记录
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(order.getTotalAmount().negate());
        transaction.setType("PAYMENT");
        transaction.setDescription("支付订单: " + order.getOrderNumber());
        transaction.setRelatedOrder(order);
        transaction.setStatus("SUCCESS");
        transaction.setCreateTime(new Date());

        walletTransactionMapper.save(transaction);

        // 更新订单状态为已完成
        order.setStatus(1);
        orderMapper.update(order);

        return transaction;
    }

    /**
     * 退款
     */
    @Transactional
    public WalletTransaction refund(Long orderId) {
        Order order = orderMapper.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new IllegalArgumentException("订单状态不正确，无法退款");
        }

        // 获取用户钱包
        Wallet wallet = getUserWallet(order.getUser().getId());

        // 更新钱包余额（加上订单金额）
        walletMapper.updateBalance(wallet.getId(), order.getTotalAmount());
        
        // 同时更新用户余额
        userMapper.updateBalance(order.getUser().getId(), order.getTotalAmount());

        // 创建交易记录
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(order.getTotalAmount());
        transaction.setType("REFUND");
        transaction.setDescription("订单退款: " + order.getOrderNumber());
        transaction.setRelatedOrder(order);
        transaction.setStatus("SUCCESS");
        transaction.setCreateTime(new Date());

        walletTransactionMapper.save(transaction);

        // 更新订单状态为已取消
        order.setStatus(4);
        orderMapper.update(order);

        return transaction;
    }
    
    /**
     * 获取用户余额
     */
    public Map<String, Object> getUserBalance(Long userId) {
        Map<String, Object> response = new HashMap<>();
        User user = userMapper.findById(userId);
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        Wallet wallet = getUserWallet(userId);
        response.put("success", true);
        response.put("balance", wallet.getBalance());
        return response;
    }
    
    /**
     * 充值（适配Controller接口）
     */
    @Transactional
    public Map<String, Object> depositWithResponse(Long userId, Map<String, Object> requestBody) {
        try {
            BigDecimal amount = new BigDecimal(requestBody.get("amount").toString());
            String description = requestBody.get("description") != null ? 
                requestBody.get("description").toString() : "用户充值";
            
            WalletTransaction transaction = deposit(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "充值成功");
            response.put("transactionId", transaction.getId());
            response.put("balance", getUserWallet(userId).getBalance());
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "充值失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 提现（适配Controller接口）
     */
    @Transactional
    public Map<String, Object> withdrawWithResponse(Long userId, Map<String, Object> requestBody) {
        try {
            BigDecimal amount = new BigDecimal(requestBody.get("amount").toString());
            String description = requestBody.get("description") != null ? 
                requestBody.get("description").toString() : "用户提现";
            
            WalletTransaction transaction = withdraw(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "提现成功");
            response.put("transactionId", transaction.getId());
            response.put("balance", getUserWallet(userId).getBalance());
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 支付订单（适配Controller接口）
     */
    @Transactional
    public Map<String, Object> payOrderWithResponse(Long userId, Map<String, Object> requestBody) {
        try {
            Long orderId = Long.valueOf(requestBody.get("orderId").toString());
            
            // 检查订单是否属于该用户
            Order order = orderMapper.findById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在");
            }
            
            if (!order.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("无权支付该订单");
            }
            
            WalletTransaction transaction = payOrder(userId, orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "支付成功");
            response.put("transactionId", transaction.getId());
            response.put("balance", getUserWallet(userId).getBalance());
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "支付失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 批量支付订单
     */
    @Transactional
    public Map<String, Object> batchPayOrders(Long userId, List<Long> orderIds) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 检查用户是否存在
            User user = userMapper.findById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Order> orders = new ArrayList<>();
            
            for (Long orderId : orderIds) {
                Order order = orderMapper.findById(orderId);
                if (order == null) {
                    results.put(orderId.toString(), "订单不存在");
                    continue;
                }
                
                if (!order.getUser().getId().equals(userId)) {
                    results.put(orderId.toString(), "无权支付该订单");
                    continue;
                }
                
                if (order.getStatus() != 0) {
                    results.put(orderId.toString(), "订单状态不正确，无法支付");
                    continue;
                }
                
                totalAmount = totalAmount.add(order.getTotalAmount());
                orders.add(order);
            }
            
            // 检查余额是否足够
            Wallet wallet = getUserWallet(userId);
            if (wallet.getBalance().compareTo(totalAmount) < 0) {
                response.put("success", false);
                response.put("message", "余额不足，请先充值");
                response.put("results", results);
                return response;
            }
            
            // 执行批量支付
            for (Order order : orders) {
                try {
                    // 更新钱包余额
                    walletMapper.updateBalance(wallet.getId(), order.getTotalAmount().negate());
                    
                    // 更新用户余额
                    userMapper.updateBalance(userId, order.getTotalAmount().negate());
                    
                    // 创建交易记录
                    WalletTransaction transaction = new WalletTransaction();
                    transaction.setWallet(wallet);
                    transaction.setAmount(order.getTotalAmount().negate());
                    transaction.setType("PAYMENT");
                    transaction.setDescription("批量支付订单: " + order.getOrderNumber());
                    transaction.setRelatedOrder(order);
                    transaction.setStatus("SUCCESS");
                    transaction.setCreateTime(new Date());
                    
                    walletTransactionMapper.save(transaction);
                    
                    // 更新订单状态
                    order.setStatus(1);
                    orderMapper.update(order);
                    
                    results.put(order.getId().toString(), "支付成功");
                } catch (Exception e) {
                    results.put(order.getId().toString(), "支付失败: " + e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "批量支付处理完成");
            response.put("results", results);
            response.put("balance", getUserWallet(userId).getBalance());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量支付失败: " + e.getMessage());
            response.put("results", results);
            return response;
        }
    }
    
    /**
     * 退款（适配Controller接口）
     */
    @Transactional
    public Map<String, Object> refundWithResponse(Long userId, Map<String, Object> requestBody) {
        try {
            Long orderId = Long.valueOf(requestBody.get("orderId").toString());
            
            // 检查订单是否属于该用户
            Order order = orderMapper.findById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在");
            }
            
            if (!order.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("无权退款该订单");
            }
            
            WalletTransaction transaction = refund(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "退款成功");
            response.put("transactionId", transaction.getId());
            response.put("balance", getUserWallet(userId).getBalance());
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "退款失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 批量支付订单（适配Controller接口）
     */
    @Transactional
    public Map<String, Object> batchPayOrdersWithResponse(Long userId, Map<String, Object> requestBody) {
        try {
            List<Long> orderIds = new ArrayList<>();
            List<?> orderIdList = (List<?>) requestBody.get("orderIds");
            
            for (Object orderIdObj : orderIdList) {
                if (orderIdObj instanceof Number) {
                    orderIds.add(((Number) orderIdObj).longValue());
                } else if (orderIdObj instanceof String) {
                    orderIds.add(Long.valueOf((String) orderIdObj));
                }
            }
            
            return batchPayOrders(userId, orderIds);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量支付处理失败: " + e.getMessage());
            return response;
        }
    }
}

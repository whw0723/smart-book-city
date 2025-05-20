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
import java.util.Date;
import java.util.List;

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
}

package com.bookstore.service;

import com.bookstore.entity.User;
import com.bookstore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    /**
     * 分页获取所有用户
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 包含分页数据和总数的Map
     */
    public Map<String, Object> getUsersPaged(int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<User> users = userMapper.findAllPaged(offset, size);
        int total = userMapper.count();

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查找用户
     */
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 根据用户名查找用户
     * 如果存在多个相同用户名的用户，返回第一个
     */
    public User getUserByUsername(String username) {
        try {
            return userMapper.findByUsername(username);
        } catch (Exception e) {
            // 记录错误但不抛出异常
            System.err.println("查询用户名时出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username) > 0;
    }

    /**
     * 保存用户
     * 在保存前检查用户名是否已存在
     * @throws IllegalArgumentException 如果用户名已存在
     */
    public User saveUser(User user) {
        // 再次检查用户名是否存在，确保数据一致性
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 如果用户没有设置余额，设置默认值
        if (user.getBalance() == null) {
            user.setBalance(new BigDecimal("0.00"));
        }

        userMapper.save(user);
        return user;
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        userMapper.update(user);
        return user;
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 获取用户余额
     */
    public BigDecimal getUserBalance(Long userId) {
        User user = userMapper.findById(userId);
        return user != null ? user.getBalance() : BigDecimal.ZERO;
    }

    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 变动金额（正数为增加，负数为减少）
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUserBalance(Long userId, BigDecimal amount) {
        // 检查用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查余额是否足够（如果是减少余额的操作）
        if (amount.compareTo(BigDecimal.ZERO) < 0 && user.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不足");
        }

        // 更新余额
        userMapper.updateBalance(userId, amount);

        // 重新获取用户信息
        return userMapper.findById(userId);
    }

    /**
     * 充值
     */
    @Transactional
    public User deposit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        return updateUserBalance(userId, amount);
    }

    /**
     * 提现
     */
    @Transactional
    public User withdraw(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于0");
        }
        return updateUserBalance(userId, amount.negate());
    }

    /**
     * 支付
     */
    @Transactional
    public User pay(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        return updateUserBalance(userId, amount.negate());
    }

    /**
     * 退款
     */
    @Transactional
    public User refund(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        return updateUserBalance(userId, amount);
    }
}

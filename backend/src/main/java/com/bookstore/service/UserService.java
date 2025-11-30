package com.bookstore.service;

import com.bookstore.entity.User;
import com.bookstore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
    
    /**
     * 检查用户名是否已存在
     */
    public Map<String, Object> checkUsername(String username) {
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            response.put("exists", false);
            response.put("message", "用户名不能为空");
            return response;
        }

        boolean exists = existsByUsername(username);
        response.put("exists", exists);
        response.put("message", exists ? "用户名已存在" : "用户名可用");
        return response;
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        User savedUser = saveUser(user);

        // 不返回密码
        savedUser.setPassword(null);
        return savedUser;
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public User login(User loginUser) {
        User user = getUserByUsername(loginUser.getUsername());
        if (user != null) {
            // 首先尝试使用密码编码器验证（用于加密后的密码）
            boolean passwordMatches = passwordEncoder.matches(loginUser.getPassword(), user.getPassword());
            
            // 如果密码验证失败，检查是否是明文密码（用于向后兼容）
            if (!passwordMatches && user.getPassword().equals(loginUser.getPassword())) {
                // 如果是明文密码验证成功，更新为加密密码
                passwordMatches = true;
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userMapper.update(user);
            }
            
            if (passwordMatches) {
                // 不返回密码
                user.setPassword(null);
                return user;
            }
        }
        throw new IllegalArgumentException("用户名或密码错误");
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public boolean changePassword(Long id, String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("当前密码和新密码不能为空");
        }
        
        User user = getUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证当前密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        // 更新密码，并对新密码进行加密
        user.setPassword(passwordEncoder.encode(newPassword));
        updateUser(user);
        return true;
    }
    
    /**
     * 获取用户订单数量
     */
    public Map<Long, Integer> getUserOrderCounts() {
        List<User> users = getAllUsers();
        Map<Long, Integer> orderCounts = new HashMap<>();

        for (User user : users) {
            int count = orderService.countByUserId(user.getId());
            orderCounts.put(user.getId(), count);
        }

        return orderCounts;
    }
}

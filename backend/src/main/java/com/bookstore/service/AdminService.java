package com.bookstore.service;

import com.bookstore.entity.Admin;
import com.bookstore.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 获取所有管理员
     */
    public List<Admin> getAllAdmins() {
        return adminMapper.findAll();
    }

    /**
     * 根据ID查找管理员
     */
    public Admin getAdminById(Long id) {
        return adminMapper.findById(id);
    }

    /**
     * 根据用户名查找管理员
     */
    public Admin getAdminByUsername(String username) {
        return adminMapper.findByUsername(username);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return adminMapper.existsByUsername(username) > 0;
    }

    /**
     * 保存管理员
     */
    public Admin saveAdmin(Admin admin) {
        // 对密码进行加密
        if (admin.getPassword() != null) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        adminMapper.save(admin);
        return admin;
    }

    /**
     * 更新管理员
     */
    public Admin updateAdmin(Admin admin) {
        adminMapper.update(admin);
        return admin;
    }

    /**
     * 删除管理员
     */
    public boolean deleteAdmin(Long id) {
        return adminMapper.deleteById(id) > 0;
    }
    
    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含管理员信息或错误消息
     */
    @Transactional
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }
        
        if (password == null || password.isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }
        
        // 查找管理员
        Admin admin = getAdminByUsername(username);
        
        // 初始密码验证状态为false
        boolean passwordValid = false;
        
        // 只有当管理员存在且密码不为空时才进行验证
        if (admin != null && admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            // 首先尝试使用密码编码器验证（用于加密后的密码）
            boolean matchesEncrypted = passwordEncoder.matches(password, admin.getPassword());
            
            // 如果密码验证失败，检查是否是明文密码（用于向后兼容）
            boolean matchesPlain = admin.getPassword().equals(password);
            
            // 只有在以下情况才认为密码有效：
            // 1. 使用BCrypt验证成功，或者
            // 2. 密码不是加密格式且明文匹配
            if (matchesEncrypted || (!admin.getPassword().startsWith("$2a$") && matchesPlain)) {
                passwordValid = true;
                
                // 如果是明文密码匹配，更新为加密密码
                if (matchesPlain && !admin.getPassword().startsWith("$2a$")) {
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setUpdateTime(new Date());
                    updateAdmin(admin);
                }
            }
        }
        
        // 只有密码验证成功才返回管理员信息
        if (passwordValid) {
            // 不返回密码
            admin.setPassword(null);
            result.put("success", true);
            result.put("admin", admin);
        } else {
            // 无论用户名是否存在，都返回相同的错误信息以防止用户名枚举攻击
            result.put("success", false);
            result.put("message", "管理员用户名或密码错误");
        }
        
        return result;
    }
    
    /**
     * 更新管理员信息
     * @param id 管理员ID
     * @param username 用户名
     * @param email 邮箱
     * @param phone 电话
     * @return 更新后的管理员信息或null（如果不存在）
     */
    public Admin updateAdminDetails(Long id, String username, String email, String phone) {
        Admin admin = getAdminById(id);
        if (admin != null) {
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPhone(phone);
            admin.setUpdateTime(new Date());
            return updateAdmin(admin);
        }
        return null;
    }
    
    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 包含操作结果和消息的Map
     */
    @Transactional
    public Map<String, Object> changePassword(Long id, String currentPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数验证
        if (currentPassword == null || newPassword == null) {
            result.put("success", false);
            result.put("message", "当前密码和新密码不能为空");
            return result;
        }
        
        try {
            Admin admin = getAdminById(id);
            if (admin == null) {
                result.put("success", false);
                result.put("message", "管理员不存在");
                return result;
            }
            
            // 验证当前密码是否正确（支持加密密码和明文密码）
            boolean passwordValid = passwordEncoder.matches(currentPassword, admin.getPassword());
            
            // 如果加密验证失败，检查是否为明文密码（向后兼容）
            if (!passwordValid && admin.getPassword().equals(currentPassword)) {
                passwordValid = true;
            }
            
            if (!passwordValid) {
                result.put("success", false);
                result.put("message", "当前密码不正确");
                return result;
            }
            
            // 更新密码（加密）
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setUpdateTime(new Date());
            updateAdmin(admin);
            
            result.put("success", true);
            result.put("message", "密码修改成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码修改失败: " + e.getMessage());
        }
        
        return result;
    }
}
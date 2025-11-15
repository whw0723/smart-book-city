package com.bookstore.service;

import com.bookstore.entity.Admin;
import com.bookstore.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

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
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        Admin admin = getAdminByUsername(username);
        if (admin != null && admin.getPassword().equals(password)) {
            // 不返回密码
            admin.setPassword(null);
            result.put("success", true);
            result.put("admin", admin);
        } else {
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
            
            // 验证当前密码是否正确
            if (!admin.getPassword().equals(currentPassword)) {
                result.put("success", false);
                result.put("message", "当前密码不正确");
                return result;
            }
            
            // 更新密码
            admin.setPassword(newPassword);
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
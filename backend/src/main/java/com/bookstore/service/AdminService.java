package com.bookstore.service;

import com.bookstore.entity.Admin;
import com.bookstore.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}

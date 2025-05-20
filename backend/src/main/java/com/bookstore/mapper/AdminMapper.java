package com.bookstore.mapper;

import com.bookstore.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    
    /**
     * 获取所有管理员
     */
    List<Admin> findAll();
    
    /**
     * 根据ID查找管理员
     */
    Admin findById(Long id);
    
    /**
     * 根据用户名查找管理员
     */
    Admin findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    int existsByUsername(String username);
    
    /**
     * 保存管理员
     */
    int save(Admin admin);
    
    /**
     * 更新管理员
     */
    int update(Admin admin);
    
    /**
     * 删除管理员
     */
    int deleteById(Long id);
}

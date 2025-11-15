package com.bookstore.controller;

import com.bookstore.entity.Admin;
import com.bookstore.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     * @param loginAdmin 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Admin loginAdmin) {
        Map<String, Object> result = adminService.login(loginAdmin.getUsername(), loginAdmin.getPassword());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        Admin admin = adminService.getAdminById(id);
        return admin != null ? ResponseEntity.ok(admin) : ResponseEntity.notFound().build();
    }

    /**
     * 更新管理员信息
     * @param id 管理员ID
     * @param adminDetails 管理员信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Admin adminDetails) {
        Admin updatedAdmin = adminService.updateAdminDetails(
                id, 
                adminDetails.getUsername(), 
                adminDetails.getEmail(), 
                adminDetails.getPhone()
        );
        
        return updatedAdmin != null ? ResponseEntity.ok(updatedAdmin) : ResponseEntity.notFound().build();
    }

    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param passwordRequest 包含当前密码和新密码的请求体
     * @return 修改结果
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordRequest) {

        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");

        Map<String, Object> result = adminService.changePassword(id, currentPassword, newPassword);
        return ResponseEntity.ok(result);
    }
}
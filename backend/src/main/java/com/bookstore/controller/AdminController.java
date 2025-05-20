package com.bookstore.controller;

import com.bookstore.entity.Admin;
import com.bookstore.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
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
        Admin admin = adminService.getAdminByUsername(loginAdmin.getUsername());
        if (admin != null && admin.getPassword().equals(loginAdmin.getPassword())) {
            // 不返回密码
            admin.setPassword(null);
            return ResponseEntity.ok(admin);
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "管理员用户名或密码错误");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        Admin admin = adminService.getAdminById(id);
        if (admin != null) {
            // 不返回密码
            admin.setPassword(null);
            return ResponseEntity.ok(admin);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 更新管理员信息
     * @param id 管理员ID
     * @param adminDetails 管理员信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Admin adminDetails) {
        Admin admin = adminService.getAdminById(id);
        if (admin != null) {
            admin.setUsername(adminDetails.getUsername());
            admin.setEmail(adminDetails.getEmail());
            admin.setPhone(adminDetails.getPhone());
            // 不更新密码，需要单独的接口处理
            Admin updatedAdmin = adminService.updateAdmin(admin);
            // 不返回密码
            updatedAdmin.setPassword(null);
            return ResponseEntity.ok(updatedAdmin);
        }
        return ResponseEntity.notFound().build();
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

        if (currentPassword == null || newPassword == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "当前密码和新密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Admin admin = adminService.getAdminById(id);
            if (admin == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证当前密码是否正确
            if (!admin.getPassword().equals(currentPassword)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "当前密码不正确");
                return ResponseEntity.badRequest().body(response);
            }

            // 更新密码
            admin.setPassword(newPassword);
            admin.setUpdateTime(new Date()); // 更新修改时间
            adminService.updateAdmin(admin);

            Map<String, String> response = new HashMap<>();
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码修改失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

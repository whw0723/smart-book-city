package com.bookstore.controller;

import com.bookstore.entity.User;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getUsersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = userService.getUsersPaged(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户名是否已存在
     * @param request 包含用户名的请求体
     * @return 用户名是否存在的响应
     */
    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("exists", false);
            response.put("message", "用户名不能为空");
            return ResponseEntity.ok(response);
        }

        boolean exists = userService.existsByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "用户名已存在" : "用户名可用");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(user.getUsername())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 保存用户
            User savedUser = userService.saveUser(user);

            // 不返回密码
            savedUser.setPassword(null);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            User user = userService.getUserByUsername(loginUser.getUsername());
            if (user != null && user.getPassword().equals(loginUser.getPassword())) {
                // 不返回密码
                user.setPassword(null);
                return ResponseEntity.ok(user);
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            // 不更新密码和角色，需要单独的接口处理
            return ResponseEntity.ok(userService.updateUser(user));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取用户订单数量
     * @return 用户ID和订单数量的映射
     */
    @GetMapping("/order-counts")
    public ResponseEntity<?> getUserOrderCounts() {
        try {
            List<User> users = userService.getAllUsers();
            Map<Long, Integer> orderCounts = new HashMap<>();

            for (User user : users) {
                int count = orderService.countByUserId(user.getId());
                orderCounts.put(user.getId(), count);
            }

            return ResponseEntity.ok(orderCounts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取用户订单数量失败: " + e.getMessage()));
        }
    }

    /**
     * 修改密码
     * @param id 用户ID
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
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证当前密码是否正确
            if (!user.getPassword().equals(currentPassword)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "当前密码不正确");
                return ResponseEntity.badRequest().body(response);
            }

            // 更新密码
            user.setPassword(newPassword);
            userService.updateUser(user);

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
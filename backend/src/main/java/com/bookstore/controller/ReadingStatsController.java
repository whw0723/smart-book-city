package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reading-stats")
@CrossOrigin(origins = "http://localhost:5173")
public class ReadingStatsController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户的阅读统计数据
     * @param userId 用户ID
     * @return 阅读统计数据
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReadingStats(@PathVariable Long userId) {
        try {
            // 验证用户是否存在
            if (userService.getUserById(userId) == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取用户的所有订单
            List<Order> userOrders = orderService.getUserOrders(userId);

            // 只统计已支付、已发货和已完成的订单
            List<Order> validOrders = userOrders.stream()
                    .filter(order -> order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3)
                    .collect(Collectors.toList());

            // 计算总购书数量
            int totalBooks = 0;
            BigDecimal totalSpent = BigDecimal.ZERO;

            // 统计每个分类的书籍数量
            Map<String, Integer> categoryCount = new HashMap<>();

            // 获取当前月份
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            Month currentMonth = now.getMonth();
            int booksThisMonth = 0;

            // 最近购买的图书列表
            List<Map<String, Object>> recentBooks = new ArrayList<>();

            for (Order order : validOrders) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        Book book = item.getBook();
                        int quantity = item.getQuantity();

                        // 累计总数和金额
                        totalBooks += quantity;
                        totalSpent = totalSpent.add(item.getPrice().multiply(new BigDecimal(quantity)));

                        // 统计分类
                        String category = book.getCategory();
                        if (category == null || category.isEmpty()) {
                            category = "未分类";
                        } else {
                            // 清理分类字符串，去除多余的空格、换行符等
                            category = category.trim().replaceAll("\\s+", "");

                            // 映射分类名称为中文（可根据需要调整）
                            switch (category.toLowerCase()) {
                                case "novel":
                                case "novell":
                                    category = "小说";
                                    break;
                                case "technology":
                                    category = "科技";
                                    break;
                                case "education":
                                    category = "教育";
                                    break;
                                case "economics":
                                    category = "经济";
                                    break;
                                case "children":
                                    category = "儿童";
                                    break;
                                case "biography":
                                    category = "传记";
                                    break;
                                case "history":
                                    category = "历史";
                                    break;
                                case "science":
                                    category = "科学";
                                    break;
                            }
                        }
                        categoryCount.put(category, categoryCount.getOrDefault(category, 0) + quantity);

                        // 统计本月购书数
                        if (order.getOrderDate() != null) {
                            LocalDateTime orderDate = order.getOrderDate();
                            int orderYear = orderDate.getYear();
                            Month orderMonth = orderDate.getMonth();

                            if (orderYear == currentYear && orderMonth == currentMonth) {
                                booksThisMonth += quantity;
                            }
                        }

                        // 添加到最近购买的图书列表
                        Map<String, Object> bookInfo = new HashMap<>();
                        bookInfo.put("id", book.getId());
                        bookInfo.put("title", book.getTitle());
                        bookInfo.put("author", book.getAuthor());
                        bookInfo.put("category", book.getCategory());
                        bookInfo.put("purchaseDate", order.getOrderDate().toLocalDate().toString());
                        recentBooks.add(bookInfo);
                    }
                }
            }

            // 对最近购买的图书按日期排序，并限制数量
            recentBooks.sort((b1, b2) -> ((String)b2.get("purchaseDate")).compareTo((String)b1.get("purchaseDate")));
            if (recentBooks.size() > 5) {
                recentBooks = recentBooks.subList(0, 5);
            }

            // 构建分类分布数据
            List<Map<String, Object>> categoryDistribution = categoryCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> category = new HashMap<>();
                        category.put("name", entry.getKey());
                        category.put("value", entry.getValue());
                        return category;
                    })
                    .collect(Collectors.toList());

            // 如果没有分类数据，添加一个默认分类
            if (categoryDistribution.isEmpty()) {
                Map<String, Object> defaultCategory = new HashMap<>();
                defaultCategory.put("name", "暂无数据");
                defaultCategory.put("value", 1);
                categoryDistribution.add(defaultCategory);
            }

            // 构建响应
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBooks", totalBooks);
            stats.put("totalSpent", totalSpent);
            stats.put("booksThisMonth", booksThisMonth);

            Map<String, Object> response = new HashMap<>();
            response.put("stats", stats);
            response.put("categoryDistribution", categoryDistribution);
            response.put("recentBooks", recentBooks);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("获取阅读统计失败: " + e.getMessage());
        }
    }
}

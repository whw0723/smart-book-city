package com.bookstore.controller;

import com.bookstore.entity.WalletTransaction;
import com.bookstore.mapper.WalletTransactionMapper;
import com.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private UserService userService;

    /**
     * 获取用户的交易记录
     * @param userId 用户ID
     * @param type 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 交易记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("接收到获取交易记录请求 - 用户ID: " + userId);
        System.out.println("请求参数 - 类型: " + type + ", 开始日期: " + startDate + ", 结束日期: " + endDate + ", 页码: " + page + ", 每页大小: " + size);

        try {
            // 验证用户是否存在
            if (userService.getUserById(userId) == null) {
                System.out.println("用户不存在: " + userId);
                return ResponseEntity.notFound().build();
            }

            // 获取用户的所有交易记录
            List<WalletTransaction> transactions = walletTransactionMapper.findByUserId(userId);

            // 确保transactions不为null
            if (transactions == null) {
                System.out.println("查询结果为null，初始化为空列表");
                transactions = new java.util.ArrayList<>();
            }

            System.out.println("查询到交易记录数量: " + transactions.size());

            // 根据类型筛选
            if (type != null && !type.equals("all")) {
                transactions = transactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase(type))
                        .collect(Collectors.toList());
            }

            // 根据日期范围筛选
            if (startDate != null && endDate != null) {
                try {
                    // 解析日期字符串为Date对象
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date start = sdf.parse(startDate);
                    Date end = sdf.parse(endDate);

                    // 设置结束日期为当天的最后一刻
                    Date endOfDay = new Date(end.getTime() + 24 * 60 * 60 * 1000 - 1);

                    transactions = transactions.stream()
                            .filter(t -> {
                                Date transactionDate = t.getCreateTime();
                                return !transactionDate.before(start) && !transactionDate.after(endOfDay);
                            })
                            .collect(Collectors.toList());
                } catch (ParseException e) {
                    // 日期解析错误，忽略日期筛选
                    System.err.println("日期解析错误: " + e.getMessage());
                }
            }

            // 计算总记录数
            int totalItems = transactions.size();

            // 分页
            int startItem = (page - 1) * size;
            List<WalletTransaction> pagedTransactions = transactions.stream()
                    .skip(startItem)
                    .limit(size)
                    .collect(Collectors.toList());

            System.out.println("分页后的交易记录数量: " + pagedTransactions.size());

            // 将WalletTransaction转换为前端友好的格式
            List<Map<String, Object>> formattedTransactions = new ArrayList<>();

            for (WalletTransaction transaction : pagedTransactions) {
                try {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", transaction.getId());
                    item.put("createTime", transaction.getCreateTime());
                    item.put("type", transaction.getType());
                    item.put("amount", transaction.getAmount());
                    item.put("description", transaction.getDescription());
                    item.put("status", transaction.getStatus());

                    // 添加关联订单信息（如果有）
                    if (transaction.getRelatedOrder() != null) {
                        item.put("relatedOrderId", transaction.getRelatedOrder().getId());
                        item.put("orderNumber", transaction.getRelatedOrder().getOrderNumber());
                    }

                    formattedTransactions.add(item);
                    System.out.println("处理交易记录: ID=" + transaction.getId() +
                                      ", 类型=" + transaction.getType() +
                                      ", 金额=" + transaction.getAmount() +
                                      ", 状态=" + transaction.getStatus());
                } catch (Exception e) {
                    System.err.println("处理交易记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("格式化后的交易记录数量: " + formattedTransactions.size());

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", formattedTransactions);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", (int) Math.ceil((double) totalItems / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("获取交易记录失败: " + e.getMessage());
        }
    }


}

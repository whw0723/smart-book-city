package com.bookstore.service;

import com.bookstore.entity.WalletTransaction;
import com.bookstore.mapper.WalletTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

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
     * @return 包含交易记录列表和分页信息的Map
     * @throws IllegalArgumentException 当请求参数无效时
     */
    public Map<String, Object> getUserTransactions(
            Long userId,
            String type,
            String startDate,
            String endDate,
            int page,
            int size) {

        // 验证参数
        validateParameters(userId, page, size);
        
        System.out.println("接收到获取交易记录请求 - 用户ID: " + userId);
        System.out.println("请求参数 - 类型: " + type + ", 开始日期: " + startDate + ", 结束日期: " + endDate + ", 页码: " + page + ", 每页大小: " + size);

        // 验证用户是否存在
        if (userService.getUserById(userId) == null) {
            System.out.println("用户不存在: " + userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在: " + userId);
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
                sdf.setLenient(false); // 严格解析日期格式
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                // 验证日期范围
                if (start.after(end)) {
                    throw new IllegalArgumentException("开始日期不能晚于结束日期");
                }

                // 设置结束日期为当天的最后一刻
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(end);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                Date endOfDay = calendar.getTime();

                transactions = transactions.stream()
                        .filter(t -> {
                            Date transactionDate = t.getCreateTime();
                            return transactionDate != null && 
                                   !transactionDate.before(start) && 
                                   !transactionDate.after(endOfDay);
                        })
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                // 日期解析错误，抛出异常
                throw new IllegalArgumentException("日期格式错误，请使用yyyy-MM-dd格式", e);
            }
        } else if (startDate != null || endDate != null) {
            // 如果只提供了一个日期参数，抛出异常
            throw new IllegalArgumentException("开始日期和结束日期必须同时提供");
        }

        // 按创建时间倒序排序
        transactions.sort(Comparator.comparing(WalletTransaction::getCreateTime).reversed());
        
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
                // 继续处理其他交易记录，不中断整个过程
            }
        }

        System.out.println("格式化后的交易记录数量: " + formattedTransactions.size());

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", formattedTransactions);
        response.put("currentPage", page);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalItems > 0 ? (int) Math.ceil((double) totalItems / size) : 1);
        response.put("pageSize", size);

        return response;
    }
    
    /**
     * 验证请求参数
     */
    private void validateParameters(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正整数");
        }
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于等于1");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }
}
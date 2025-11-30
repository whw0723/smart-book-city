package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    /**
     * 获取销售统计数据
     * @param period 时间周期：day, week, month, year
     * @return 销售统计数据
     */
    public List<Map<String, Object>> getSalesStatistics(String period) {
        List<Order> orders = orderService.getAllOrders();

        // 只统计已支付、已发货和已完成的订单
        List<Order> validOrders = orders.stream()
                .filter(order -> order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3)
                .collect(Collectors.toList());

        Map<String, BigDecimal> salesData = new LinkedHashMap<>();

        // 根据不同时间周期生成统计数据
        switch (period) {
            case "day":
                salesData = getDailySales(validOrders);
                break;
            case "week":
                salesData = getWeeklySales(validOrders);
                break;
            case "month":
                salesData = getMonthlySales(validOrders);
                break;
            case "year":
                salesData = getYearlySales(validOrders);
                break;
            default:
                // 这里会在Controller中处理无效参数的情况
                return null;
        }

        // 转换为前端需要的格式
        return salesData.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取畅销书籍排行
     * @param limit 返回的数量限制
     * @return 畅销书籍排行数据
     */
    public List<Map<String, Object>> getTopBooks(int limit) {
        List<Order> orders = orderService.getAllOrders();

        // 只统计已支付、已发货和已完成的订单
        List<Order> validOrders = orders.stream()
                .filter(order -> order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3)
                .collect(Collectors.toList());

        // 统计每本书的销量
        Map<Long, Integer> bookSales = new HashMap<>();

        for (Order order : validOrders) {
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Long bookId = item.getBook().getId();
                    int quantity = item.getQuantity();

                    bookSales.put(bookId, bookSales.getOrDefault(bookId, 0) + quantity);
                }
            }
        }

        // 获取所有图书信息
        List<Book> allBooks = bookService.getAllBooks();
        Map<Long, Book> bookMap = allBooks.stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        // 构建结果
        return bookSales.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    Book book = bookMap.get(entry.getKey());
                    if (book != null) {
                        item.put("title", book.getTitle());
                        item.put("sales", entry.getValue());
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取分类销售比例（按书籍数量）
     * @return 分类销售比例数据（按书籍数量）
     */
    public List<Map<String, Object>> getCategorySales() {
        List<Order> orders = orderService.getAllOrders();

        // 只统计已支付、已发货和已完成的订单
        List<Order> validOrders = orders.stream()
                .filter(order -> order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3)
                .collect(Collectors.toList());

        // 统计每个分类的书籍数量
        Map<String, Integer> categoryQuantities = new HashMap<>();

        for (Order order : validOrders) {
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    String category = item.getBook().getCategory();
                    // 处理null分类和格式问题
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
                    // 使用书籍数量而不是销售额
                    int quantity = item.getQuantity();
                    categoryQuantities.put(category, categoryQuantities.getOrDefault(category, 0) + quantity);
                }
            }
        }

        // 如果没有数据，添加一个默认项
        if (categoryQuantities.isEmpty()) {
            categoryQuantities.put("暂无数据", 0);
        }

        // 构建结果
        return categoryQuantities.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取订单状态分布
     * @return 订单状态分布数据
     */
    public List<Map<String, Object>> getOrderStatusDistribution() {
        List<Order> orders = orderService.getAllOrders();

        // 如果没有任何订单，返回默认项
        if (orders.isEmpty()) {
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("name", "暂无数据");
            defaultItem.put("value", 1);
            return Collections.singletonList(defaultItem);
        }

        // 统计每种状态的订单数量
        Map<Integer, Integer> statusCount = new HashMap<>();

        for (Order order : orders) {
            Integer status = order.getStatus();
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        // 状态文本映射 - 只使用待支付和已完成两种状态
        Map<Integer, String> statusText = new HashMap<>();
        statusText.put(0, "待支付");
        statusText.put(1, "已完成");

        // 确保这两种状态都有数据，即使是0
        for (int i = 0; i <= 1; i++) {
            if (!statusCount.containsKey(i)) {
                statusCount.put(i, 0);
            }
        }

        // 构建结果 - 只包含待支付和已完成两种状态
        return statusCount.entrySet().stream()
                .filter(entry -> entry.getKey() == 0 || entry.getKey() == 1) // 只保留状态 0,1
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", statusText.getOrDefault(entry.getKey(), "未知状态"));
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    // 辅助方法：获取每日销售数据
    private Map<String, BigDecimal> getDailySales(List<Order> orders) {
        Map<String, BigDecimal> dailySales = new LinkedHashMap<>();

        // 初始化24小时的数据
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%d:00", i);
            dailySales.put(hour, BigDecimal.ZERO);
        }

        // 统计每小时的销售额
        for (Order order : orders) {
            LocalDateTime orderDate = order.getOrderDate();
            if (orderDate != null) {
                // 只统计今天的订单
                LocalDate today = LocalDate.now();
                if (orderDate.toLocalDate().equals(today)) {
                    int hour = orderDate.getHour();
                    String hourKey = String.format("%d:00", hour);

                    BigDecimal amount = order.getTotalAmount();
                    dailySales.put(hourKey, dailySales.get(hourKey).add(amount));
                }
            }
        }

        return dailySales;
    }

    // 辅助方法：获取每周销售数据
    private Map<String, BigDecimal> getWeeklySales(List<Order> orders) {
        Map<String, BigDecimal> weeklySales = new LinkedHashMap<>();

        // 初始化一周的数据
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        for (String day : days) {
            weeklySales.put(day, BigDecimal.ZERO);
        }

        // 获取本周的开始日期（周日）
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() % 7);

        // 统计每天的销售额
        for (Order order : orders) {
            LocalDateTime orderDate = order.getOrderDate();
            if (orderDate != null) {
                LocalDate orderDay = orderDate.toLocalDate();

                // 只统计本周的订单
                if (!orderDay.isBefore(startOfWeek) && !orderDay.isAfter(startOfWeek.plusDays(6))) {
                    int dayOfWeek = orderDay.getDayOfWeek().getValue() % 7; // 0=周日, 1=周一, ..., 6=周六
                    String dayKey = days[dayOfWeek];

                    BigDecimal amount = order.getTotalAmount();
                    weeklySales.put(dayKey, weeklySales.get(dayKey).add(amount));
                }
            }
        }

        return weeklySales;
    }

    // 辅助方法：获取每月销售数据
    private Map<String, BigDecimal> getMonthlySales(List<Order> orders) {
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();

        // 获取当前月份的天数
        LocalDate today = LocalDate.now();
        int daysInMonth = today.lengthOfMonth();

        // 初始化当月每天的数据
        for (int i = 1; i <= daysInMonth; i++) {
            String day = i + "日";
            monthlySales.put(day, BigDecimal.ZERO);
        }

        // 统计每天的销售额
        for (Order order : orders) {
            LocalDateTime orderDate = order.getOrderDate();
            if (orderDate != null) {
                LocalDate orderDay = orderDate.toLocalDate();

                // 只统计本月的订单
                if (orderDay.getYear() == today.getYear() && orderDay.getMonth() == today.getMonth()) {
                    int dayOfMonth = orderDay.getDayOfMonth();
                    String dayKey = dayOfMonth + "日";

                    BigDecimal amount = order.getTotalAmount();
                    monthlySales.put(dayKey, monthlySales.getOrDefault(dayKey, BigDecimal.ZERO).add(amount));
                }
            }
        }

        return monthlySales;
    }

    // 辅助方法：获取每年销售数据
    private Map<String, BigDecimal> getYearlySales(List<Order> orders) {
        Map<String, BigDecimal> yearlySales = new LinkedHashMap<>();

        // 初始化12个月的数据
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        for (String month : months) {
            yearlySales.put(month, BigDecimal.ZERO);
        }

        // 获取今年的年份
        int currentYear = LocalDate.now().getYear();

        // 统计每月的销售额
        for (Order order : orders) {
            LocalDateTime orderDate = order.getOrderDate();
            if (orderDate != null && orderDate.getYear() == currentYear) {
                int monthIndex = orderDate.getMonthValue() - 1; // 0-based index
                String monthKey = months[monthIndex];

                BigDecimal amount = order.getTotalAmount();
                yearlySales.put(monthKey, yearlySales.get(monthKey).add(amount));
            }
        }

        return yearlySales;
    }
}
package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.User;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.OrderItemMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        return orderMapper.findAll();
    }

    /**
     * 分页获取所有订单
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 包含分页数据和总数的Map
     */
    public Map<String, Object> getOrdersPaged(int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<Order> orders = orderMapper.findAllPaged(offset, size);
        int total = orderMapper.count();

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 获取订单详情
     */
    public Order getOrderById(Long orderId) {
        return orderMapper.findById(orderId);
    }

    /**
     * 获取用户的所有订单
     */
    public List<Order> getUserOrders(Long userId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            return orderMapper.findByUserIdOrderByOrderDateDesc(userId);
        }
        return new ArrayList<>();
    }

    /**
     * 获取用户订单数量
     */
    public int countByUserId(Long userId) {
        return orderMapper.countByUserId(userId);
    }

    /**
     * 分页获取用户订单
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 包含分页数据和总数的Map
     */
    public Map<String, Object> getUserOrdersPaged(Long userId, int page, int size) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Map.of(
                "orders", List.of(),
                "total", 0,
                "page", page,
                "size", size,
                "pages", 0
            );
        }

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<Order> orders = orderMapper.findByUserIdPaged(userId, offset, size);
        int total = orderMapper.countByUserId(userId);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 创建新订单
     */
    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(0); // 待支付状态

        // 生成订单号
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setTotalAmount(totalAmount);

        // 保存订单
        orderMapper.save(order);

        // 保存订单项
        for (OrderItem item : items) {
            item.setOrder(order);
            orderItemMapper.save(item);
        }

        // 重新获取订单（包含订单项）
        return orderMapper.findById(order.getId());
    }

    /**
     * 从图书创建订单
     */
    @Transactional
    public Order createOrderFromBook(Long userId, Long bookId, Integer quantity) {
        User user = userMapper.findById(userId);
        Book book = bookMapper.findById(bookId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(0); // 待支付状态

        // 生成订单号
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);

        // 计算订单总金额
        BigDecimal totalAmount = book.getPrice().multiply(new BigDecimal(quantity));
        order.setTotalAmount(totalAmount);

        // 保存订单
        orderMapper.save(order);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(book.getPrice());

        // 保存订单项
        orderItemMapper.save(orderItem);

        // 重新获取订单（包含订单项）
        return orderMapper.findById(order.getId());
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        order.setStatus(status);
        orderMapper.update(order);

        return orderMapper.findById(orderId);
    }

    /**
     * 取消订单（直接删除）
     */
    @Transactional
    public boolean cancelOrder(Long orderId) {
        return deleteOrder(orderId); // 直接删除订单
    }

    /**
     * 删除订单
     */
    @Transactional
    public boolean deleteOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 先删除订单项
        orderItemMapper.deleteByOrderId(orderId);

        // 再删除订单
        return orderMapper.deleteById(orderId) > 0;
    }

    /**
     * 批量删除订单
     */
    @Transactional
    public int batchDeleteOrders(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;

        for (Long orderId : orderIds) {
            try {
                // 先删除订单项
                orderItemMapper.deleteByOrderId(orderId);

                // 再删除订单
                int result = orderMapper.deleteById(orderId);
                if (result > 0) {
                    deletedCount++;
                }
            } catch (Exception e) {
                // 记录异常但继续处理其他订单
                System.err.println("删除订单 " + orderId + " 失败: " + e.getMessage());
            }
        }

        return deletedCount;
    }

    /**
     * 生成订单号
     * 格式：ORD-年月日-随机数字
     */
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 生成四位随机数
        java.util.Random random = new java.util.Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);

        return "ORD-" + dateStr + "-" + randomStr;
    }
}

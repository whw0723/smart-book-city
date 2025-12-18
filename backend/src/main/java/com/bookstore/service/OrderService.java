package com.bookstore.service;

import com.bookstore.dto.OrderDTO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.User;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.OrderItemMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.UserMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        // 检查库存并减少库存
        for (OrderItem item : items) {
            Book book = item.getBook();
            if (book.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足：" + book.getTitle());
            }
            // 减少库存
            book.setStock(book.getStock() - item.getQuantity());
            bookMapper.update(book);
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

        // 检查库存并减少库存
        if (book.getStock() < quantity) {
            throw new RuntimeException("库存不足：" + book.getTitle());
        }
        // 减少库存
        book.setStock(book.getStock() - quantity);
        bookMapper.update(book);

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
    public Order updateOrderStatusById(Long orderId, Integer status) {
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
     * 批量取消订单
     */
    @Transactional
    public int batchCancelOrders(List<Long> orderIds) {
        // 复用批量删除的逻辑，但不真正删除记录，而是修改状态
        int cancelledCount = 0;
        
        for (Long orderId : orderIds) {
            Order order = orderMapper.findById(orderId);
            if (order != null && order.getStatus() != 3) { // 不是已取消状态
                order.setStatus(3); // 设置为已取消
                order.setOrderDate(LocalDateTime.now());
                orderMapper.update(order);
                cancelledCount++;
            }
        }
        return cancelledCount;
    }

    // 获取用户订单列表并转换为DTO
    public List<OrderDTO> getUserOrdersWithDTO(Long userId) {
        List<Order> orders = orderMapper.findByUserIdOrderByOrderDateDesc(userId);
        return orders.stream()
            .map(OrderDTO::new)
            .collect(Collectors.toList());
    }
    
    // 从book创建订单（处理参数解析逻辑）
    @Transactional
    public Order createOrderFromBookWithPayload(Map<String, Object> payload) {
        Long userId = Long.parseLong(payload.get("userId").toString());
        Long bookId = Long.parseLong(payload.get("bookId").toString());
        Integer quantity = Integer.parseInt(payload.get("quantity").toString());
        
        return createOrderFromBook(userId, bookId, quantity);
    }

    /**
     * 从payload创建订单
     */
    @Transactional
    public Order createOrderFromPayload(Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        List<OrderItem> orderItems = new ArrayList<>();
        
        // 解析订单项
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        for (Map<String, Object> item : items) {
            OrderItem orderItem = new OrderItem();
            Long bookId = Long.valueOf(item.get("bookId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            // 查询书籍信息
            Book book = bookMapper.findById(bookId);
            if (book == null) {
                throw new RuntimeException("书籍不存在: " + bookId);
            }
            
            orderItem.setBook(book);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(book.getPrice());
            orderItems.add(orderItem);
        }
        
        return createOrder(userId, orderItems);
    }

    /**
     * 分页获取用户订单并转换为DTO
     */
    public Map<String, Object> getUserOrdersPagedWithDTO(Long userId, int page, int size) {
        Map<String, Object> response = getUserOrdersPaged(userId, page, size);
        
        // 将订单转换为DTO
        if (response.containsKey("orders")) {
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) response.get("orders");
            List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
            response.put("orders", orderDTOs);
        }
        
        return response;
    }

    /**
     * 更新订单状态，处理特殊状态
     */
    @Transactional
    public Order updateOrderStatus(Long id, Integer status) {
        // 如果状态是取消(2)，则直接删除订单
        if (status == 2) {
            cancelOrder(id);
            return null; // 返回null表示订单已取消
        }
        
        // 其他状态正常更新
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        order.setStatus(status);
        orderMapper.update(order);
        
        return orderMapper.findById(id);
    }
    
    /**
     * 查找并取消所有超过5分钟未支付的订单
     */
    @Transactional
    public void cancelAllOverdueOrders() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 计算5分钟前的时间
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        
        // 查询所有待支付订单
        List<Order> allOrders = orderMapper.findAll();
        
        // 过滤出超过5分钟的订单
        List<Order> overdueOrders = allOrders.stream()
            .filter(order -> order.getStatus() == 0 && order.getOrderDate().isBefore(fiveMinutesAgo))
            .collect(Collectors.toList());
        
        // 取消所有过期订单
        for (Order order : overdueOrders) {
            try {
                // 获取订单的订单项
                List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
                
                // 恢复库存
                for (OrderItem item : orderItems) {
                    Book book = bookMapper.findById(item.getBook().getId());
                    if (book != null) {
                        // 恢复库存
                        book.setStock(book.getStock() + item.getQuantity());
                        bookMapper.update(book);
                    }
                }
                
                // 删除订单项
                orderItemMapper.deleteByOrderId(order.getId());
                
                // 删除订单
                orderMapper.deleteById(order.getId());
                
                System.out.println("自动取消超时订单：" + order.getOrderNumber());
            } catch (Exception e) {
                System.err.println("取消订单失败：" + order.getOrderNumber() + "，错误：" + e.getMessage());
            }
        }
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

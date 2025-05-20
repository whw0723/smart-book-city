package com.bookstore.controller;

import com.bookstore.dto.OrderDTO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.service.BookService;
import com.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getOrdersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = orderService.getOrdersPaged(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            OrderDTO orderDTO = new OrderDTO(order);
            return ResponseEntity.ok(orderDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        if (!orders.isEmpty()) {
            // 将订单转换为 DTO
            List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        }
        return ResponseEntity.ok(List.of()); // 返回空列表而不是 404
    }

    @GetMapping("/user/{userId}/paged")
    @Transactional
    public ResponseEntity<Map<String, Object>> getOrdersByUserPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        Map<String, Object> response = orderService.getUserOrdersPaged(userId, page, size);

        // 将订单转换为DTO
        if (response.containsKey("orders")) {
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) response.get("orders");
            List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
            response.put("orders", orderDTOs);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());

            // 获取 items 数组并正确转换
            List<OrderItem> items = new ArrayList<>();
            if (payload.containsKey("items") && payload.get("items") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawItems = (List<Map<String, Object>>) payload.get("items");

                for (Map<String, Object> rawItem : rawItems) {
                    OrderItem item = new OrderItem();

                    // 设置图书信息
                    if (rawItem.containsKey("bookId")) {
                        Long bookId = Long.valueOf(rawItem.get("bookId").toString());
                        Book book = bookService.getBookById(bookId);
                        item.setBook(book);
                    }

                    // 设置数量
                    if (rawItem.containsKey("quantity")) {
                        item.setQuantity(Integer.valueOf(rawItem.get("quantity").toString()));
                    }

                    // 设置价格
                    if (rawItem.containsKey("price")) {
                        item.setPrice(new BigDecimal(rawItem.get("price").toString()));
                    }

                    items.add(item);
                }
            }

            Order savedOrder = orderService.createOrder(userId, items);
            OrderDTO orderDTO = new OrderDTO(savedOrder);
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("创建订单失败: " + e.getMessage());
        }
    }

    @PostMapping("/book")
    @Transactional
    public ResponseEntity<?> createOrderFromBook(@RequestBody Map<String, Object> payload) {
        try {
            // 获取参数
            Long userId = Long.parseLong(payload.get("userId").toString());
            Long bookId = Long.parseLong(payload.get("bookId").toString());
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());

            // 使用服务层创建订单
            Order savedOrder = orderService.createOrderFromBook(userId, bookId, quantity);

            // 转换为 DTO 返回
            OrderDTO orderDTO = new OrderDTO(savedOrder);
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            // 打印详细的异常信息
            e.printStackTrace();
            System.out.println("创建订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body("创建订单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            // 如果状态是取消(2)，则直接删除订单
            if (status == 2) {
                return cancelOrder(id);
            }

            Order updatedOrder = orderService.updateOrderStatus(id, status);
            OrderDTO orderDTO = new OrderDTO(updatedOrder);
            return ResponseEntity.ok(orderDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            boolean cancelled = orderService.cancelOrder(id);
            if (cancelled) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("取消订单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            boolean deleted = orderService.deleteOrder(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("删除订单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Transactional
    public ResponseEntity<?> batchDeleteOrders(@RequestBody List<Long> orderIds) {
        try {
            int deletedCount = orderService.batchDeleteOrders(orderIds);
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "deletedCount", deletedCount,
                "message", "成功删除 " + deletedCount + " 个订单"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除订单失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/batch/cancel")
    @Transactional
    public ResponseEntity<?> batchCancelOrders(@RequestBody List<Long> orderIds) {
        try {
            int cancelledCount = 0;
            for (Long orderId : orderIds) {
                if (orderService.cancelOrder(orderId)) {
                    cancelledCount++;
                }
            }
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "cancelledCount", cancelledCount,
                "message", "成功取消 " + cancelledCount + " 个订单"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量取消订单失败: " + e.getMessage()
            ));
        }
    }
}


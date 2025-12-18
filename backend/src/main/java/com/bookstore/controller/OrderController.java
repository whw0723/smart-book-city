package com.bookstore.controller;

import com.bookstore.dto.OrderDTO;
import com.bookstore.entity.Order;
import com.bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

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
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        OrderDTO orderDTO = new OrderDTO(order);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        // 直接调用service层方法，由service负责数据转换
        List<OrderDTO> orderDTOs = orderService.getUserOrdersWithDTO(userId);
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Map<String, Object>> getOrdersByUserPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        // 调用service层方法，由service负责数据转换
        Map<String, Object> response = orderService.getUserOrdersPagedWithDTO(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody Map<String, Object> payload) {
        // 直接将整个payload传递给service层处理
        Order savedOrder = orderService.createOrderFromPayload(payload);
        OrderDTO orderDTO = new OrderDTO(savedOrder);
        return ResponseEntity.ok(orderDTO);
    }

    @PostMapping("/book")
    public ResponseEntity<OrderDTO> createOrderFromBook(@RequestBody Map<String, Object> payload) {
        // 直接将整个payload传递给service层处理
        Order savedOrder = orderService.createOrderFromBookWithPayload(payload);
        OrderDTO orderDTO = new OrderDTO(savedOrder);
        return ResponseEntity.ok(orderDTO);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 直接调用service层方法，由service处理状态转换逻辑
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        // 如果返回null表示订单已取消
        if (updatedOrder == null) {
            return ResponseEntity.ok().build();
        }
        OrderDTO orderDTO = new OrderDTO(updatedOrder);
        return ResponseEntity.ok(orderDTO);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteOrders(@RequestBody List<Long> orderIds) {
        int deletedCount = orderService.batchDeleteOrders(orderIds);
        return ResponseEntity.ok().body(Map.of(
            "success", true,
            "deletedCount", deletedCount,
            "message", "成功删除 " + deletedCount + " 个订单"
        ));
    }

    @PutMapping("/batch/cancel")
    public ResponseEntity<Map<String, Object>> batchCancelOrders(@RequestBody List<Long> orderIds) {
        // 调用service层的批量取消订单方法
        int cancelledCount = orderService.batchCancelOrders(orderIds);
        return ResponseEntity.ok().body(Map.of(
            "success", true,
            "cancelledCount", cancelledCount,
            "message", "成功取消 " + cancelledCount + " 个订单"
        ));
    }
    
    @PostMapping("/check-overdue")
    public ResponseEntity<Map<String, Object>> checkOverdueOrders() {
        // 调用service层的检查并取消过期订单方法
        orderService.cancelAllOverdueOrders();
        return ResponseEntity.ok().body(Map.of(
            "success", true,
            "message", "成功检查并取消过期订单"
        ));
    }
}


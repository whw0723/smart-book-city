package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    private String orderNumber;

    private User user;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    // 订单状态: 0=待支付, 1=已完成
    private Integer status;

    @JsonManagedReference
    private List<OrderItem> orderItems;
}
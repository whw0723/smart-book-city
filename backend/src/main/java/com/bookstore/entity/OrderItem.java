package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Long id;

    @JsonBackReference
    private Order order;

    private Book book;

    private Integer quantity;

    private BigDecimal price;
}
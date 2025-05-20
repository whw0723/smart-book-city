package com.bookstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private Long id;

    private String title;

    private String author;

    private BigDecimal price;

    private Integer stock;

    private String category;

    private String description;
}
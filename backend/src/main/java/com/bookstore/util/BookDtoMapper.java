package com.bookstore.util;

import com.bookstore.dto.BookDTO;
import com.bookstore.entity.Book;
import org.springframework.stereotype.Component;

/**
 * Book实体与DTO之间的转换工具
 */
@Component
public class BookDtoMapper {

    /**
     * 将DTO转换为实体
     */
    public Book toEntity(BookDTO dto) {
        if (dto == null) {
            return null;
        }

        Book book = new Book();
        book.setId(dto.getId());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPrice(dto.getPrice());
        book.setStock(dto.getStock());
        
        // 处理分类信息，确保格式正确
        if (dto.getCategory() != null) {
            book.setCategory(dto.getCategory().trim());
        }
        
        book.setDescription(dto.getDescription());
        
        return book;
    }
    
    /**
     * 将实体转换为DTO
     */
    public BookDTO toDto(Book entity) {
        if (entity == null) {
            return null;
        }
        
        BookDTO dto = new BookDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAuthor(entity.getAuthor());
        dto.setPrice(entity.getPrice());
        dto.setStock(entity.getStock());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        
        return dto;
    }
    
    /**
     * 将请求DTO数据更新到已有实体中
     */
    public void updateEntityFromDto(BookDTO dto, Book entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        entity.setTitle(dto.getTitle());
        entity.setAuthor(dto.getAuthor());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        
        // 处理分类信息，确保格式正确
        if (dto.getCategory() != null) {
            entity.setCategory(dto.getCategory().trim());
        }
        
        entity.setDescription(dto.getDescription());
    }
} 
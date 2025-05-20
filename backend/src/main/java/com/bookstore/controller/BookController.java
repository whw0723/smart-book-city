package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.dto.BookDTO;
import com.bookstore.entity.Book;
import com.bookstore.exception.BusinessException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.BookService;
import com.bookstore.util.BookDtoMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书管理控制器
 */
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Validated
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BookDtoMapper bookDtoMapper;
    
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * 获取所有图书
     */
    @GetMapping
    public ApiResponse<List<BookDTO>> getAllBooks() {
        logger.info("获取所有图书");
        List<Book> books = bookService.getAllBooks();
        List<BookDTO> bookDTOs = books.stream()
                .map(bookDtoMapper::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(bookDTOs);
    }

    /**
     * 分页获取图书
     */
    @GetMapping("/paged")
    public ApiResponse<Map<String, Object>> getBooksPaged(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        logger.info("分页获取图书, page={}, size={}", page, size);
        Map<String, Object> response = bookService.getBooksPaged(page, size);
        return ApiResponse.success(response);
    }

    /**
     * 根据ID获取图书
     */
    @GetMapping("/{id}")
    public ApiResponse<BookDTO> getBookById(@PathVariable Long id) {
        logger.info("获取图书详情, id={}", id);
        Book book = bookService.getBookById(id);
        if (book == null) {
            throw new ResourceNotFoundException("图书", "ID", id);
        }
        return ApiResponse.success(bookDtoMapper.toDto(book));
    }

    /**
     * 搜索图书
     */
    @GetMapping("/search")
    public ApiResponse<List<BookDTO>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        logger.info("搜索图书, title={}, author={}", title, author);
        
        List<Book> result;
        // 如果两个参数都提供，在服务层实现"与"逻辑搜索
        if (title != null && !title.isEmpty() && author != null && !author.isEmpty()) {
            // 修改为在服务层实现复合搜索
            result = bookService.searchBooksByTitleAndAuthor(title, author);
        }
        // 如果只提供了标题
        else if (title != null && !title.isEmpty()) {
            result = bookService.searchBooksByTitle(title);
        }
        // 如果只提供了作者
        else if (author != null && !author.isEmpty()) {
            result = bookService.searchBooksByAuthor(author);
        }
        // 如果没有提供任何参数，返回空列表
        else {
            result = List.of();
        }
        
        List<BookDTO> bookDTOs = result.stream()
                .map(bookDtoMapper::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(bookDTOs);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        logger.info("获取所有图书分类");
        List<String> categories = bookService.getAllCategories();
        return ApiResponse.success(categories);
    }

    /**
     * 根据分类获取图书
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<BookDTO>> getBooksByCategory(@PathVariable String category) {
        logger.info("获取分类下的图书, category={}", category);
        List<Book> books = bookService.getBooksByCategory(category);
        List<BookDTO> bookDTOs = books.stream()
                .map(bookDtoMapper::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(bookDTOs);
    }

    /**
     * 分页获取分类下的图书
     */
    @GetMapping("/category/{category}/paged")
    public ApiResponse<Map<String, Object>> getBooksByCategoryPaged(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        logger.info("分页获取分类下的图书, category={}, page={}, size={}", category, page, size);
        Map<String, Object> response = bookService.getBooksByCategoryPaged(category, page, size);
        return ApiResponse.success(response);
    }

    /**
     * 创建图书
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        logger.info("创建图书: {}", bookDTO.getTitle());
        Book book = bookDtoMapper.toEntity(bookDTO);
        Book savedBook = bookService.saveBook(book);
        return ApiResponse.success("图书创建成功", bookDtoMapper.toDto(savedBook));
    }

    /**
     * 更新图书
     */
    @PutMapping("/{id}")
    public ApiResponse<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        logger.info("更新图书, id={}", id);
        Book existingBook = bookService.getBookById(id);
        if (existingBook == null) {
            throw new ResourceNotFoundException("图书", "ID", id);
        }
        
        bookDtoMapper.updateEntityFromDto(bookDTO, existingBook);
        Book updatedBook = bookService.updateBook(existingBook);
        return ApiResponse.success("图书更新成功", bookDtoMapper.toDto(updatedBook));
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        logger.info("删除图书, id={}", id);
        Book existingBook = bookService.getBookById(id);
        if (existingBook == null) {
            throw new ResourceNotFoundException("图书", "ID", id);
        }
        
        bookService.deleteBook(id);
        return ApiResponse.success("图书删除成功", null);
    }

    /**
     * 批量删除图书
     */
    @PostMapping("/batch-delete")
    @Transactional
    public ApiResponse<Map<String, Object>> batchDeleteBooks(@RequestBody Map<String, List<Long>> payload) {
        List<Long> bookIds = payload.get("bookIds");
        if (bookIds == null || bookIds.isEmpty()) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "未提供要删除的图书ID");
        }
        
        logger.info("批量删除图书, bookIds={}", bookIds);
        
        try {
            // 调用业务层处理批量删除逻辑
            Map<String, Object> result = bookService.batchDeleteBooksWithResult(bookIds);
            return ApiResponse.success(result);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode() != null ? e.getCode() : HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
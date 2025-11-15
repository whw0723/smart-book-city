package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.dto.BookDTO;
import com.bookstore.entity.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.BookDtoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
        
        List<Book> result = bookService.searchBooksByTitleAndAuthor(title, author);
        
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
        
        Book book = bookDtoMapper.toEntity(bookDTO);
        book.setId(id);
        Book updatedBook = bookService.updateBook(book);
        return ApiResponse.success("图书更新成功", bookDtoMapper.toDto(updatedBook));
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        logger.info("删除图书, id={}", id);
        bookService.deleteBook(id);
        return ApiResponse.success("图书删除成功", null);
    }

    /**
     * 批量删除图书
     */
    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Object>> batchDeleteBooks(@RequestBody Map<String, List<Integer>> payload) {
        List<Integer> bookIdInts = payload.get("bookIds");
        
        // 将 Integer 类型的 ID 转换为 Long 类型
        List<Long> bookIds = bookIdInts.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        
        logger.info("批量删除图书, bookIds={}", bookIds);
        
        Map<String, Object> result = bookService.batchDeleteBooksWithResult(bookIds);
        return ApiResponse.success(result);
    }
}
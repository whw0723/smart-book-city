package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.exception.BusinessException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.OrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 获取所有图书
     */
    public List<Book> getAllBooks() {
        return bookMapper.findAll();
    }

    /**
     * 分页获取所有图书
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 包含分页数据和总数的Map
     */
    public Map<String, Object> getBooksPaged(int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<Book> books = bookMapper.findAllPaged(offset, size);
        int total = bookMapper.count();

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查找图书
     */
    public Book getBookById(Long id) {
        return bookMapper.findById(id);
    }

    /**
     * 根据标题模糊查询
     */
    public List<Book> searchBooksByTitle(String title) {
        return bookMapper.findByTitleContaining(title);
    }

    /**
     * 根据作者模糊查询
     */
    public List<Book> searchBooksByAuthor(String author) {
        return bookMapper.findByAuthorContaining(author);
    }
    
    /**
     * 根据标题和作者组合查询
     */
    public List<Book> searchBooksByTitleAndAuthor(String title, String author) {
        List<Book> titleResults = bookMapper.findByTitleContaining(title);
        // 从标题结果中筛选出作者匹配的图书
        return titleResults.stream()
            .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有图书分类
     */
    public List<String> getAllCategories() {
        return bookMapper.findAllCategories();
    }

    /**
     * 根据分类查询
     */
    public List<Book> getBooksByCategory(String category) {
        return bookMapper.findByCategory(category);
    }

    /**
     * 分页获取分类图书
     * @param category 分类
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 包含分页数据和总数的Map
     */
    public Map<String, Object> getBooksByCategoryPaged(String category, int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<Book> books = bookMapper.findByCategoryPaged(category, offset, size);
        int total = bookMapper.countByCategory(category);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 保存图书
     */
    public Book saveBook(Book book) {
        bookMapper.save(book);
        return book;
    }

    /**
     * 更新图书
     */
    public Book updateBook(Book book) {
        bookMapper.update(book);
        return book;
    }

    /**
     * 检查图书是否被订单项引用
     */
    public boolean isBookReferenced(Long id) {
        return orderItemMapper.countByBookId(id) > 0;
    }

    /**
     * 删除图书
     */
    public boolean deleteBook(Long id) {
        // 检查图书是否被订单项引用
        if (isBookReferenced(id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "该图书已被订单引用，无法删除");
        }
        return bookMapper.deleteById(id) > 0;
    }

    /**
     * 批量删除图书
     */
    @Transactional
    public int batchDeleteBooks(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return 0;
        }

        // 检查每本图书是否被引用
        List<Long> safeToDeleteIds = new ArrayList<>();
        List<String> referencedBooks = new ArrayList<>();

        for (Long bookId : bookIds) {
            if (isBookReferenced(bookId)) {
                Book book = getBookById(bookId);
                if (book != null) {
                    referencedBooks.add(book.getTitle());
                }
            } else {
                safeToDeleteIds.add(bookId);
            }
        }

        // 如果有图书被引用，抛出异常
        if (!referencedBooks.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "以下图书已被订单引用，无法删除: " + String.join(", ", referencedBooks));
        }

        // 执行批量删除
        if (!safeToDeleteIds.isEmpty()) {
            return bookMapper.batchDeleteByIds(safeToDeleteIds);
        }

        return 0;
    }
    
    /**
     * 批量删除图书并返回结果信息
     * @param bookIds 要删除的图书ID列表
     * @return 包含删除结果信息的Map
     */
    @Transactional
    public Map<String, Object> batchDeleteBooksWithResult(List<Long> bookIds) {
        // 参数校验
        if (bookIds == null || bookIds.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "未提供要删除的图书ID");
        }
        
        // 调用现有的批量删除方法
        int deletedCount = batchDeleteBooks(bookIds);
        
        // 构建并返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", deletedCount);
        result.put("message", "成功删除 " + deletedCount + " 本图书");
        
        return result;
    }
}

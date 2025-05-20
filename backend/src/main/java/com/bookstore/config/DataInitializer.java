package com.bookstore.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Component
public class DataInitializer implements CommandLineRunner {

    // @Autowired
    // private UserMapper userMapper;

    // @Autowired
    // private AdminMapper adminMapper;

    // @Autowired
    // private BookMapper bookMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 数据初始化逻辑已移至 data.sql 文件
        // Spring Boot 会在启动时自动执行 src/main/resources/data.sql 文件中的SQL语句
        // (前提是配置了 spring.datasource.initialization-mode=always 或 embedded)
        // 如果需要通过 CommandLineRunner 执行SQL脚本，可以使用 JdbcTemplate 或其他方式
        System.out.println("DataInitializer: Data initialization is now handled by data.sql.");
    }

    // createBook 方法已不再需要，因为数据通过 data.sql 插入
    /*
    private void createBook(String title, String author, BigDecimal price, int stock, String category, String description) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setStock(stock);
        book.setCategory(category);
        book.setDescription(description);
        bookMapper.save(book);
    }
    */
}
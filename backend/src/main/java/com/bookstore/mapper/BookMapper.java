package com.bookstore.mapper;

import com.bookstore.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {

    /**
     * 获取所有图书
     */
    List<Book> findAll();

    /**
     * 分页查询所有图书
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 图书列表
     */
    List<Book> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取图书总数
     * @return 图书总数
     */
    int count();

    /**
     * 根据ID查找图书
     */
    Book findById(Long id);

    /**
     * 根据标题模糊查询
     */
    List<Book> findByTitleContaining(String title);

    /**
     * 根据作者模糊查询
     */
    List<Book> findByAuthorContaining(String author);

    /**
     * 获取所有图书分类
     */
    List<String> findAllCategories();

    /**
     * 根据分类查询
     */
    List<Book> findByCategory(String category);

    /**
     * 分页查询分类图书
     * @param category 分类
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 图书列表
     */
    List<Book> findByCategoryPaged(@Param("category") String category, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取指定分类的图书总数
     * @param category 分类
     * @return 图书总数
     */
    int countByCategory(@Param("category") String category);

    /**
     * 保存图书
     */
    int save(Book book);

    /**
     * 更新图书
     */
    int update(Book book);

    /**
     * 删除图书
     */
    int deleteById(Long id);

    /**
     * 批量删除图书
     */
    int batchDeleteByIds(@Param("bookIds") List<Long> bookIds);
}

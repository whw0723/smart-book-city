package com.bookstore.mapper;

import com.bookstore.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 获取所有用户
     */
    List<User> findAll();

    /**
     * 分页查询所有用户
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 用户列表
     */
    List<User> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取用户总数
     * @return 用户总数
     */
    int count();

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    int existsByUsername(String username);

    /**
     * 保存用户
     */
    int save(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 删除用户
     */
    int deleteById(Long id);

    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 变动金额（正数为增加，负数为减少）
     * @return 影响的行数
     */
    int updateBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}

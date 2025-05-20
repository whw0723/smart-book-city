package com.bookstore.mapper;

import com.bookstore.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 获取所有订单
     */
    List<Order> findAll();

    /**
     * 分页查询所有订单
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     */
    List<Order> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取订单总数
     * @return 订单总数
     */
    int count();

    /**
     * 根据ID查找订单
     */
    Order findById(Long id);

    /**
     * 根据用户ID查找订单
     */
    List<Order> findByUserId(Long userId);

    /**
     * 根据用户ID查找订单并按下单时间降序排序
     */
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    /**
     * 根据用户ID分页查询订单并按下单时间降序排序
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 订单列表
     */
    List<Order> findByUserIdPaged(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取用户订单总数
     * @param userId 用户ID
     * @return 订单总数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 保存订单
     */
    int save(Order order);

    /**
     * 更新订单
     */
    int update(Order order);

    /**
     * 删除订单
     */
    int deleteById(Long id);
}

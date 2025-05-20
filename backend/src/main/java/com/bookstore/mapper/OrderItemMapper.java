package com.bookstore.mapper;

import com.bookstore.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    /**
     * 获取所有订单项
     */
    List<OrderItem> findAll();

    /**
     * 根据ID查找订单项
     */
    OrderItem findById(Long id);

    /**
     * 根据订单ID查找订单项
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根据图书ID查找订单项
     */
    List<OrderItem> findByBookId(Long bookId);

    /**
     * 检查图书是否被订单项引用
     */
    int countByBookId(Long bookId);

    /**
     * 保存订单项
     */
    int save(OrderItem orderItem);

    /**
     * 批量保存订单项
     */
    int batchSave(List<OrderItem> orderItems);

    /**
     * 更新订单项
     */
    int update(OrderItem orderItem);

    /**
     * 删除订单项
     */
    int deleteById(Long id);

    /**
     * 根据订单ID删除订单项
     */
    int deleteByOrderId(Long orderId);
}

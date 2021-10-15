package com.solvay.order.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.solvay.order.entity.Order;
import org.apache.ibatis.annotations.Param;

/**
 * (Order)表数据库访问层
 *
 * @author makejava
 * @since 2021-10-13 19:52:01
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 创建订单
     * @param order
     */
    void createOrder(@Param("order") Order order);
}

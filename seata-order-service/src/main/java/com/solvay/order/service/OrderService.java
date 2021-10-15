package com.solvay.order.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.solvay.common.api.R;
import com.solvay.common.dto.OrderDTO;
import com.solvay.order.entity.Order;

/**
 * (Order)表服务接口
 *
 * @author makejava
 * @since 2021-10-13 19:52:01
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    R<OrderDTO> createOrder(OrderDTO orderDTO);
}

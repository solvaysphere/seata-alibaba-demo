package com.solvay.order.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.solvay.common.api.R;
import com.solvay.common.dto.AccountDTO;
import com.solvay.common.dto.OrderDTO;
import com.solvay.common.enums.ApiErrorCode;
import com.solvay.order.feign.AccountFeignClient;
import com.solvay.order.mapper.OrderMapper;
import com.solvay.order.entity.Order;
import com.solvay.order.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * (Order)表服务实现类
 *
 * @author makejava
 * @since 2021-10-13 19:52:01
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private AccountFeignClient accountFeignClient;
    /**
     * 创建订单
     * @param orderDTO 订单对象
     * @return
     */
    @Override
    public R<OrderDTO> createOrder(OrderDTO orderDTO) {
        //扣减用户账户
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(orderDTO.getUserId());
        accountDTO.setAmount(orderDTO.getOrderAmount());
        R accountResponse = accountFeignClient.decreaseAccount(accountDTO);
        //生成订单号
        orderDTO.setOrderNo(UUID.randomUUID().toString().replace("-",""));
        //生成订单
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO,order);
        order.setCount(orderDTO.getOrderCount());
        order.setAmount(orderDTO.getOrderAmount().doubleValue());
        try {
            baseMapper.createOrder(order);
        } catch (Exception e) {
            return R.failed(ApiErrorCode.FAILED);
        }

        if (accountResponse.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            return R.failed(ApiErrorCode.FAILED);
        }
        return R.ok(orderDTO);
    }
}

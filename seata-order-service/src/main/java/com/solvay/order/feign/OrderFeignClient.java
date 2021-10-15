package com.solvay.order.feign;

import com.solvay.common.api.R;
import com.solvay.common.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seata-order-service")
public interface OrderFeignClient {

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/order/create_order")
    R<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO);
}

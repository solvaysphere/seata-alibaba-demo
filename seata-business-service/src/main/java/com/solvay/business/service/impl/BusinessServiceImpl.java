package com.solvay.business.service.impl;

import com.solvay.business.feign.OrderFeignClient;
import com.solvay.business.feign.StorageFeignClient;
import com.solvay.business.service.BusinessService;
import com.solvay.common.api.R;
import com.solvay.common.dto.BusinessDTO;
import com.solvay.common.dto.CommodityDTO;
import com.solvay.common.dto.OrderDTO;
import com.solvay.common.enums.ApiErrorCode;
import com.solvay.common.exceptions.ApiException;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("businessService")
@Slf4j
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private StorageFeignClient storageFeignClient;

    boolean flag;

    /**
     * 处理业务逻辑 正常的业务逻辑
     * @param businessDTO
     * @return
     */
    @GlobalTransactional(timeoutMills = 300000, name = "feign-gts-seata-demo")
    @Override
    public R handleBusiness(BusinessDTO businessDTO) {
        log.info("开始全局事务，XID = " + RootContext.getXID());
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setCount(businessDTO.getCount());
        R storageResponse = storageFeignClient.decreaseStorage(commodityDTO);
        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderCount(businessDTO.getCount());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        R<OrderDTO> response = orderFeignClient.createOrder(orderDTO);

        if (storageResponse.getCode() != ApiErrorCode.SUCCESS.getCode() || response.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            throw new ApiException(ApiErrorCode.FAILED);
        }
        return R.ok(response.getData());
    }

    /**
     * 处理业务服务，出现异常回顾
     * @param businessDTO
     * @return
     */
    @GlobalTransactional(timeoutMills = 300000, name = "feign-gts-seata-demo")
    @Override
    public R handleBusiness2(BusinessDTO businessDTO) {
        log.info("开始全局事务，XID = " + RootContext.getXID());
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setCount(businessDTO.getCount());
        R storageResponse = storageFeignClient.decreaseStorage(commodityDTO);
        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderCount(businessDTO.getCount());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        R<OrderDTO> response = orderFeignClient.createOrder(orderDTO);

//        打开注释测试事务发生异常后，全局回滚功能
        if (!flag) {
            throw new RuntimeException("测试抛异常后，分布式事务回滚！");
        }

        if (storageResponse.getCode() != ApiErrorCode.SUCCESS.getCode() || response.getCode() != ApiErrorCode.SUCCESS.getCode()) {
            throw new ApiException(ApiErrorCode.FAILED);
        }
        return R.ok(response.getData());
    }
}

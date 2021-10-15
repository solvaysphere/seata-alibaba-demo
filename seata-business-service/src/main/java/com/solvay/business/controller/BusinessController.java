package com.solvay.business.controller;

import com.solvay.business.service.BusinessService;
import com.solvay.common.api.R;
import com.solvay.common.dto.BusinessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 模拟用户购买商品下单业务逻辑流程
     * @Param:
     * @Return:
     */
    @PostMapping("/buy")
    public R handleBusiness(@RequestBody BusinessDTO businessDTO){
        log.info("请求参数：{}",businessDTO.toString());
        return businessService.handleBusiness(businessDTO);
    }

    /**
     * 模拟用户购买商品下单业务逻辑流程
     * @Param:
     * @Return:
     */
    @PostMapping("/buy2")
    public R handleBusiness2(@RequestBody BusinessDTO businessDTO){
        log.info("请求参数：{}",businessDTO.toString());
        return businessService.handleBusiness2(businessDTO);
    }
}

package com.solvay.order.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.solvay.common.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.solvay.common.dto.OrderDTO;
import com.solvay.order.entity.Order;
import com.solvay.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * (Order)表控制层
 *
 * @author makejava
 * @since 2021-10-13 19:52:01
 */
@RestController
@RequestMapping("order")
@Slf4j
public class OrderController {
    /**
     * 服务对象
     */
    @Autowired
    private OrderService orderService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param order 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<Order> page, Order order) {
        return R.ok(this.orderService.page(page, new QueryWrapper<>(order)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return R.ok(this.orderService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param order 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody Order order) {
        return R.ok(this.orderService.save(order));
    }

    /**
     * 修改数据
     *
     * @param order 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody Order order) {
        return R.ok(this.orderService.updateById(order));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return R.ok(this.orderService.removeByIds(idList));
    }

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/create_order")
    public R<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO){
        log.info("请求订单微服务：{}",orderDTO.toString());
        return orderService.createOrder(orderDTO);
    }
}

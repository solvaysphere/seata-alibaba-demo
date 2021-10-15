package com.solvay.storage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.solvay.common.api.R;
import com.solvay.common.dto.CommodityDTO;
import com.solvay.common.enums.ApiErrorCode;
import com.solvay.storage.entity.Storage;
import com.solvay.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * (Storage)表控制层
 *
 * @author makejava
 * @since 2021-10-13 19:12:36
 */
@RestController
@RequestMapping("storage")
@Slf4j
public class StorageController {
    /**
     * 服务对象
     */
    @Autowired
    private StorageService storageService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param storage 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<Storage> page, Storage storage) {
        return R.ok(this.storageService.page(page, new QueryWrapper<>(storage)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return R.ok(this.storageService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param storage 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody Storage storage) {
        return R.ok(this.storageService.save(storage));
    }

    /**
     * 修改数据
     *
     * @param storage 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody Storage storage) {
        return R.ok(this.storageService.updateById(storage));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return R.ok(this.storageService.removeByIds(idList));
    }


    /**
     * 扣减库存
     */
    @PostMapping("/dec_storage")
    public R decreaseStorage(@RequestBody CommodityDTO commodityDTO){
        log.info("请求库存微服务：{}",commodityDTO.toString());
        boolean ret = storageService.decreaseStorage(commodityDTO);
        if (ret){
            return R.ok(ret);
        }
        return R.failed(ApiErrorCode.FAILED);
    }
}

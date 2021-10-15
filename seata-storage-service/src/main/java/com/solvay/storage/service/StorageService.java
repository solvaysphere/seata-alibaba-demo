package com.solvay.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.solvay.common.dto.CommodityDTO;
import com.solvay.storage.entity.Storage;

/**
 * (Storage)表服务接口
 *
 * @author makejava
 * @since 2021-10-13 19:12:36
 */
public interface StorageService extends IService<Storage> {

    /**
     * 扣减库存
     */
    boolean decreaseStorage(CommodityDTO commodityDTO);
}

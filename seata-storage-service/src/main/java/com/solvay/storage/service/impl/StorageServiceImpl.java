package com.solvay.storage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.solvay.common.dto.CommodityDTO;
import com.solvay.storage.mapper.StorageMapper;
import com.solvay.storage.entity.Storage;
import com.solvay.storage.service.StorageService;
import org.springframework.stereotype.Service;

/**
 * (Storage)表服务实现类
 *
 * @author makejava
 * @since 2021-10-13 19:12:36
 */
@Service("storageService")
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {

    @Override
    public boolean decreaseStorage(CommodityDTO commodityDTO) {
        int storage = baseMapper.decreaseStorage(commodityDTO.getCommodityCode(), commodityDTO.getCount());
        return SqlHelper.retBool(storage);
    }
}

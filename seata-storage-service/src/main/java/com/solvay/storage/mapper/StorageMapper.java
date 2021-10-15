package com.solvay.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.solvay.storage.entity.Storage;
import org.apache.ibatis.annotations.Param;

/**
 * (Storage)表数据库访问层
 *
 * @author makejava
 * @since 2021-10-13 19:12:36
 */
public interface StorageMapper extends BaseMapper<Storage> {

    /**
     * 扣减商品库存
     * @Param: commodityCode 商品code  count扣减数量
     * @Return:
     */
    int decreaseStorage(@Param("commodityCode") String commodityCode, @Param("count") Integer count);
}

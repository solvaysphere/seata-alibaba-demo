package com.solvay.storage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * (Storage)表实体类
 *
 * @author makejava
 * @since 2021-10-13 20:17:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_storage")
public class Storage extends Model<Storage> {
    private static final long serialVersionUID = 219362408331554636L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String commodityCode;
    private String name;
    private Integer count;


    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

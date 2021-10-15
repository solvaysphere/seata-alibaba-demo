package com.solvay.order.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * (Order)表实体类
 *
 * @author makejava
 * @since 2021-10-13 19:52:01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_order")
public class Order extends Model<Order> {
    private static final long serialVersionUID = 859924819823619817L;
    @TableId(type = IdType.AUTO)
        private Integer id;
        private String orderNo;
        private String userId;
        private String commodityCode;
        private Integer count;
        private Object amount;


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

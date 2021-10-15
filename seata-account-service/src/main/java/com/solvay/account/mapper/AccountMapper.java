package com.solvay.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.solvay.account.entity.Account;
import org.apache.ibatis.annotations.Param;

/**
 * (Account)表数据库访问层
 *
 * @author makejava
 * @since 2021-10-13 19:28:55
 */
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 减少账户余额
     * @param userId
     * @param amount
     * @return
     */
    int decreaseAccount(@Param("userId") String userId, @Param("amount") Double amount);
}

package com.solvay.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.solvay.account.entity.Account;
import com.solvay.common.dto.AccountDTO;

/**
 * (Account)表服务接口
 *
 * @author makejava
 * @since 2021-10-13 19:28:55
 */
public interface AccountService extends IService<Account> {

    /**
     * 扣减余额
     * @param accountDTO
     * @return
     */
    boolean decreaseAccount(AccountDTO accountDTO);
}

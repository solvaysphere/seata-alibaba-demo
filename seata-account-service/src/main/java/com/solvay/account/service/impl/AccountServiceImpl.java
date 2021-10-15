package com.solvay.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.solvay.account.entity.Account;
import com.solvay.account.mapper.AccountMapper;
import com.solvay.account.service.AccountService;
import com.solvay.common.dto.AccountDTO;
import org.springframework.stereotype.Service;

/**
 * (Account)表服务实现类
 *
 * @author makejava
 * @since 2021-10-13 19:28:55
 */
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public boolean decreaseAccount(AccountDTO accountDTO) {
        return SqlHelper.retBool(baseMapper.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue()));
    }
}

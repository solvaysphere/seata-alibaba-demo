package com.solvay.account.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.solvay.account.entity.Account;
import com.solvay.common.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.solvay.account.service.AccountService;
import com.solvay.common.dto.AccountDTO;
import com.solvay.common.enums.ApiErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * (Account)表控制层
 *
 * @author makejava
 * @since 2021-10-13 19:28:55
 */
@RestController
@RequestMapping("account")
@Slf4j
public class AccountController{
    /**
     * 服务对象
     */
    @Autowired
    private AccountService accountService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param account 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<Account> page, Account account) {
        return R.ok(this.accountService.page(page, new QueryWrapper<>(account)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return R.ok(this.accountService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param account 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody Account account) {
        return R.ok(this.accountService.save(account));
    }

    /**
     * 修改数据
     *
     * @param account 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody Account account) {
        return R.ok(this.accountService.updateById(account));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return R.ok(this.accountService.removeByIds(idList));
    }

    /**
     * 扣减余额
     * @param accountDTO
     * @return
     */
    @PostMapping("/dec_account")
    public R decreaseAccount(@RequestBody AccountDTO accountDTO) {
        log.info("请求账户微服务：{}", accountDTO.toString());
        boolean ret = accountService.decreaseAccount(accountDTO);
        if (ret){
            return R.ok(ret);
        }
        return R.failed(ApiErrorCode.FAILED);
    }
}

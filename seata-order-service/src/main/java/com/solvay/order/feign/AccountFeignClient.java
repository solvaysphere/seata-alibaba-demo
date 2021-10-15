package com.solvay.order.feign;

import com.solvay.common.api.R;
import com.solvay.common.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用账户系统
 */
@FeignClient(name = "seata-account-service")
public interface AccountFeignClient {
    /**
     * 扣减余额
     *
     * @param accountDTO
     * @return
     */
    @PostMapping("/account/dec_account")
    R decreaseAccount(@RequestBody AccountDTO accountDTO);
}

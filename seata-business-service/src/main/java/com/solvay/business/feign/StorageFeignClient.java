package com.solvay.business.feign;

import com.solvay.common.api.R;
import com.solvay.common.dto.CommodityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seata-storage-service")
public interface StorageFeignClient {

    /**
     * 扣减库存
     */
    @PostMapping("/storage/dec_storage")
    R decreaseStorage(@RequestBody CommodityDTO commodityDTO);
}

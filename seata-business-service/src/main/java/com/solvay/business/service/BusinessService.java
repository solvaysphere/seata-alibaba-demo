package com.solvay.business.service;

import com.solvay.common.api.R;
import com.solvay.common.dto.BusinessDTO;

public interface BusinessService {
    /**
     * 处理业务服务
     * @param businessDTO
     * @return
     */
    R handleBusiness(BusinessDTO businessDTO);

    /**
     * 处理业务服务，出现异常回顾
     * @param businessDTO
     * @return
     */
    R handleBusiness2(BusinessDTO businessDTO);
}

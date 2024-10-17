package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigCreateCmd;
import com.example.odyssey.bean.cmd.RebateConfigListQryCmd;
import com.example.odyssey.bean.cmd.RebateConfigUpdateCmd;
import com.example.odyssey.bean.dto.RebateConfigDTO;

public interface RebateConfigService {


    SingleResponse add(RebateConfigCreateCmd rebateConfigCreateCmd);
    /**
     * 更新返利的比例配置
     * @param rebateConfigUpdateCmd
     * @return
     */
    SingleResponse update(RebateConfigUpdateCmd rebateConfigUpdateCmd);


    MultiResponse<RebateConfigDTO> listRebateConfig(RebateConfigListQryCmd rebateConfigListQryCmd);
}

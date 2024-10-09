package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigUpdateCmd;
import com.example.odyssey.bean.dto.RebateConfigDTO;
import com.example.odyssey.core.service.RebateConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/rebate/config")
public class RebateConfigController {

    @Resource
    private RebateConfigService rebateConfigService;

    @PostMapping("/update")
    public SingleResponse update(@RequestBody RebateConfigUpdateCmd rebateConfigUpdateCmd){

        Assert.notNull(rebateConfigUpdateCmd.getRebateType(), "返佣类型不能为空");
        Assert.notNull(rebateConfigUpdateCmd.getRecommendType(), "推荐类型不能为空");

        return rebateConfigService.update(rebateConfigUpdateCmd);
    }

    @PostMapping("/list")
    public MultiResponse<RebateConfigDTO> list(){

        return rebateConfigService.list();
    }
}

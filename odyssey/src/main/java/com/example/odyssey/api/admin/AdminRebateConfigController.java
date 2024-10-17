package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigCreateCmd;
import com.example.odyssey.bean.cmd.RebateConfigListQryCmd;
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
public class AdminRebateConfigController {

    @Resource
    private RebateConfigService rebateConfigService;

    @PostMapping("/add")
    SingleResponse add(@RequestBody RebateConfigCreateCmd rebateConfigCreateCmd) {

        Assert.notNull(rebateConfigCreateCmd.getAddress(), "地址不能为空");
        Assert.notNull(rebateConfigCreateCmd.getRebateType(), "返佣类型不能为空");
        Assert.notNull(rebateConfigCreateCmd.getRecommendType(), "推荐类型不能为空");

        return rebateConfigService.add(rebateConfigCreateCmd);

    }


    @PostMapping("/update")
    public SingleResponse update(@RequestBody RebateConfigUpdateCmd rebateConfigUpdateCmd) {
        Assert.notNull(rebateConfigUpdateCmd.getId(), "配置id不能为空");
        Assert.notNull(rebateConfigUpdateCmd.getRebateType(), "返佣类型不能为空");
        Assert.notNull(rebateConfigUpdateCmd.getRecommendType(), "推荐类型不能为空");

        return rebateConfigService.update(rebateConfigUpdateCmd);
    }

    @PostMapping("/list")
    public MultiResponse<RebateConfigDTO> list(@RequestBody RebateConfigListQryCmd rebateConfigListQryCmd) {

        return rebateConfigService.listRebateConfig(rebateConfigListQryCmd);
    }
}

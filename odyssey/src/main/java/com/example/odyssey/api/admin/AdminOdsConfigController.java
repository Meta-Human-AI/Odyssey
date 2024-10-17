package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsConfigUpdateCmd;
import com.example.odyssey.bean.dto.OdsConfigDTO;
import com.example.odyssey.core.service.OdsConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/ods/config")
public class AdminOdsConfigController {

    @Resource
    private OdsConfigService odsConfigService;

    /**
     * 更新ods 每天发放数量
     * @param odsConfigUpdateCmd
     * @return
     */
    @PostMapping("/update")
    public SingleResponse update(@RequestBody OdsConfigUpdateCmd odsConfigUpdateCmd){

        Assert.notNull(odsConfigUpdateCmd.getType(), "配置类型不能为空");

        return odsConfigService.update(odsConfigUpdateCmd);
    }

    /**
     * ods 等级配置
     * @return
     */
    @PostMapping("/list")
    public MultiResponse<OdsConfigDTO> list(){

        return odsConfigService.list();
    }
}

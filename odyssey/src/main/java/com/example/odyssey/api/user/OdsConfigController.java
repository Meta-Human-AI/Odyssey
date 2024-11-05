package com.example.odyssey.api.user;

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
@RequestMapping("/v1/ods/config")
public class OdsConfigController {

    @Resource
    private OdsConfigService odsConfigService;


    /**
     * ods 等级配置
     * @return
     */
    @PostMapping("/list")
    public MultiResponse<OdsConfigDTO> list(){

        return odsConfigService.list();
    }
}

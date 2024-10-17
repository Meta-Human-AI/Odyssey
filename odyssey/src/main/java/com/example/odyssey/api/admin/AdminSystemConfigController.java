package com.example.odyssey.api.admin;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.dto.SystemConfigDTO;
import com.example.odyssey.core.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/system/config")
public class AdminSystemConfigController {

    @Resource
    SystemConfigService systemConfigService;

    @GetMapping("/list")
    public MultiResponse<SystemConfigDTO> listSystemConfig() {
        return systemConfigService.listSystemConfig();
    }

}

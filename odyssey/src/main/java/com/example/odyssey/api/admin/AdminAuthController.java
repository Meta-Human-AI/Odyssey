package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OperatorLoginCmd;
import com.example.odyssey.bean.dto.OperatorLoginDTO;
import com.example.odyssey.core.service.OperatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/auth")
public class AdminAuthController {

    @Resource
    OperatorService operatorService;

    /**
     * 登录
     *
     * @param operatorLoginCmd
     * @return
     */
    @PostMapping("/login")
    SingleResponse<OperatorLoginDTO> loginOperator(@RequestBody OperatorLoginCmd operatorLoginCmd){
        Assert.notNull(operatorLoginCmd.getUsername(), "用户名不能为空");
        Assert.notNull(operatorLoginCmd.getPassword(), "密码不能为空");
        return operatorService.loginOperator(operatorLoginCmd);
    }
}

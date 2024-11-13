package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OperatorDTO;
import com.example.odyssey.bean.dto.OperatorInfoDTO;
import com.example.odyssey.core.service.OperatorService;
import com.example.odyssey.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/operator")
public class OperatorController {

    @Resource
    OperatorService operatorService;

    @PostMapping("/info")
    SingleResponse<OperatorInfoDTO> queryOperatorInfo(@RequestBody OperatorQryCmd operatorQryCmd){

        Assert.notNull(operatorQryCmd.getWalletAddress(), "钱包地址不能为空");

        return operatorService.queryOperatorInfo(operatorQryCmd);
    }
}

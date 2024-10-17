package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OperatorDTO;
import com.example.odyssey.bean.dto.OperatorLoginDTO;
import com.example.odyssey.core.service.OperatorService;
import com.example.odyssey.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/admin/operator")
public class AdminOperatorController {

    @Resource
    OperatorService operatorService;


    /**
     * 创建操作员
     *
     * @param operatorCreateCmd
     * @return
     */
    @PostMapping("/create")
    SingleResponse createOperator(@RequestBody OperatorCreateCmd operatorCreateCmd) {

        Assert.notNull(operatorCreateCmd.getUsername(), "用户名不能为空");
        Assert.notNull(operatorCreateCmd.getPassword(), "密码不能为空");

        return operatorService.createOperator(operatorCreateCmd);
    }

    /**
     * 修改密码
     *
     * @param operatorChargePasswordCmd
     * @return
     */
    @PostMapping("/charge/password")
    SingleResponse chargePassword(@RequestBody OperatorChargePasswordCmd operatorChargePasswordCmd) {
        Assert.notNull(operatorChargePasswordCmd.getId(), "用户id不能为空");
        Assert.notNull(operatorChargePasswordCmd.getOldPassword(), "旧密码不能为空");
        Assert.notNull(operatorChargePasswordCmd.getNewPassword(), "新密码不能为空");

        return operatorService.chargePassword(operatorChargePasswordCmd);
    }

    /**
     * 修改操作员信息
     *
     * @param operatorUpdateCmd
     * @return
     */
    @PostMapping("/update")
    SingleResponse updateOperator(@RequestBody OperatorUpdateCmd operatorUpdateCmd) {

        Assert.notNull(operatorUpdateCmd.getId(), "用户id不能为空");
        Assert.notNull(operatorUpdateCmd.getStatus(), "状态不能为空");
        return operatorService.updateOperator(operatorUpdateCmd);
    }


    @PostMapping("/logout")
    SingleResponse logoutOperator(HttpServletRequest request) {

        Integer operatorId = JwtTokenUtil.getOperatorId(request);

        OperatorLogoutCmd operatorLogoutCmd = new OperatorLogoutCmd();
        operatorLogoutCmd.setId(operatorId);

        return operatorService.logoutOperator(operatorLogoutCmd);

    }

    /**
     * 查询操作员
     *
     * @return
     */
    @GetMapping("/query")
    SingleResponse<OperatorDTO> queryOperator(HttpServletRequest request) {

        Integer operatorId = JwtTokenUtil.getOperatorId(request);

        OperatorQryCmd operatorQryCmd = new OperatorQryCmd();
        operatorQryCmd.setId(operatorId);

        return operatorService.queryOperator(operatorQryCmd);

    }

    /**
     * 操作员列表
     *
     * @param operatorListQryCmd
     * @return
     */
    @PostMapping("/list")
    MultiResponse<OperatorDTO> listOperator(@RequestBody OperatorListQryCmd operatorListQryCmd) {
        return operatorService.listOperator(operatorListQryCmd);
    }
}

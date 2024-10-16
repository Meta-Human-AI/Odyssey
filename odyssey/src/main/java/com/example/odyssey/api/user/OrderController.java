package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OrderCreateCmd;
import com.example.odyssey.core.service.OrderService;
import com.example.odyssey.util.EmailUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/order")
public class OrderController {

    @Resource
    EmailUtil emailUtil;

    @Resource
    OrderService orderService;

    @PostMapping("/add")
    SingleResponse createOrder(@RequestBody OrderCreateCmd orderCreateCmd) {

        Assert.notNull(orderCreateCmd.getAddress(), "地址不能为空");
        Assert.notNull(orderCreateCmd.getTokenId(), "nft不能为空");
        Assert.notNull(orderCreateCmd.getEmail(), "邮箱不能为空");

        Assert.isTrue(emailUtil.isEmail(orderCreateCmd.getEmail()), "邮箱格式不正确");

        return orderService.createOrder(orderCreateCmd);
    }
}

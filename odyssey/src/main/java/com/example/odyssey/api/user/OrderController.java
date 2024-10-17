package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OrderAppealCancelCmd;
import com.example.odyssey.bean.cmd.OrderAppealCreateCmd;
import com.example.odyssey.bean.cmd.OrderCancelCmd;
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

    /**
     * 创建酒店订单
     * @param orderCreateCmd
     * @return
     */
    @PostMapping("/add")
    SingleResponse createOrder(@RequestBody OrderCreateCmd orderCreateCmd) {

        Assert.notNull(orderCreateCmd.getAddress(), "地址不能为空");
        Assert.notNull(orderCreateCmd.getTokenId(), "nft不能为空");
        Assert.notNull(orderCreateCmd.getEmail(), "邮箱不能为空");
        Assert.notNull(orderCreateCmd.getPhone(), "手机号不能为空");
        Assert.notNull(orderCreateCmd.getNumber(), "入住人数不能为空");
        Assert.notNull(orderCreateCmd.getStartTime(), "入住时间不能为空");
        Assert.notNull(orderCreateCmd.getEndTime(), "离店时间不能为空");
        Assert.notNull(orderCreateCmd.getName(), "姓名不能为空");
        Assert.isTrue(emailUtil.isEmail(orderCreateCmd.getEmail()), "邮箱格式不正确");

        return orderService.createOrder(orderCreateCmd);
    }
    /**
     * 取消酒店订单
     * @param orderCancelCmd
     * @return
     */
    @PostMapping("/cancel")
    SingleResponse cancelOrder(@RequestBody OrderCancelCmd orderCancelCmd) {
        Assert.notNull(orderCancelCmd.getOrderId(), "订单id不能为空");
        return orderService.cancelOrder(orderCancelCmd);
    }

    /**
     * 申诉订单
     * @param orderAppealCreateCmd
     * @return
     */
    @PostMapping("/appeal")
    SingleResponse appealOrder(@RequestBody OrderAppealCreateCmd orderAppealCreateCmd) {
        Assert.notNull(orderAppealCreateCmd.getOrderId(), "订单id不能为空");
        return orderService.appealOrder(orderAppealCreateCmd);
    }

    /**
     * 取消申诉订单
     * @param orderAppealCancelCmd
     * @return
     */
    @PostMapping("/cancel/appeal")
    SingleResponse cancelAppealOrder(@RequestBody OrderAppealCancelCmd orderAppealCancelCmd) {
        Assert.notNull(orderAppealCancelCmd.getOrderAppealId(), "订单申诉id不能为空");
        return orderService.cancelAppealOrder(orderAppealCancelCmd);
    }
}

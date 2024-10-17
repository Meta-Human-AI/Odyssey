package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OrderAppealHandleCmd;
import com.example.odyssey.bean.cmd.OrderExamineCmd;
import com.example.odyssey.bean.cmd.OrderFinishCmd;
import com.example.odyssey.core.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/order")
public class AdminOrderController {

    @Resource
    OrderService orderService;
    /**
     * 审核订单
     * @param orderExamineCmd
     * @return
     */
    @PostMapping("/examine")
    SingleResponse examineOrder(@RequestBody OrderExamineCmd orderExamineCmd){
        Assert.notNull(orderExamineCmd.getOrderId(), "订单id不能为空");
        return orderService.examineOrder(orderExamineCmd);
    }
    /**
     * 处理申诉订单
     * @param orderAppealHandleCmd
     * @return
     */
    @PostMapping("/handle/appeal")
    SingleResponse handleAppealOrder(OrderAppealHandleCmd orderAppealHandleCmd){
        Assert.notNull(orderAppealHandleCmd.getOrderAppealId(), "订单申诉id不能为空");
        return orderService.handleAppealOrder(orderAppealHandleCmd);
    }

    /**
     * 完成订单
     * @param orderFinishCmd
     * @return
     */
    @PostMapping("/finish")
    SingleResponse finishOrder(@RequestBody OrderFinishCmd orderFinishCmd){
        Assert.notNull(orderFinishCmd.getOrderId(), "订单id不能为空");
        return orderService.finishOrder(orderFinishCmd);
    }
}

package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OrderAppealDTO;
import com.example.odyssey.bean.dto.OrderDTO;
import com.example.odyssey.core.service.OrderService;
import com.example.odyssey.util.EmailUtil;
import com.example.odyssey.util.MinioUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/order")
public class OrderController {

    @Resource
    EmailUtil emailUtil;
    @Resource
    MinioUtils minioUtils;
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

    @PostMapping("/appeal/upload")
    public SingleResponse<String> upload(@RequestBody MultipartFile file) {
        try {
            String hotel = minioUtils.upload("order-appeal", file);
            return SingleResponse.of(hotel);
        }catch (Exception e) {
            e.printStackTrace();
            return SingleResponse.buildFailure("文件上传异常");
        }
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

    /**
     * 分页查询订单
     * @param orderListQryCmd
     * @return
     */
    @PostMapping("/page")
    MultiResponse<OrderDTO> listOrder(@RequestBody OrderListQryCmd orderListQryCmd){
        Assert.notNull(orderListQryCmd.getAddress(), "钱包地址不能为空");
        return orderService.listOrder(orderListQryCmd);
    }


    /**
     * 分页查询订单申诉
     * @param orderAppealListQryCmd
     * @return
     */
    @PostMapping("/appeal/page")
    MultiResponse<OrderAppealDTO> listOrderAppeal(@RequestBody OrderAppealListQryCmd orderAppealListQryCmd) {
        Assert.notNull(orderAppealListQryCmd.getOrderId(),"订单id不能为空");
        return orderService.listOrderAppeal(orderAppealListQryCmd);
    }
}

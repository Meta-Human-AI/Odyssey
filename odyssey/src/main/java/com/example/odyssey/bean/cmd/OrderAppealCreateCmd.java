package com.example.odyssey.bean.cmd;

import lombok.Data;

import java.util.List;

@Data
public class OrderAppealCreateCmd {

    /**
     * 订单id
     */
    private Integer orderId;
    /**
     * 申诉原因
     */
    private String reason;
    /**
     * 申诉图片
     */
    private List<String> images;
}

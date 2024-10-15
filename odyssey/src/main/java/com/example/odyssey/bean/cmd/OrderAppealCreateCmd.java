package com.example.odyssey.bean.cmd;

import lombok.Data;

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
}

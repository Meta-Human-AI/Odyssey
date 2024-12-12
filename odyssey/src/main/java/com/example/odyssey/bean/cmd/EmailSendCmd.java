package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class EmailSendCmd {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 订单id
     */
    private Integer orderId;

    private String hotelName;

    private String address;

    /**
     * 入住时间
     */
    private String startTime;
    /**
     * 离店时间
     */
    private String endTime;
    /**
     * 审核时间
     */
    private String examineTime;

    private String reason;


}

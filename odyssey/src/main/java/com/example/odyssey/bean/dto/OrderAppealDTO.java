package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class OrderAppealDTO {

    private Integer id;
    /**
     * 订单id
     */
    private Integer orderId;
    /**
     * 申诉原因
     */
    private String reason;
    /**
     * 申诉状态
     */
    private String status;
    /**
     * 处理说明
     */
    private String remark;
    /**
     * 申诉时间
     */
    private String createTime;
    /**
     * 处理时间
     */
    private String finishTime;
}

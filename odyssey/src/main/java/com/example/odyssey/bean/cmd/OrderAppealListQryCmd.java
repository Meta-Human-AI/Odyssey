package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class OrderAppealListQryCmd extends PageQuery {
    /**
     * 订单id
     */
    private Integer orderId;
    /**
     * 申诉状态
     */
    private String status;

    /**
     * 申诉时间
     */
    private String createStartTime;

    private String createEndTime;
    /**
     * 处理时间
     */
    private String finishStartTime;

    private String finishEndTime;
}

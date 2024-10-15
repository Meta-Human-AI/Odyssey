package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OrderCancelCmd {

    private Integer orderId;

    /**
     * 取消原因
     */
    private String reason;
}

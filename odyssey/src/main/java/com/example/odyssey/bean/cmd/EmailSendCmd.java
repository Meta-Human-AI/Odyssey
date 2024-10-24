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


}

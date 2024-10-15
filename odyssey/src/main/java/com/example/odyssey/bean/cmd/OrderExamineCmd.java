package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OrderExamineCmd {

    private Integer orderId;

    private String status;

    private String reason;
}

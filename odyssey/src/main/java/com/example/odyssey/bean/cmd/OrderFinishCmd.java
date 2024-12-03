package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OrderFinishCmd {

    private Integer orderId;


    private String status;

    private String reason;
}

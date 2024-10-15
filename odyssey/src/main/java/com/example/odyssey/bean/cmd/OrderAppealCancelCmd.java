package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OrderAppealCancelCmd {

    private Integer orderAppealId;

    private String remark;
}

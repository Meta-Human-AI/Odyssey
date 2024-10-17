package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OperatorChargePasswordCmd {

    private Integer id;

    private String oldPassword;

    private String newPassword;
}

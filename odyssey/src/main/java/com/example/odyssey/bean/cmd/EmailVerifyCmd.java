package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class EmailVerifyCmd {
    /**
     * 验证码
     */
    private String verificationCode;

}

package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailCreateCmd;
import com.example.odyssey.bean.cmd.EmailVerifyCmd;

public interface EmailService {

    SingleResponse sendEmail(EmailCreateCmd emailCreateCmd);

    SingleResponse verifyEmail(EmailVerifyCmd emailVerifyCmd);
}

package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailCreateCmd;
import com.example.odyssey.bean.cmd.EmailSendCmd;
import com.example.odyssey.bean.cmd.EmailVerifyCmd;

public interface EmailService {

    SingleResponse createEmail(EmailCreateCmd emailCreateCmd);

    SingleResponse sendCreateOrderEmail(EmailSendCmd emailSendCmd);

    SingleResponse sendOrderExaminePassEmail(EmailSendCmd emailSendCmd);

    SingleResponse sendOrderExamineRejectEmail(EmailSendCmd emailSendCmd);

    SingleResponse sendOrderFinishEmail(EmailSendCmd emailSendCmd);

    SingleResponse sendOrderFailEmail(EmailSendCmd emailSendCmd);



    SingleResponse verifyEmail(EmailVerifyCmd emailVerifyCmd);
}

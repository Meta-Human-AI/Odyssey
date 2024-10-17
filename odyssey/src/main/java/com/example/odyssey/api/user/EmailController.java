package com.example.odyssey.api.user;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailVerifyCmd;
import com.example.odyssey.core.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    /**
     * 验证邮箱
     * @param code
     * @return
     */
    @GetMapping("/verify")
    public SingleResponse verify(@RequestParam String code) {

        EmailVerifyCmd emailVerifyCmd = new EmailVerifyCmd();
        emailVerifyCmd.setVerificationCode(code);
        return emailService.verifyEmail(emailVerifyCmd);
    }
}

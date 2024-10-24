package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailCreateCmd;
import com.example.odyssey.bean.cmd.EmailVerifyCmd;
import com.example.odyssey.core.service.EmailService;
import com.example.odyssey.util.EmailUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/email")
public class EmailController {
    @Resource
    EmailUtil emailUtil;
    @Resource
    private EmailService emailService;

    /**
     * 验证邮箱
     *
     * @param code
     * @return
     */
    @GetMapping("/verify")
    public SingleResponse verify(@RequestParam String code) {

        EmailVerifyCmd emailVerifyCmd = new EmailVerifyCmd();
        emailVerifyCmd.setVerificationCode(code);
        return emailService.verifyEmail(emailVerifyCmd);
    }

    @PostMapping("/add")
    SingleResponse createEmail(@RequestBody EmailCreateCmd emailCreateCmd) {

        Assert.isTrue(emailUtil.isEmail(emailCreateCmd.getEmail()), "邮箱格式不正确");
        return emailService.createEmail(emailCreateCmd);
    }
}

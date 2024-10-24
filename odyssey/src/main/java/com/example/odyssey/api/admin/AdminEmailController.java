package com.example.odyssey.api.admin;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailSendCmd;
import com.example.odyssey.core.service.EmailService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/email")
public class AdminEmailController {

    @Resource
    private EmailService emailService;

    @PostMapping("/send")
    public SingleResponse send(@RequestBody EmailSendCmd emailSendCmd) {
        return emailService.sendEmail(emailSendCmd);
    }

}

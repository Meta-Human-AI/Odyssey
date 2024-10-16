package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.EmailCreateCmd;
import com.example.odyssey.bean.cmd.EmailVerifyCmd;
import com.example.odyssey.common.OrderStatusEnum;
import com.example.odyssey.core.service.EmailService;
import com.example.odyssey.model.entity.Order;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.OrderMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import com.example.odyssey.util.EmailUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {

    @Resource
    EmailUtil emailUtil;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    JavaMailSender javaMailSender;

    @Resource
    OrderMapper orderMapper;

    @Resource
    SystemConfigMapper systemConfigMapper;

    @Value("${spring.mail.username}")
    private String from;

    @SneakyThrows
    @Override
    public SingleResponse sendEmail(EmailCreateCmd emailCreateCmd) {

        QueryWrapper<SystemConfig> systemQueryWrapper = new QueryWrapper<>();

        systemQueryWrapper.eq("key", "ods_url");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemQueryWrapper);

        if (Objects.isNull(systemConfig)) {
            return SingleResponse.buildFailure("URL configuration not found");
        }

        String verificationCode = emailUtil.getVerificationCode();

        String emailContent = emailUtil.getEmailContent(verificationCode, systemConfig.getValue());

        // todo 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(emailCreateCmd.getEmail());
        message.setSubject("Email Verification");
        message.setText(emailContent);

        javaMailSender.send(message);

        // todo 将验证码存入redis
        redisTemplate.opsForValue().set("ODYSSEY:EMAIL:VERIFICATION：" + verificationCode, emailCreateCmd.getOrderId(), 5, TimeUnit.MINUTES);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse verifyEmail(EmailVerifyCmd emailVerifyCmd) {

        // todo 验证验证码
        Object orderId = redisTemplate.opsForValue().get("ODYSSEY:EMAIL:VERIFICATION：" + emailVerifyCmd.getVerificationCode());
        if (Objects.isNull(orderId)) {
            return SingleResponse.buildFailure("Verification code is invalid or expired");
        }

        // todo 验证成功更新订单状态
        Order order = orderMapper.selectById(Integer.valueOf(orderId.toString()));
        if (Objects.isNull(order)) {
            return SingleResponse.buildFailure("Order not found");
        }

        order.setStatus(OrderStatusEnum.EXAMINE.getCode());
        orderMapper.updateById(order);
        // todo 删除redis中的验证码

        redisTemplate.delete("ODYSSEY:EMAIL:VERIFICATION：" + emailVerifyCmd.getVerificationCode());

        return SingleResponse.buildSuccess();
    }
}

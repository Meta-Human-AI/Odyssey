package com.example.odyssey.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailUtil {

    /**
     * 生成验证码
     *
     * @return
     */
    public String getVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
    /**
     * 生成邮件内容
     *
     * @param verificationCode
     * @return
     */
    public String getEmailContent(String verificationCode,String url) {
        String content = "Dear User, please click on the following link to verify your email: " +
                url + "/odyssey/v1/admin/email/verify?code=" + verificationCode;
        return content;
    }
    /**
     * 验证邮箱格式
     *
     * @param email
     * @return
     */
    public  Boolean isEmail(String email) {
        if (email == null || email.length() < 1 || email.length() > 256) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(email).matches();
    }


}

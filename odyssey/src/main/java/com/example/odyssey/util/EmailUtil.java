package com.example.odyssey.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    public String getEmailContent(String verificationCode) {
        String content = "Dear user, this is your hotel reservation verification code: " + verificationCode
                + " please verify it on the platform within 5 minutes ";

        return content;
    }

    public String getOrderExaminePassContent(String hotelName) {

        String content = "Dear user, your hotel [" + hotelName + "] reservation has been approved. Please wait for the administrator to make a reservation for you";

        return content;
    }

    public String getOrderExamineRejectContent(String hotelName, String reason) {

        if (StringUtils.hasLength(reason)) {
            String content = "DDear user, we are very sorry to inform you that your hotel [" + hotelName + "] order has not been approved," +
                    " The reason is [" + reason + "], if you have any questions, please contact us";
            return content;
        } else {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "] order has not been approved," +
                    " if you have any questions, please contact us";
            return content;
        }
    }


    public String getOrderFinishContent(String hotelName) {

        String content = "Dear user, congratulations! Your hotel [" + hotelName + "]reservation has been successfully booked";

        return content;
    }


    public String getOrderFailContent(String hotelName, String reason) {

        if (StringUtils.hasLength(reason)) {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "]reservation was not successfully booked." +
                    " The reason is [" + reason + "]. we apologize again. If there are any problems, please contact us";
            return content;
        } else {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "]reservation was not successfully booked." +
                    " we apologize again. If there are any problems, please contact us";
            return content;
        }
    }

    /**
     * 验证邮箱格式
     *
     * @param email
     * @return
     */
    public Boolean isEmail(String email) {
        if (email == null || email.length() < 1 || email.length() > 256) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(email).matches();
    }


}

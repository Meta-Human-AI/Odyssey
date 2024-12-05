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
        String content = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<p>Hotel Reservation Verification Code</p>" +
                "<p>Dear User</p>" +
                "<p>Thank you for choosing our service! To ensure the security of your reservation, please verify your booking using the code below within the next 5 minutes:</p>" +
                "<p>Verification Code: <span style='font-size: 24px; font-weight: bold; color: #333;'>" + verificationCode + "</span></p>" +
                "<p>Simply enter this code on the platform to complete your verification. If you did not request this, please contact our support team immediately.</p>" +
                "<p>We will do our utmost to ensure your extraordinary journey!</p>" +
                "<p>Warm regards,<br>" +
                "Support Team<br>" +
                "[reservation@odysseyglobal.io | Contact Team | Website Link]</p>" +
                "</div>" +
                "</body></html>";

        return content;
    }

    public String getOrderExaminePassContent(String hotelName) {

        String content = "Dear user, your hotel [" + hotelName + "] reservation has been approved. Please wait for the administrator to make a reservation for you.";

        return content;
    }

    public String getOrderExamineRejectContent(String hotelName, String reason) {

        if (StringUtils.hasLength(reason)) {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "] order has not been approved," +
                    " The reason is [" + reason + "], if you have any questions, please contact us.";
            return content;
        } else {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "] order has not been approved," +
                    " if you have any questions, please contact us.";
            return content;
        }
    }


    public String getOrderFinishContent(String hotelName) {

        String content = "Dear user, congratulations! Your hotel [" + hotelName + "]reservation has been successfully booked.";

        return content;
    }


    public String getOrderFailContent(String hotelName, String reason) {

        if (StringUtils.hasLength(reason)) {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "]reservation was not successfully booked." +
                    " The reason is [" + reason + "]. we apologize again. If there are any problems, please contact us.";
            return content;
        } else {
            String content = "Dear user, we are very sorry to inform you that your hotel [" + hotelName + "]reservation was not successfully booked." +
                    " we apologize again. If there are any problems, please contact us.";
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

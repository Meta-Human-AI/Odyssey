package com.example.odyssey.handler;


import com.example.odyssey.bean.Response;
import com.example.odyssey.bean.SingleResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Response customerException(RuntimeException runtimeException) {

        runtimeException.printStackTrace();

        runtimeException.printStackTrace();

        SingleResponse response = new SingleResponse();
        response.setCode(500);
        response.setErrMessage(runtimeException.getMessage());
        return response;
    }
}

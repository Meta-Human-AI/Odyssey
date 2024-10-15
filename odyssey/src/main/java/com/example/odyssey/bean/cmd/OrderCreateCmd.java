package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class OrderCreateCmd {

    private String address;

    private Long tokenId;

    private String hotel;

    private String startTime;

    private String endTime;

    private Integer number;

    private String name;

    private String phone;

    private String email;
}

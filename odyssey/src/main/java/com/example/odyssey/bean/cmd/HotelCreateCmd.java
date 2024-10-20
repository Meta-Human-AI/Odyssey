package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class HotelCreateCmd {

    private String name;

    private Long state;

    private Long city;

    private String address;

    private String phone;

    private String email;

}

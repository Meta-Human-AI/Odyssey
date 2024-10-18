package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class HotelCreateCmd {

    private String name;

    private String state;

    private String city;

    private String address;

    private String phone;

    private String email;

}

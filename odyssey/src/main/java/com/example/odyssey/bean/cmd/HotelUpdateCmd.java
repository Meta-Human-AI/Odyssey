package com.example.odyssey.bean.cmd;

import lombok.Data;

import java.util.List;

@Data
public class HotelUpdateCmd {

    private Integer id;
    private String name;

    private Long state;

    private Long city;

    private String address;

    private String phone;

    private String email;


    private String introduction;


    private String officialWebsite;


    private List<String> image;
}

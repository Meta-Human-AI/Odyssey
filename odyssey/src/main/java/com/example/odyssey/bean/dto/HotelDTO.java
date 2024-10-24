package com.example.odyssey.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class HotelDTO {

    private Integer id;

    private String name;


    private String state;


    private String city;


    private String address;


    private String phone;


    private String email;


    /**
     * 简介
     */
    private String introduction;
    /**
     * 图片
     */
    private List<String> image;
    /**
     * 官网
     */
    private String officialWebsite;
}

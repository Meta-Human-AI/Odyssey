package com.example.odyssey.bean.cmd;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class HotelCreateCmd {

    private String name;

    private Long state;

    private String stateName;

    private Long city;

    private String cityName;

    private String address;

    private String phone;

    private String email;


    private String introduction;


    private String officialWebsite;


    private List<String> image;

}

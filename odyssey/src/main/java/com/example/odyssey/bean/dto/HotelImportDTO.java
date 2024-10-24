package com.example.odyssey.bean.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class HotelImportDTO {

    @ExcelProperty(value = "HOTEL_NAME")
    private String name;

    @ExcelProperty(value = "HOTEL_CITY_STATE")
    private String stateName;

    @ExcelProperty(value = "HOTEL_CITY")
    private String cityName;

    @ExcelProperty(value = "HOTEL_DETAIL_ADDRESS")
    private String address;

    @ExcelProperty(value = "HOTEL_PHONE")
    private String phone;

    @ExcelProperty(value = "HOTEL_EMAIL")
    private String email;

    @ExcelProperty(value = "HOTEL_INTRODUCTION")
    private String introduction;

    @ExcelProperty(value = "HOTEL_OFFICCIAL_WEBSITE")
    private String officialWebsite;

    @ExcelProperty(value = "HOTEL_PICTURE_URL")
    private String image;
}

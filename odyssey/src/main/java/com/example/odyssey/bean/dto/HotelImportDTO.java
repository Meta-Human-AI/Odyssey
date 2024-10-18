package com.example.odyssey.bean.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class HotelImportDTO {

    @ExcelProperty(value = "酒店名称")
    private String name;

    @ExcelProperty(value = "所在州")
    private String state;

    @ExcelProperty(value = "所在城市")
    private String city;

    @ExcelProperty(value = "详细地址")
    private String address;

    @ExcelProperty(value = "酒店手机号")
    private String phone;

    @ExcelProperty(value = "酒店邮箱")
    private String email;
}

package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class HotelListQryCmd extends PageQuery {

    private String name;


    private Long state;


    private Long city;


    private String address;


    private String phone;


    private String email;

    private Long tokenId;
}

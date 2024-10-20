package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class CityListQryCmd extends PageQuery {

    private Long state;

    private String name;
}

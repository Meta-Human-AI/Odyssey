package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class OperatorListQryCmd extends PageQuery {

    private String username;
}

package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class RebateConfigListQryCmd extends PageQuery {

    private String address;
    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 返利类型
     */
    private String rebateType;
}

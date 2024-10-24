package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RebateConfigUpdateCmd {

    private Integer id;
    /**
     * 一级推荐人返利比例
     */
    private String firstRebateRate;

    /**
     * 二级推荐人返利比例
     */
    private String secondRebateRate;


    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 返利类型
     */
    private String rebateType;
}

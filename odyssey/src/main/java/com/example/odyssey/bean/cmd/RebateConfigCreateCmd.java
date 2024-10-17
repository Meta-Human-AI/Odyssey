package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RebateConfigCreateCmd {

    /**
     * 领导地址
     */
    private String address;
    /**
     * 一级推荐人返利比例
     */
    private String firstRebateRate;

    /**
     * 二级推荐人返利比例
     */
    private String secondRebateRate;

    /**
     * 三级推荐人返利比例
     */
    private String thirdRebateRate;

    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 返利类型
     */
    private String rebateType;
}

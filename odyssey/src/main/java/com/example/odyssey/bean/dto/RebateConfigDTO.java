package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class RebateConfigDTO {

    private Integer id;

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
     * 推荐类型
     */
    private String recommendType;

    /**
     * 返利类型
     */
    private String rebateType;
    /**
     * 三级推荐人
     */
    private String threeRebateRate;
}

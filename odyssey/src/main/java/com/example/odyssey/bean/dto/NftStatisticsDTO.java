package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class NftStatisticsDTO {

    /**
     * 统计日期
     */
    private String date;

    /**
     * OL总数
     */
    private Integer olTotal;

    /**
     * OS总数
     */
    private Integer osTotal;

    /**
     * OA总数
     */
    private Integer oaTotal;

    /**
     * OB总数
     */
    private Integer obTotal;

    /**
     * OL新增
     */
    private Integer olNew;

    /**
     * OS新增
     */
    private Integer osNew;

    /**
     * OA新增
     */
    private Integer oaNew;

    /**
     * OB新增
     */
    private Integer obNew;

    /**
     * 更新时间
     */
    private String updateTime;
}

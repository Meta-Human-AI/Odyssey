package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class RecommendDTO {

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 一级推荐人钱包地址
     */
    private String firstRecommendWalletAddress;

    /**
     * 二级推荐人钱包地址
     */
    private String secondRecommendWalletAddress;

    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 推荐码
     */
    private String recommendCode;

    /**
     * 推荐人钱包地址
     */
    private String recommendWalletAddress;

    /**
     * 领导钱包地址
     */
    private String leaderWalletAddress;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 推荐时间
     */
    private Long recommendTime;
}

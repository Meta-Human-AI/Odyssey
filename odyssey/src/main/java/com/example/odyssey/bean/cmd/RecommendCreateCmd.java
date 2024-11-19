package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RecommendCreateCmd {

    /**
     * 钱包地址
     */
    private String walletAddress;
    /**
     * 推荐码
     */
    private String recommendCore;

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
     * 推荐人钱包地址
     */
    private String recommendWalletAddress;

    /**
     * 领导钱包地址
     */
    private String leaderWalletAddress;
    /**
     * 推荐时间
     */
    private Long recommendTime;
}

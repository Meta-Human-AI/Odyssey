package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class RewardDistributionDTO {

    private Integer id;

    /**
     * 钱包地址
     */
    private String walletAddress;
    /**
     * 关联地址 因为谁 而获得返利
     */
    private String relationAddress;
    /**
     * nft
     */
    private Long tokenId;

    /**
     * 奖励数量
     */
    private String rewardNumber;

    /**
     * 奖励类型
     */
    private String rewardType;

    /**
     * 奖励状态
     */
    private String rewardStatus;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 奖励时间
     */
    private String rewardTime;

    private String hash;
}

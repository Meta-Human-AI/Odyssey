package com.example.odyssey.bean.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RewardCalculateResultDTO {

    private String date;            // 计算日期
    private Long tokenId;           // NFT ID
    private String type;            // NFT类型
    private String walletAddress;   // 持有人地址
    private String firstRecommend;  // 一级推荐人
    private String secondRecommend; // 二级推荐人
    private String leaderAddress;   // 队长地址
    private String recommendType;   // 推荐类型
    private BigDecimal amount;      // 奖励金额
    private String rewardType;      // 奖励类型(持有/推荐)

    private String recommendAddress;

}

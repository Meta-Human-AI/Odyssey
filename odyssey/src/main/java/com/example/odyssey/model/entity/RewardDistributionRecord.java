package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖励发放记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reward_distribution_record")
public class RewardDistributionRecord {

    @TableId(type = IdType.AUTO)
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
}

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

    @TableId(type = IdType.INPUT)
    private Integer id;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 等级类型
     */
    private String type;

    /**
     * 拥有等级数量
     */
    private Integer number;

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
    private Long createTime;

    /**
     * 奖励时间
     */
    private Long rewardTime;
}

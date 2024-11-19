package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("region_recommend_log")
public class RegionRecommendLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 区域推荐人的地址
     */
    private String regionAddress;

    /**
     * 被推荐队长
     */
    private String leaderAddress;

    /**
     * 返佣数量
     */
    private String rewardNumber;
    /**
     * 返佣类型
     */
    private String type;
    /**
     * 对应的token_id
     */
    private Long tokenId;
    /**
     * 奖励发放id
     */
    private Integer rewardDistributionRecordId;
}

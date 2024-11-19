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
@TableName("region_recommend")
public class RegionRecommend {

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
     * 返佣比例
     */
    private String rebateRate;
}

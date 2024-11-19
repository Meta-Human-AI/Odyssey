package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommend")
public class Recommend{

    @TableId(type = IdType.AUTO)
    private Integer id;

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

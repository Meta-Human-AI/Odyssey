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
@TableName("`nft_daily_hold_record`")
public class NftDailyHoldRecord {

    @TableId(type = IdType.INPUT)
    private Integer id;

    /**
     * NFT ID
     */
    private Long tokenId;

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
     * 领导钱包地址
     */
    private String leaderWalletAddress;
    /**
     * 推荐类型
     */
    private String recommendType;
    /**
     * 日期
     */
    private String date;
    /**
     * nft对应奖励的数量
     */
    private String number;
    /**
     * 转入时间
     */
    private String transferTime;
}

package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class NftMessageTotalDTO {

    /**
     * 钱包地址
     */
    private String address;
    /**
     * nft
     */
    private Long tokenId;

    /**
     * nft图片
     */
    private String url;
    /**
     * 购买或转入时间
     */
    private String time;
    /**
     * 购买或转入天数
     */
    private Integer day;
    /**
     * nft等级
     */
    private String type;
    /**
     * 挖矿奖励
     */
    private String rewardTotalNumber;
}

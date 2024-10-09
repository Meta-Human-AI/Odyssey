package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class RewardDistributionDTO {

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
}

package com.example.odyssey.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendListDTO {
    /**
     * 钱包地址
     */
    private String walletAddress;


    private List<RecommendListDTO> recommendList;


    private Integer count = 0;


    private String odsRewardNumber = "0";

    private String usdtRewardNumber = "0";
}

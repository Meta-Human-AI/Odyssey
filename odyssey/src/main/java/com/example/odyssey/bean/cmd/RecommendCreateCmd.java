package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RecommendCreateCmd {

    /**
     * 钱包地址
     */
    private String walletAddress;
    /**
     * 推荐码
     */
    private String recommendCore;
}

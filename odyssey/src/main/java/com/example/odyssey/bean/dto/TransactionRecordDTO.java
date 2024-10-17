package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class TransactionRecordDTO {

    private Integer id;
    /**
     * 地址
     */
    private String walletAddress;
    /**
     * 时间
     */
    private String time;
    /**
     * 动作
     */
    private String action;

    private String actionName;
    /**
     * 等级
     */
    private String type;
    /**
     * nft
     */
    private Long tokenId;
    /**
     * 区块高度
     */
    private Long blockNumber;
    /**
     * 交易
     */
    private String transactionHash;
    /**
     * 日志
     */
    private String logIndex;
}
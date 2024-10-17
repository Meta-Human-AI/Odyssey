package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class TransactionRecordListQryCmd extends PageQuery {

    private String walletAddress;
    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 动作
     */
    private String action;

    /**
     * 等级
     */
    private String type;
    /**
     * nft
     */
    private Long tokenId;
    /**
     * 交易
     */
    private String transactionHash;
}

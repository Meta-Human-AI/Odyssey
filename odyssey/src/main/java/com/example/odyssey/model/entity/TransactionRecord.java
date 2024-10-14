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
@TableName("transaction_record")
public class TransactionRecord {

    @TableId(type = IdType.INPUT)
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

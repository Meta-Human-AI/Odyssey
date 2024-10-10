package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class BscScanTransaction {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String blockNumber;

    private String blockHash;

    private String timeStamp;

    private String hash;

    private String nonce;

    private String transactionIndex;

    private String from;

    private String to;

    private String value;

    private String gas;

    private String gasPrice;

    private String input;

    private String decodedInput;

    private String methodId;

    private String functionName;

    private String contractAddress;

    private String isError;

    private String cumulativeGasUsed;

    private String gasUsed;

    private String confirmations;

    private String receiptStatus;


}

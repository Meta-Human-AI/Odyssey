package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class BscScanTransactionLog {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String transactionHash;

    private String transactionIndex;

    private String blockHash;

    private String blockNumber;

    private Long decodedBlockNumber;

    private String address;

    @TableField("`data`")
    private String data;

    private String topics;

    private String decodedTopics;

    private String logIndex;

    private String timeStamp;

    private String gasPrice;

    private String gasUsed;
}

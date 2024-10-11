package com.example.odyssey.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class BscScanTransactionLogDTO {

    private String transactionHash;

    private String transactionIndex;

    private String blockHash;

    private String blockNumber;

    private String address;

    private String data;

    private List<String> topics;

    private String logIndex;

    private String timeStamp;


    private String gasPrice;

    private String gasUsed;
}

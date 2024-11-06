package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RecommendListQryCmd {

    private String walletAddress;

    private String recommendType;
}

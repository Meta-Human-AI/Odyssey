package com.example.odyssey.bean.cmd;

import lombok.Data;

import java.util.List;

@Data
public class RewardDistributionIssuedCmd {


    private Integer id;

    private List<Integer> ids;

    private String status;
}

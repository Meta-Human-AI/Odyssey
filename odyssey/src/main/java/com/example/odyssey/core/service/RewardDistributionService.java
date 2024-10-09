package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RewardDistributionCmd;


public interface RewardDistributionService {

    /**
     * 奖励发放
     */
    SingleResponse rewardDistribution(RewardDistributionCmd rewardDistributionCmd);
}

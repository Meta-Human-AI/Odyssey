package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RewardDistributionCmd;
import com.example.odyssey.bean.cmd.RewardDistributionIssuedCmd;
import com.example.odyssey.bean.cmd.RewardDistributionListQryCmd;
import com.example.odyssey.bean.cmd.RewardDistributionTotalQryCmd;
import com.example.odyssey.bean.dto.RewardDistributionDTO;
import com.example.odyssey.bean.dto.RewardDistributionTotalDTO;


public interface RewardDistributionService {


    MultiResponse<RewardDistributionDTO> listRewardDistribution(RewardDistributionListQryCmd rewardDistributionListQryCmd);


    SingleResponse issuedRewardDistribution(RewardDistributionIssuedCmd rewardDistributionIssuedCmd);


    MultiResponse<RewardDistributionTotalDTO> rewardDistributionTotal(RewardDistributionTotalQryCmd rewardDistributionTotalQryCmd);

}

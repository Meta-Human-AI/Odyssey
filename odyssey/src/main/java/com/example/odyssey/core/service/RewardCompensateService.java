package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsCompensateCmd;
import com.example.odyssey.bean.cmd.RecalculateOdsRewardsByDateRangeCmd;

public interface RewardCompensateService {

     SingleResponse compensateOdsReward(OdsCompensateCmd odsCompensateCmd);


     SingleResponse recalculateAllOdsRewards();

     SingleResponse recalculateOdsRewardsByDateRange(RecalculateOdsRewardsByDateRangeCmd recalculateOdsRewardsByDateRangeCmd);
}

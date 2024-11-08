package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsCompensateCmd;

public interface RewardCompensateService {

    public SingleResponse compensateOdsReward(OdsCompensateCmd odsCompensateCmd);
}

package com.example.odyssey.api.admin;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsCompensateCmd;
import com.example.odyssey.core.service.RewardCompensateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/reward/compensate")
@Slf4j
public class AdminRewardCompensateController {

    @Resource
    RewardCompensateService rewardCompensateService;

    /**
     * ODS奖励补偿
     */
    @PostMapping("/ods")
    public SingleResponse compensateOdsReward(@RequestBody OdsCompensateCmd odsCompensateCmd) {

        return rewardCompensateService.compensateOdsReward(odsCompensateCmd);
    }
}

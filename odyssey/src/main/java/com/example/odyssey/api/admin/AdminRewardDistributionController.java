package com.example.odyssey.api.admin;

import com.example.odyssey.core.scheduled.RewardDistributionScheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/reward/distribution")
public class AdminRewardDistributionController {

    @Resource
    RewardDistributionScheduled  rewardDistributionScheduled;

    @RequestMapping("/ods/start")
    public void rewardDistribution() {
        rewardDistributionScheduled.odsRewardDistribution();
    }
}

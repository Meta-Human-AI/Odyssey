package com.example.odyssey.api.admin;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsCompensateCmd;
import com.example.odyssey.bean.cmd.RecalculateOdsRewardsByDateRangeCmd;
import com.example.odyssey.core.service.RewardCompensateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    /**
     * 重新计算所有ODS奖励
     */
    @GetMapping("/recalculate/all/ods/rewards")
    SingleResponse recalculateAllOdsRewards(){
        return rewardCompensateService.recalculateAllOdsRewards();
    }

    /**
     * 重新计算指定日期范围内的ODS奖励
     */
    @PostMapping("/recalculate/ods/rewards/by/date/range")
    SingleResponse recalculateOdsRewardsByDateRange(@RequestBody RecalculateOdsRewardsByDateRangeCmd recalculateOdsRewardsByDateRangeCmd){
        return rewardCompensateService.recalculateOdsRewardsByDateRange(recalculateOdsRewardsByDateRangeCmd);
    }
}

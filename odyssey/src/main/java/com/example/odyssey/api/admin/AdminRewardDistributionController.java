package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RewardDistributionIssuedCmd;
import com.example.odyssey.bean.cmd.RewardDistributionListQryCmd;
import com.example.odyssey.bean.cmd.RewardDistributionTotalQryCmd;
import com.example.odyssey.bean.dto.RewardDistributionDTO;
import com.example.odyssey.bean.dto.RewardDistributionTotalDTO;
import com.example.odyssey.core.scheduled.RewardDistributionScheduled;
import com.example.odyssey.core.service.RewardDistributionService;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/reward/distribution")
public class AdminRewardDistributionController {

    @Resource
    RewardDistributionService rewardDistributionService;

    @Resource
    RewardDistributionScheduled rewardDistributionScheduled;

    @GetMapping("/ods/start")
    public void rewardDistribution() {
        rewardDistributionScheduled.odsRewardDistribution();
    }

    /**
     * 查询奖励分配列表
     *
     * @param rewardDistributionListQryCmd
     * @return
     */
    @PostMapping("/list")
    public MultiResponse<RewardDistributionDTO> listRewardDistribution(@RequestBody RewardDistributionListQryCmd rewardDistributionListQryCmd) {
        return rewardDistributionService.listRewardDistribution(rewardDistributionListQryCmd);
    }

    /**
     * 发放奖励分配
     *
     * @param rewardDistributionIssuedCmd
     * @return
     */
    @PostMapping("/issued")
    public SingleResponse issuedRewardDistribution(@RequestBody RewardDistributionIssuedCmd rewardDistributionIssuedCmd) {

        Assert.notNull(rewardDistributionIssuedCmd.getStatus(), "奖励状态不能为空");

        return rewardDistributionService.issuedRewardDistribution(rewardDistributionIssuedCmd);
    }


    /**
     * 查询奖励分配总数
     *
     * @param rewardDistributionTotalQryCmd
     * @return
     */
    @PostMapping("/total")
    public MultiResponse<RewardDistributionTotalDTO> rewardDistributionTotal(@RequestBody RewardDistributionTotalQryCmd rewardDistributionTotalQryCmd){
        return rewardDistributionService.rewardDistributionTotal(rewardDistributionTotalQryCmd);
    }

}

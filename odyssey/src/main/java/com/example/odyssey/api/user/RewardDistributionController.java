package com.example.odyssey.api.user;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.RewardDistributionListQryCmd;
import com.example.odyssey.bean.cmd.RewardDistributionTotalQryCmd;
import com.example.odyssey.bean.dto.RewardDistributionDTO;
import com.example.odyssey.bean.dto.RewardDistributionTotalDTO;
import com.example.odyssey.core.service.RewardDistributionService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/reward/distribution")
public class RewardDistributionController {

    @Resource
    RewardDistributionService rewardDistributionService;

    /**
     * 查询奖励分配列表
     *
     * @param rewardDistributionListQryCmd
     * @return
     */
    @RequestMapping("/list")
    public MultiResponse<RewardDistributionDTO> listRewardDistribution(@RequestBody RewardDistributionListQryCmd rewardDistributionListQryCmd) {
        Assert.notNull(rewardDistributionListQryCmd.getWalletAddress(), "钱包地址不能为空");
        return rewardDistributionService.listRewardDistribution(rewardDistributionListQryCmd);
    }
    /**
     * 查询奖励分配总数
     *
     * @param rewardDistributionTotalQryCmd
     * @return
     */
    @RequestMapping("/total")
    public MultiResponse<RewardDistributionTotalDTO> rewardDistributionTotal(@RequestBody RewardDistributionTotalQryCmd rewardDistributionTotalQryCmd){

        Assert.notNull(rewardDistributionTotalQryCmd.getAddress(), "钱包地址不能为空");
        return rewardDistributionService.rewardDistributionTotal(rewardDistributionTotalQryCmd);
    }

}

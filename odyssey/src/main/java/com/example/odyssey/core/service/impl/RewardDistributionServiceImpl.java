package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RewardDistributionCmd;
import com.example.odyssey.bean.dto.RewardDistributionDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.core.service.RewardDistributionService;
import com.example.odyssey.model.entity.OdsConfig;
import com.example.odyssey.model.entity.RewardDistributionRecord;
import com.example.odyssey.model.mapper.OdsConfigMapper;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import com.example.odyssey.model.mapper.RewardDistributionRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@Transactional
public class RewardDistributionServiceImpl implements RewardDistributionService {

    @Resource
    OdsConfigMapper odsConfigMapper;
    @Resource
    RebateConfigMapper rebateConfigMapper;
    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;

    @Override
    public SingleResponse rewardDistribution(RewardDistributionCmd rewardDistributionCmd) {
        //计算每个等级 每人获取的ods数量 再根据返佣比例计算返佣数量
        rewardDistributionCmd.getList().stream().collect(Collectors.groupingBy(RewardDistributionDTO::getType))
                .forEach((type, list) -> {
                    QueryWrapper<OdsConfig> odsConfigQueryWrapper = new QueryWrapper();
                    odsConfigQueryWrapper.eq("type", type);

                    OdsConfig odsConfig = odsConfigMapper.selectOne(odsConfigQueryWrapper);
                    if (odsConfig == null) {
                        return;
                    }
                    //计算每个等级总数
                    Integer typeSum = list.stream().map(RewardDistributionDTO::getNumber).mapToInt(Integer::intValue).sum();
                    //计算每个等级获取的ods数量
                    BigDecimal number = BigDecimal.valueOf(odsConfig.getNumber()).divide(BigDecimal.valueOf(typeSum), 2, BigDecimal.ROUND_DOWN);

                    for (RewardDistributionDTO rewardDistributionDTO : list) {

                        RewardDistributionRecord rewardDistributionRecord = new RewardDistributionRecord();
                        rewardDistributionRecord.setWalletAddress(rewardDistributionDTO.getWalletAddress());
                        rewardDistributionRecord.setRewardNumber(number.multiply(BigDecimal.valueOf(rewardDistributionDTO.getNumber())).toString());
                        rewardDistributionRecord.setType(type);
                        rewardDistributionRecord.setNumber(rewardDistributionDTO.getNumber());
                        rewardDistributionRecord.setRewardType(RebateEnum.ODS.getCode());
                        rewardDistributionRecord.setCreateTime(System.currentTimeMillis());
                        rewardDistributionRecord.setRewardStatus(RewardDistributionStatusEnum.UNISSUED.getCode());
                        rewardDistributionRecordMapper.insert(rewardDistributionRecord);
                    }

                });

        return null;
    }


}

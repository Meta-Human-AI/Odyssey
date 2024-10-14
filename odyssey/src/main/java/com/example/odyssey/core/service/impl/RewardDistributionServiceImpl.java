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



}

package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RewardDistributionCmd;
import com.example.odyssey.bean.cmd.RewardDistributionIssuedCmd;
import com.example.odyssey.bean.cmd.RewardDistributionListQryCmd;
import com.example.odyssey.bean.cmd.RewardDistributionTotalQryCmd;
import com.example.odyssey.bean.dto.RewardDistributionDTO;
import com.example.odyssey.bean.dto.RewardDistributionTotalDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.core.service.RewardDistributionService;
import com.example.odyssey.model.entity.OdsConfig;
import com.example.odyssey.model.entity.RewardDistributionRecord;
import com.example.odyssey.model.mapper.OdsConfigMapper;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import com.example.odyssey.model.mapper.RewardDistributionRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class RewardDistributionServiceImpl implements RewardDistributionService {
    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;


    @Override
    public MultiResponse<RewardDistributionDTO> listRewardDistribution(RewardDistributionListQryCmd rewardDistributionListQryCmd) {

        QueryWrapper<RewardDistributionRecord> queryWrapper = new QueryWrapper<>();

        if (Objects.nonNull(rewardDistributionListQryCmd.getRewardStatus())) {
            queryWrapper.eq("reward_status", rewardDistributionListQryCmd.getRewardStatus());
        }

        if (Objects.nonNull(rewardDistributionListQryCmd.getWalletAddress())) {
            queryWrapper.eq("wallet_address", rewardDistributionListQryCmd.getWalletAddress());
        }

        if (Objects.nonNull(rewardDistributionListQryCmd.getRelationAddress())) {
            queryWrapper.eq("relation_address", rewardDistributionListQryCmd.getRelationAddress());
        }

        if (Objects.nonNull(rewardDistributionListQryCmd.getTokenId())) {
            queryWrapper.eq("token_id", rewardDistributionListQryCmd.getTokenId());
        }

        if (Objects.nonNull(rewardDistributionListQryCmd.getRewardType())) {
            queryWrapper.eq("reward_type", rewardDistributionListQryCmd.getRewardType());
        }

        List<RewardDistributionDTO> rewardDistributionDTOList = new ArrayList<>();

        Page<RewardDistributionRecord> rewardDistributionRecordPage = rewardDistributionRecordMapper.selectPage(Page.of(rewardDistributionListQryCmd.getPageNum(), rewardDistributionListQryCmd.getPageSize()), queryWrapper);

        for (RewardDistributionRecord rewardDistributionRecord : rewardDistributionRecordPage.getRecords()) {
            RewardDistributionDTO rewardDistributionDTO = new RewardDistributionDTO();
            BeanUtils.copyProperties(rewardDistributionRecord, rewardDistributionDTO);

            rewardDistributionDTOList.add(rewardDistributionDTO);
        }

        return MultiResponse.of(rewardDistributionDTOList, (int) rewardDistributionRecordPage.getTotal());
    }

    @Override
    public SingleResponse issuedRewardDistribution(RewardDistributionIssuedCmd rewardDistributionIssuedCmd) {

        //todo 是运维人员自己发放吗

        RewardDistributionRecord rewardDistributionRecord = rewardDistributionRecordMapper.selectById(rewardDistributionIssuedCmd.getId());

        if (Objects.isNull(rewardDistributionRecord)) {
            return SingleResponse.buildFailure("奖励分配记录不存在");
        }

        if (RewardDistributionStatusEnum.ISSUED.getCode().equals(rewardDistributionRecord.getRewardStatus())) {
            return SingleResponse.buildFailure("奖励已发放");
        }

        rewardDistributionRecord.setRewardStatus(RewardDistributionStatusEnum.ISSUED.getCode());
        rewardDistributionRecordMapper.updateById(rewardDistributionRecord);
        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<RewardDistributionTotalDTO> rewardDistributionTotal(RewardDistributionTotalQryCmd rewardDistributionTotalQryCmd) {


        //todo 1.查询奖励分配记录
        QueryWrapper<RewardDistributionRecord> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(rewardDistributionTotalQryCmd.getAddress())) {
            queryWrapper.eq("wallet_address", rewardDistributionTotalQryCmd.getAddress());
        }

        List<RewardDistributionRecord> rewardDistributionRecordList = rewardDistributionRecordMapper.selectList(queryWrapper);

        BigDecimal odsTotalNumber = rewardDistributionRecordList.stream()
                .filter(rewardDistributionRecord -> RebateEnum.ODS.getCode().equals(rewardDistributionRecord.getRewardType()))
                .map(RewardDistributionRecord::getRewardNumber)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal usdtTotalNumber = rewardDistributionRecordList.stream()
                .filter(rewardDistributionRecord -> RebateEnum.USDT.getCode().equals(rewardDistributionRecord.getRewardType()))
                .map(RewardDistributionRecord::getRewardNumber)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal odsIssuedNumber = rewardDistributionRecordList.stream()
                .filter(rewardDistributionRecord -> RebateEnum.ODS.getCode().equals(rewardDistributionRecord.getRewardType()))
                .filter(rewardDistributionRecord -> RewardDistributionStatusEnum.ISSUED.getCode().equals(rewardDistributionRecord.getRewardStatus()))
                .map(RewardDistributionRecord::getRewardNumber)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal usdtIssuedNumber = rewardDistributionRecordList.stream()
                .filter(rewardDistributionRecord -> RebateEnum.USDT.getCode().equals(rewardDistributionRecord.getRewardType()))
                .filter(rewardDistributionRecord -> RewardDistributionStatusEnum.ISSUED.getCode().equals(rewardDistributionRecord.getRewardStatus()))
                .map(RewardDistributionRecord::getRewardNumber)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RewardDistributionTotalDTO odsRewardDistributionTotalDTO = new RewardDistributionTotalDTO();
        odsRewardDistributionTotalDTO.setRewardType(RebateEnum.ODS.getCode());
        odsRewardDistributionTotalDTO.setRewardTotalNumber(odsTotalNumber.toString());
        odsRewardDistributionTotalDTO.setRewardTotalIssuedNumber(odsIssuedNumber.toString());
        odsRewardDistributionTotalDTO.setRewardTotalUnIssuedNumber(odsTotalNumber.subtract(odsIssuedNumber).toString());

        RewardDistributionTotalDTO usdtRewardDistributionTotalDTO = new RewardDistributionTotalDTO();
        usdtRewardDistributionTotalDTO.setRewardType(RebateEnum.USDT.getCode());
        usdtRewardDistributionTotalDTO.setRewardTotalNumber(usdtTotalNumber.toString());
        usdtRewardDistributionTotalDTO.setRewardTotalIssuedNumber(usdtIssuedNumber.toString());
        usdtRewardDistributionTotalDTO.setRewardTotalUnIssuedNumber(usdtTotalNumber.subtract(usdtIssuedNumber).toString());

        List<RewardDistributionTotalDTO> rewardDistributionTotalDTOList = new ArrayList<>();
        rewardDistributionTotalDTOList.add(odsRewardDistributionTotalDTO);
        rewardDistributionTotalDTOList.add(usdtRewardDistributionTotalDTO);

        return MultiResponse.of(rewardDistributionTotalDTOList);

    }
}

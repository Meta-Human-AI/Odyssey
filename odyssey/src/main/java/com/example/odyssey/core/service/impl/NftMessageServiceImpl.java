package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.NftMessageListQryCmd;
import com.example.odyssey.bean.cmd.NftMessageQryCmd;
import com.example.odyssey.bean.cmd.NftMessageTotalCmd;
import com.example.odyssey.bean.cmd.NftMessageTransferCmd;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.bean.dto.NftMessageTotalDTO;
import com.example.odyssey.common.ActionTypeEnum;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.core.scheduled.TransferScheduled;
import com.example.odyssey.core.service.NftMessageService;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.entity.RewardDistributionRecord;
import com.example.odyssey.model.entity.TransactionRecord;
import com.example.odyssey.model.mapper.NftMessageMapper;
import com.example.odyssey.model.mapper.RewardDistributionRecordMapper;
import com.example.odyssey.model.mapper.TransactionRecordMapper;
import com.example.odyssey.util.Web3jUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class NftMessageServiceImpl implements NftMessageService {

    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    TransactionRecordMapper transactionRecordMapper;
    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;
    @Resource
    Web3jUtil web3jUtil;




    @Override
    public SingleResponse<NftMessageDTO> getNftMessage(NftMessageQryCmd nftMessageQryCmd) {

        NftMessageDTO nftMessageDTO = new NftMessageDTO();

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token_id", nftMessageQryCmd.getTokenId());

        NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
        if (Objects.nonNull(nftMessage)) {
            BeanUtils.copyProperties(nftMessage, nftMessageDTO);
            return SingleResponse.of(nftMessageDTO);
        }

        String nftIdToLevel = web3jUtil.getNftIdToLevel(nftMessageQryCmd.getTokenId(), nftMessageQryCmd.getAddress());
        if (Objects.nonNull(nftIdToLevel)) {

            nftMessage = new NftMessage();
            nftMessage.setTokenId(nftMessageQryCmd.getTokenId());
            nftMessage.setType(nftIdToLevel);
            nftMessage.setBlockadeTime(0L);
            nftMessageMapper.insert(nftMessage);

            nftMessageDTO.setTokenId(nftMessageQryCmd.getTokenId());
            nftMessageDTO.setType(nftIdToLevel);
            return SingleResponse.of(nftMessageDTO);
        }

        nftMessageDTO.setTokenId(nftMessageQryCmd.getTokenId());
        nftMessageDTO.setType(null);
        return SingleResponse.of(nftMessageDTO);

    }

    @Override
    public SingleResponse transferNftMessage(NftMessageTransferCmd nftMessageTransferCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token_id", nftMessageTransferCmd.getTokenId());
        NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
        if (Objects.isNull(nftMessage)){
            return SingleResponse.buildFailure("nft不存在");
        }

        if (Objects.nonNull(nftMessageTransferCmd.getOldAddress())){
            nftMessage.setOldAddress(nftMessageTransferCmd.getOldAddress());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getNewAddress())){
            nftMessage.setNewAddress(nftMessageTransferCmd.getNewAddress());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getBugTime())){
            nftMessage.setBuyTime(nftMessageTransferCmd.getBugTime());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getBuyAddress())){
            nftMessage.setBuyAddress(nftMessageTransferCmd.getBuyAddress());
        }

        nftMessageMapper.updateById(nftMessage);
        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<NftMessageDTO> listNftMessage(NftMessageListQryCmd nftMessageListQryCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(nftMessageListQryCmd.getTokenId())){
            queryWrapper.eq("token_id", nftMessageListQryCmd.getTokenId());
        }

        if(Objects.nonNull(nftMessageListQryCmd.getType())){
            queryWrapper.eq("type", nftMessageListQryCmd.getType());
        }

        if(Objects.nonNull(nftMessageListQryCmd.getOldAddress())){
            queryWrapper.eq("old_address", nftMessageListQryCmd.getOldAddress());
        }

        if(Objects.nonNull(nftMessageListQryCmd.getNewAddress())){
            queryWrapper.eq("new_address", nftMessageListQryCmd.getNewAddress());
        }

        if(Objects.nonNull(nftMessageListQryCmd.getBuyAddress())){
            queryWrapper.eq("buy_address", nftMessageListQryCmd.getBuyAddress());
        }

        queryWrapper.orderByAsc("token_id");

        Page<NftMessage> nftMessagePage = nftMessageMapper.selectPage(Page.of(nftMessageListQryCmd.getPageNum(), nftMessageListQryCmd.getPageSize()), queryWrapper);

        List<NftMessageDTO> nftMessageDTOList = new ArrayList<>();

        for (NftMessage nftMessage : nftMessagePage.getRecords()){

            NftMessageDTO nftMessageDTO = new NftMessageDTO();
            BeanUtils.copyProperties(nftMessage,nftMessageDTO);

            nftMessageDTOList.add(nftMessageDTO);
        }
        return MultiResponse.of(nftMessageDTOList, (int) nftMessagePage.getTotal());
    }

    @Override
    public MultiResponse<NftMessageTotalDTO> nftMessageTotal(NftMessageTotalCmd nftMessageTotalCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(nftMessageTotalCmd.getAddress())){
            queryWrapper.eq("new_address", nftMessageTotalCmd.getAddress());
        }

        List<NftMessage> nftMessageList = nftMessageMapper.selectList(queryWrapper);

        List<NftMessageTotalDTO> nftMessageTotalDTOList = new ArrayList<>();

        for (NftMessage nftMessage : nftMessageList){

           //todo 获取最新一次买入或转入的时间
            QueryWrapper<TransactionRecord> transactionRecordQueryWrapper = new QueryWrapper<>();
            transactionRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
            transactionRecordQueryWrapper.eq("wallet_address", nftMessage.getNewAddress());
            transactionRecordQueryWrapper.in("action", Arrays.asList(ActionTypeEnum.BUY.getCode(), ActionTypeEnum.TRANSFER_IN.getCode()));
            transactionRecordQueryWrapper.orderByDesc("time");

            TransactionRecord transactionRecord = transactionRecordMapper.selectOne(transactionRecordQueryWrapper);
            if (Objects.isNull(transactionRecord)){
                continue;
            }


            QueryWrapper<RewardDistributionRecord> rewardDistributionRecordQueryWrapper = new QueryWrapper<>();
            rewardDistributionRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
            rewardDistributionRecordQueryWrapper.eq("wallet_address", nftMessage.getNewAddress());
            rewardDistributionRecordQueryWrapper.eq("reward_type", RebateEnum.ODS.getCode());
            rewardDistributionRecordQueryWrapper.ge("create_time", transactionRecord.getTime());
            rewardDistributionRecordQueryWrapper.eq("reward_status", RewardDistributionStatusEnum.ISSUED.getCode());


            List<RewardDistributionRecord> rewardDistributionRecordList = rewardDistributionRecordMapper.selectList(rewardDistributionRecordQueryWrapper);

            BigDecimal rewardNumberTotal = rewardDistributionRecordList.stream()
                    .map(RewardDistributionRecord::getRewardNumber)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            NftMessageTotalDTO nftMessageTotalDTO = new NftMessageTotalDTO();
            nftMessageTotalDTO.setTokenId(nftMessage.getTokenId());
            nftMessageTotalDTO.setTime(transactionRecord.getTime());
            nftMessageTotalDTO.setAddress(nftMessage.getNewAddress());
            nftMessageTotalDTO.setDay(rewardDistributionRecordList.size());
            nftMessageTotalDTO.setType(nftMessage.getType());
            nftMessageTotalDTO.setRewardTotalNumber(rewardNumberTotal.toString());

            nftMessageTotalDTOList.add(nftMessageTotalDTO);
        }
        return MultiResponse.of(nftMessageTotalDTOList);
    }
}

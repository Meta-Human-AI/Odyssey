package com.example.odyssey.core.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.NftLevelDTO;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.bean.dto.NftMessageMetadataDTO;
import com.example.odyssey.bean.dto.NftMessageTotalDTO;
import com.example.odyssey.common.*;
import com.example.odyssey.core.scheduled.TransferScheduled;
import com.example.odyssey.core.service.NftMessageService;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import com.example.odyssey.util.AESUtils;
import com.example.odyssey.util.Web3jUtil;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    CityMapper cityMapper;
    @Resource
    ContractAddressMapper contractAddressMapper;

    @Override
    public SingleResponse createNftMessage(NftMessageCreateCmd nftMessageCreateCmd) {

        String data = new String(AESUtils.AESDecode(nftMessageCreateCmd.getData()));

        NftMessageCreateDTO nftMessageCreateDTO = JSONUtil.toBean(data, NftMessageCreateDTO.class);

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token_id", nftMessageCreateDTO.getTokenId());

        NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
        if (Objects.nonNull(nftMessage)) {
            nftMessage.setOldAddress(nftMessage.getNewAddress());
            nftMessage.setNewAddress(nftMessageCreateDTO.getAddress());
            nftMessageMapper.updateById(nftMessage);
        } else {
            QueryWrapper<ContractAddress> contractAddressQueryWrapper = new QueryWrapper<>();
            contractAddressQueryWrapper.last("limit 1");
            ContractAddress contractAddress = contractAddressMapper.selectOne(contractAddressQueryWrapper);

            NftMessageQryCmd nftMessageQryCmd = new NftMessageQryCmd();
            nftMessageQryCmd.setTokenId(nftMessageCreateDTO.getTokenId());
            nftMessageQryCmd.setAddress(contractAddress.getAddress());

            while (true){

                NftMessageDTO nftMessageDTO = getNftMessage(nftMessageQryCmd).getData();
                if (Objects.nonNull(nftMessageDTO.getId())) {
                    nftMessage = nftMessageMapper.selectById(nftMessageDTO.getId());
                    nftMessage.setOldAddress(nftMessageCreateDTO.getAddress());
                    nftMessage.setNewAddress(nftMessageCreateDTO.getAddress());
                    nftMessage.setBuyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    nftMessage.setBuyAddress(nftMessageCreateDTO.getAddress());
                    nftMessageMapper.updateById(nftMessage);
                    break;
                }
            }
        }
        return SingleResponse.buildSuccess();
    }

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

        NftLevelDTO nftIdToLevel = web3jUtil.getNftIdToLevel(nftMessageQryCmd.getTokenId(), nftMessageQryCmd.getAddress());
        if (Objects.nonNull(nftIdToLevel)) {

            nftMessage = new NftMessage();
            nftMessage.setTokenId(nftMessageQryCmd.getTokenId());
            nftMessage.setBlockadeTime(0L);
            nftMessage.setType(nftIdToLevel.getLevel());

            if (nftIdToLevel.getLevel().equals(NftLevelEnum.OA.getName())) {
                nftMessage.setState(nftIdToLevel.getName());
                nftMessage.setCity(0L);
            } else if (nftIdToLevel.getLevel().equals(NftLevelEnum.OB.getName())) {
                nftMessage.setState(0L);
                nftMessage.setCity(nftIdToLevel.getName());
            } else {
                nftMessage.setCity(0L);
                nftMessage.setState(0L);
            }
            nftMessageMapper.insert(nftMessage);

            nftMessageDTO.setTokenId(nftMessageQryCmd.getTokenId());
            nftMessageDTO.setType(nftIdToLevel.getLevel());
            nftMessageDTO.setId(nftMessage.getId());
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
        if (Objects.isNull(nftMessage)) {
            return SingleResponse.buildFailure("nft不存在");
        }

        if (Objects.nonNull(nftMessageTransferCmd.getOldAddress())) {
            nftMessage.setOldAddress(nftMessageTransferCmd.getOldAddress());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getNewAddress())) {
            nftMessage.setNewAddress(nftMessageTransferCmd.getNewAddress());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getBugTime())) {
            nftMessage.setBuyTime(nftMessageTransferCmd.getBugTime());
        }

        if (Objects.nonNull(nftMessageTransferCmd.getBuyAddress())) {
            nftMessage.setBuyAddress(nftMessageTransferCmd.getBuyAddress());
        }

        nftMessageMapper.updateById(nftMessage);
        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<NftMessageDTO> listNftMessage(NftMessageListQryCmd nftMessageListQryCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(nftMessageListQryCmd.getTokenId())) {
            queryWrapper.eq("token_id", nftMessageListQryCmd.getTokenId());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getType())) {
            queryWrapper.eq("type", nftMessageListQryCmd.getType());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getOldAddress())) {
            queryWrapper.eq("old_address", nftMessageListQryCmd.getOldAddress());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getNewAddress())) {
            queryWrapper.eq("new_address", nftMessageListQryCmd.getNewAddress());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getBuyAddress())) {
            queryWrapper.eq("buy_address", nftMessageListQryCmd.getBuyAddress());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getState())) {
            queryWrapper.eq("state", nftMessageListQryCmd.getState());
        }

        if (Objects.nonNull(nftMessageListQryCmd.getCity())) {
            queryWrapper.eq("city", nftMessageListQryCmd.getCity());
        }

        queryWrapper.orderByAsc("token_id");

        Page<NftMessage> nftMessagePage = nftMessageMapper.selectPage(Page.of(nftMessageListQryCmd.getPageNum(), nftMessageListQryCmd.getPageSize()), queryWrapper);

        if (nftMessagePage.getRecords().isEmpty()) {
            return MultiResponse.of(new ArrayList<>());
        }

        List<Long> cityIds = nftMessagePage.getRecords().stream().map(NftMessage::getCity).filter(city -> city > 0).collect(Collectors.toList());

        Map<Long, String> cityMap = new HashMap<>();

        if (!Collections.isEmpty(cityIds)) {
            QueryWrapper<City> cityQueryWrapper = new QueryWrapper<>();
            cityQueryWrapper.in("code", cityIds);
            List<City> cities = cityMapper.selectList(cityQueryWrapper);

            cityMap = cities.stream().collect(Collectors.toMap(City::getCode, City::getName));
        }

        List<NftMessageDTO> nftMessageDTOList = new ArrayList<>();

        for (NftMessage nftMessage : nftMessagePage.getRecords()) {

            NftMessageDTO nftMessageDTO = new NftMessageDTO();
            BeanUtils.copyProperties(nftMessage, nftMessageDTO);
            StateEnum stateEnum = StateEnum.of(nftMessage.getState());

            if (Objects.nonNull(stateEnum)) {
                nftMessageDTO.setState(stateEnum.getName());
            }
            nftMessageDTO.setCity(cityMap.get(nftMessage.getCity()));
            nftMessageDTOList.add(nftMessageDTO);
        }
        return MultiResponse.of(nftMessageDTOList, (int) nftMessagePage.getTotal());
    }

    @Override
    public MultiResponse<NftMessageTotalDTO> nftMessageTotal(NftMessageTotalCmd nftMessageTotalCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(nftMessageTotalCmd.getAddress())) {
            queryWrapper.eq("new_address", nftMessageTotalCmd.getAddress());
        }

        List<NftMessage> nftMessageList = nftMessageMapper.selectList(queryWrapper);

        List<NftMessageTotalDTO> nftMessageTotalDTOList = new ArrayList<>();

        for (NftMessage nftMessage : nftMessageList) {

            //todo 获取最新一次买入或转入的时间
            QueryWrapper<TransactionRecord> transactionRecordQueryWrapper = new QueryWrapper<>();
            transactionRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
            transactionRecordQueryWrapper.eq("wallet_address", nftMessage.getNewAddress());
            transactionRecordQueryWrapper.in("action", Arrays.asList(ActionTypeEnum.BUY.getCode(), ActionTypeEnum.TRANSFER_IN.getCode()));
            transactionRecordQueryWrapper.orderByDesc("time");

            TransactionRecord transactionRecord = transactionRecordMapper.selectOne(transactionRecordQueryWrapper);
            if (Objects.isNull(transactionRecord)) {
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
            nftMessageTotalDTO.setUrl(nftMessage.getUrl());
            nftMessageTotalDTOList.add(nftMessageTotalDTO);
        }
        return MultiResponse.of(nftMessageTotalDTOList);
    }

    @Override
    public SingleResponse<NftMessageMetadataDTO> getNftMessageMetadata(NftMessageMetadataQryCmd nftMessageMetadataQryCmd) {

        QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token_id", nftMessageMetadataQryCmd.getTokenId());
        NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
        if (Objects.isNull(nftMessage)) {
            return SingleResponse.buildFailure("nft不存在");
        }

        NftMessageMetadataDTO nftMessageMetadataDTO = new NftMessageMetadataDTO();
        nftMessageMetadataDTO.setName(nftMessage.getType());
        nftMessageMetadataDTO.setTokenId(nftMessage.getTokenId());
        nftMessageMetadataDTO.setAttributes(new ArrayList<>());
        nftMessageMetadataDTO.setImage(nftMessage.getUrl());
        nftMessageMetadataDTO.setDescription("nft描述");
        return SingleResponse.of(nftMessageMetadataDTO);
    }
}

package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.NftMessageQryCmd;
import com.example.odyssey.bean.cmd.NftMessageTransferCmd;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.core.scheduled.TransferScheduled;
import com.example.odyssey.core.service.NftMessageService;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.mapper.NftMessageMapper;
import com.example.odyssey.util.Web3jUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Transactional
public class NftMessageServiceImpl implements NftMessageService {

    @Resource
    NftMessageMapper nftMessageMapper;
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

        nftMessage.setAddress(nftMessageTransferCmd.getTo());
        nftMessageMapper.updateById(nftMessage);
        return SingleResponse.buildSuccess();
    }
}

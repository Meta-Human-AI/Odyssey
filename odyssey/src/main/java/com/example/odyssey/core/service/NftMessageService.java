package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.NftMessageListQryCmd;
import com.example.odyssey.bean.cmd.NftMessageQryCmd;
import com.example.odyssey.bean.cmd.NftMessageTransferCmd;
import com.example.odyssey.bean.dto.NftMessageDTO;

public interface NftMessageService {


    SingleResponse<NftMessageDTO> getNftMessage(NftMessageQryCmd nftMessageQryCmd);

    SingleResponse transferNftMessage(NftMessageTransferCmd nftMessageTransferCmd);

    MultiResponse<NftMessageDTO> listNftMessage(NftMessageListQryCmd nftMessageListQryCmd);
}

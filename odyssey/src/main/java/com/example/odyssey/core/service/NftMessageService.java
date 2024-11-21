package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.bean.dto.NftMessageMetadataDTO;
import com.example.odyssey.bean.dto.NftMessageTotalDTO;
import com.example.odyssey.bean.dto.NftStatisticsDTO;

public interface NftMessageService {


    SingleResponse createNftMessage(NftMessageCreateCmd nftMessageCreateCmd);

    SingleResponse<NftMessageDTO> getNftMessage(NftMessageQryCmd nftMessageQryCmd);

    SingleResponse transferNftMessage(NftMessageTransferCmd nftMessageTransferCmd);

    MultiResponse<NftMessageDTO> listNftMessage(NftMessageListQryCmd nftMessageListQryCmd);

    MultiResponse<NftMessageTotalDTO> nftMessageTotal(NftMessageTotalCmd nftMessageTotalCmd);


    SingleResponse<NftMessageMetadataDTO> getNftMessageMetadata(NftMessageMetadataQryCmd nftMessageMetadataQryCmd);


    MultiResponse<NftStatisticsDTO> nftStatisticsList(NftStatisticsListQryCmd nftStatisticsListQryCmd);
}

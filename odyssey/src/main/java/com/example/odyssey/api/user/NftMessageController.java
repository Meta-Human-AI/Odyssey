package com.example.odyssey.api.user;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.NftMessageCreateCmd;
import com.example.odyssey.bean.cmd.NftMessageMetadataQryCmd;
import com.example.odyssey.bean.cmd.NftMessageTotalCmd;
import com.example.odyssey.bean.dto.NftMessageMetadataDTO;
import com.example.odyssey.bean.dto.NftMessageTotalDTO;
import com.example.odyssey.core.service.NftMessageService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/nft")
public class NftMessageController {

    @Resource
    private NftMessageService nftMessageService;

    @PostMapping("/total")
    MultiResponse<NftMessageTotalDTO> nftMessageTotal(@RequestBody NftMessageTotalCmd nftMessageTotalCmd) {
        Assert.notNull(nftMessageTotalCmd.getAddress(), "钱包地址不能为空");
        return nftMessageService.nftMessageTotal(nftMessageTotalCmd);
    }

    @PostMapping("/buy")
    SingleResponse createNftMessage(@RequestBody NftMessageCreateCmd nftMessageCreateCmd) {
        Assert.notNull(nftMessageCreateCmd.getData(),"Data 不能为空");
        return nftMessageService.createNftMessage(nftMessageCreateCmd);
    }

    @GetMapping("/info")
    NftMessageMetadataDTO getNftMessage(@RequestParam Long tokenId) {
        NftMessageMetadataQryCmd nftMessageMetadataQryCmd = new NftMessageMetadataQryCmd();
        nftMessageMetadataQryCmd.setTokenId(tokenId);

        return nftMessageService.getNftMessageMetadata(nftMessageMetadataQryCmd).getData();
    }
}

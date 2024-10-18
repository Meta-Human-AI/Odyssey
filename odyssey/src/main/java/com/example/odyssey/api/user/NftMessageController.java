package com.example.odyssey.api.user;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.NftMessageTotalCmd;
import com.example.odyssey.bean.dto.NftMessageTotalDTO;
import com.example.odyssey.core.service.NftMessageService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

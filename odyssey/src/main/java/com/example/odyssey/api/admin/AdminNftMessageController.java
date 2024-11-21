package com.example.odyssey.api.admin;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.NftMessageListQryCmd;
import com.example.odyssey.bean.cmd.NftStatisticsListQryCmd;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.bean.dto.NftStatisticsDTO;
import com.example.odyssey.core.scheduled.TransferScheduled;
import com.example.odyssey.core.service.NftMessageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/nft")
public class AdminNftMessageController {

    @Resource
    private NftMessageService nftMessageService;

    @Resource
    TransferScheduled transferScheduled;


    @GetMapping("/transfer")
    public void transfer() {
        transferScheduled.transfer();
    }

    /**
     * nft 列表
     * @param nftMessageListQryCmd
     * @return
     */
    @PostMapping("/page")
    MultiResponse<NftMessageDTO> listNftMessage(@RequestBody NftMessageListQryCmd nftMessageListQryCmd) {
        return nftMessageService.listNftMessage(nftMessageListQryCmd);
    }

    /**
     * nft 统计
     * @param nftStatisticsListQryCmd
     * @return
     */
    @PostMapping("/statistics/page")
    MultiResponse<NftStatisticsDTO> nftStatisticsList(@RequestBody NftStatisticsListQryCmd nftStatisticsListQryCmd){
        return nftMessageService.nftStatisticsList(nftStatisticsListQryCmd);
    }
}

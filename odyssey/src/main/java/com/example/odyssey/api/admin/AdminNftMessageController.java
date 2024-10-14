package com.example.odyssey.api.admin;

import com.example.odyssey.core.scheduled.TransferScheduled;
import com.example.odyssey.core.service.NftMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package com.example.odyssey.api.admin;

import com.example.odyssey.core.service.BscScanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/bsc/scan")
public class AdminBscScanController {

    @Resource
    private BscScanService bscScanService;

    @GetMapping("/transaction/account/record")
    public void transactionAccountRecord() {
        bscScanService.transactionAccountRecord();
    }

    @GetMapping("/transaction/log/record")
    public void transactionLogRecord() {
        bscScanService.transactionLogRecord();
    }
}

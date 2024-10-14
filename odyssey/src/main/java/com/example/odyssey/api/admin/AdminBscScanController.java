package com.example.odyssey.api.admin;

import com.example.odyssey.core.scheduled.BscScanTransactionScheduled;
import com.example.odyssey.core.service.BscScanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/bsc/scan")
public class AdminBscScanController {

    @Resource
    private BscScanTransactionScheduled bscScanTransactionScheduled;

    @GetMapping("/transaction/account/record")
    public void transactionAccountRecord() {
        bscScanTransactionScheduled.transactionAccountRecord();
    }

    @GetMapping("/transaction/log/record")
    public void transactionLogRecord() {
        bscScanTransactionScheduled.transactionLogRecord();
    }
}

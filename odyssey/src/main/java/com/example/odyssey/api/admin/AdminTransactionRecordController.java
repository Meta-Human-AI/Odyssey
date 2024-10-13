package com.example.odyssey.api.admin;

import com.example.odyssey.core.service.TransactionRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/transaction/record")
public class AdminTransactionRecordController {

    @Resource
    TransactionRecordService transactionRecordService;

    @GetMapping("/start")
    public void transactionAccountRecord() {
        transactionRecordService.transactionRecord();
    }
}

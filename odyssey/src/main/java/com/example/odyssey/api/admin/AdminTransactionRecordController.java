package com.example.odyssey.api.admin;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.TransactionRecordListQryCmd;
import com.example.odyssey.bean.dto.TransactionRecordDTO;
import com.example.odyssey.core.scheduled.TransactionRecordScheduled;
import com.example.odyssey.core.service.TransactionRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/transaction/record")
public class AdminTransactionRecordController {

    @Resource
    TransactionRecordService transactionRecordService;

    @Resource
    TransactionRecordScheduled transactionRecordScheduled;

    @GetMapping("/start")
    public void transactionAccountRecord() {
        transactionRecordScheduled.transactionRecord();
    }

    @PostMapping("/list")
    MultiResponse<TransactionRecordDTO> listTransactionRecord(@RequestBody TransactionRecordListQryCmd transactionRecordListQryCmd){
        return transactionRecordService.listTransactionRecord(transactionRecordListQryCmd);
    }


}

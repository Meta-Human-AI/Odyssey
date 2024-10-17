package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.TransactionRecordListQryCmd;
import com.example.odyssey.bean.dto.TransactionRecordDTO;

public interface TransactionRecordService {


    MultiResponse<TransactionRecordDTO> listTransactionRecord(TransactionRecordListQryCmd transactionRecordListQryCmd);
}

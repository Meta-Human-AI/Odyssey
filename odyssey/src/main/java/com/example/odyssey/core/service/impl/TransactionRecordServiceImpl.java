package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.cmd.TransactionRecordListQryCmd;
import com.example.odyssey.bean.dto.TransactionRecordDTO;
import com.example.odyssey.core.scheduled.TransactionRecordScheduled;
import com.example.odyssey.core.service.TransactionRecordService;
import com.example.odyssey.model.entity.TransactionRecord;
import com.example.odyssey.model.mapper.TransactionRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TransactionRecordServiceImpl implements TransactionRecordService {

    @Resource
    TransactionRecordMapper transactionRecordMapper;

    @Override
    public MultiResponse<TransactionRecordDTO> listTransactionRecord(TransactionRecordListQryCmd transactionRecordListQryCmd) {

        QueryWrapper<TransactionRecord> queryWrapper = new QueryWrapper<>();

        if (Objects.nonNull(transactionRecordListQryCmd.getWalletAddress())) {
            queryWrapper.eq("wallet_address", transactionRecordListQryCmd.getWalletAddress());
        }

        if (Objects.nonNull(transactionRecordListQryCmd.getTransactionHash())) {
            queryWrapper.eq("transaction_hash", transactionRecordListQryCmd.getTransactionHash());
        }

        if (Objects.nonNull(transactionRecordListQryCmd.getType())) {
            queryWrapper.eq("type", transactionRecordListQryCmd.getType());
        }

        if (Objects.nonNull(transactionRecordListQryCmd.getStartTime()) && Objects.nonNull(transactionRecordListQryCmd.getEndTime())) {
            queryWrapper.between("time", transactionRecordListQryCmd.getStartTime(), transactionRecordListQryCmd.getEndTime());
        }

        if (Objects.nonNull(transactionRecordListQryCmd.getAction())){
            queryWrapper.eq("action", transactionRecordListQryCmd.getAction());
        }

        Page<TransactionRecord> transactionRecordPage = transactionRecordMapper.selectPage(Page.of(transactionRecordListQryCmd.getPageNum(), transactionRecordListQryCmd.getPageSize()), queryWrapper);

        List<TransactionRecordDTO> transactionRecordDTOList = new ArrayList<>();

        for (TransactionRecord transactionRecord : transactionRecordPage.getRecords()) {

            TransactionRecordDTO transactionRecordDTO = new TransactionRecordDTO();
            BeanUtils.copyProperties(transactionRecord, transactionRecordDTO);

            transactionRecordDTOList.add(transactionRecordDTO);
        }

        return MultiResponse.of(transactionRecordDTOList, (int) transactionRecordPage.getTotal());
    }
}

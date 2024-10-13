package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.common.FunctionTopicEnum;
import com.example.odyssey.model.entity.BscScanAccountTransaction;
import com.example.odyssey.model.entity.BscScanTransactionLog;
import com.example.odyssey.model.entity.ContractAddress;
import com.example.odyssey.model.entity.TransactionRecord;
import com.example.odyssey.model.mapper.BscScanAccountTransactionMapper;
import com.example.odyssey.model.mapper.BscScanTransactionLogMapper;
import com.example.odyssey.model.mapper.TransactionRecordMapper;
import com.example.odyssey.util.TokenTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransactionRecordScheduled {

    @Resource
    BscScanTransactionLogMapper bscScanTransactionLogMapper;

    @Resource
    TransactionRecordMapper transactionRecordMapper;


    public void transactionRecord() {
        log.info("transactionRecord 开始执行");

        Long blockNumber = 0L;

        QueryWrapper<TransactionRecord> transactionRecordQueryWrapper = new QueryWrapper<>();
        transactionRecordQueryWrapper.orderByDesc("block_number");
        transactionRecordQueryWrapper.last("limit 1");

        TransactionRecord transactionRecord = transactionRecordMapper.selectOne(transactionRecordQueryWrapper);
        if (Objects.nonNull(transactionRecord)) {
            blockNumber = transactionRecord.getBlockNumber();
        }

        Integer PAGE = 1;

        while (true) {

            QueryWrapper<BscScanTransactionLog> transactionLogQueryWrapper = new QueryWrapper<>();
            transactionLogQueryWrapper.ge("decoded_block_number", blockNumber);
            transactionLogQueryWrapper.eq("topic", FunctionTopicEnum.TRANSFER.getTopic());
            List<BscScanTransactionLog> scanTransactionLogs = bscScanTransactionLogMapper.selectPage(Page.of(PAGE, 500), new QueryWrapper<>()).getRecords();

            if (CollectionUtils.isEmpty(scanTransactionLogs)) {
                break;
            }

            for (BscScanTransactionLog bscScanTransactionLog : scanTransactionLogs) {

                if (Objects.isNull(bscScanTransactionLog.getTokenId())) {
                    continue;
                }

                String timeStamp = bscScanTransactionLog.getTimeStamp();

                Date date = new Date(Long.parseLong(timeStamp.substring(2), 16)* 1000);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = simpleDateFormat.format(date);

                List<TransactionRecord> transactionRecordList = new ArrayList<>();
                //转出
                QueryWrapper<TransactionRecord> transferOutQueryWrapper = new QueryWrapper<>();
                transferOutQueryWrapper.eq("wallet_address", bscScanTransactionLog.getFrom());
                transferOutQueryWrapper.eq("transaction_hash", bscScanTransactionLog.getTransactionHash());
                transferOutQueryWrapper.eq("log_index", bscScanTransactionLog.getLogIndex());

                TransactionRecord transferOut = transactionRecordMapper.selectOne(transferOutQueryWrapper);
                if (Objects.isNull(transferOut)) {

                    transferOut = new TransactionRecord();
                    transferOut.setTransactionHash(bscScanTransactionLog.getTransactionHash());
                    transferOut.setAction("转出");
                    transferOut.setBlockNumber(bscScanTransactionLog.getDecodedBlockNumber());
                    transferOut.setType(TokenTypeUtil.getType(bscScanTransactionLog.getTokenId()));
                    transferOut.setTokenId(bscScanTransactionLog.getTokenId());
                    transferOut.setLogIndex(bscScanTransactionLog.getLogIndex());
                    transferOut.setWalletAddress(bscScanTransactionLog.getFrom());
                    transferOut.setTime(time);

                    transactionRecordList.add(transferOut);
                }

                //转入
                QueryWrapper<TransactionRecord> transferInQueryWrapper = new QueryWrapper<>();
                transferInQueryWrapper.eq("wallet_address", bscScanTransactionLog.getTo());
                transferInQueryWrapper.eq("transaction_hash", bscScanTransactionLog.getTransactionHash());
                transferInQueryWrapper.eq("log_index", bscScanTransactionLog.getLogIndex());

                TransactionRecord transferIn = transactionRecordMapper.selectOne(transferInQueryWrapper);
                if (Objects.isNull(transferIn)) {

                    transferIn = new TransactionRecord();
                    transferIn.setTransactionHash(bscScanTransactionLog.getTransactionHash());
                    transferIn.setAction("转入");
                    transferIn.setBlockNumber(bscScanTransactionLog.getDecodedBlockNumber());
                    transferIn.setType(TokenTypeUtil.getType(bscScanTransactionLog.getTokenId()));
                    transferIn.setTokenId(bscScanTransactionLog.getTokenId());
                    transferIn.setLogIndex(bscScanTransactionLog.getLogIndex());
                    transferIn.setWalletAddress(bscScanTransactionLog.getTo());
                    transferIn.setTime(time);

                    transactionRecordList.add(transferIn);
                }



                if (CollectionUtils.isEmpty(transactionRecordList)){
                    continue;
                }

                transactionRecordMapper.insertBatchSomeColumn(transactionRecordList);

            }
            PAGE++;
        }

        log.info("transactionRecord 结束执行");

    }


}

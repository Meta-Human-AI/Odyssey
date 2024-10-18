package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.cmd.NftMessageQryCmd;
import com.example.odyssey.bean.cmd.NftMessageTransferCmd;
import com.example.odyssey.bean.dto.NftMessageDTO;
import com.example.odyssey.common.ActionTypeEnum;
import com.example.odyssey.common.FunctionTopicEnum;
import com.example.odyssey.core.service.NftMessageService;
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
import org.springframework.scheduling.annotation.Scheduled;
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
    BscScanAccountTransactionMapper bscScanAccountTransactionMapper;

    @Resource
    BscScanTransactionLogMapper bscScanTransactionLogMapper;

    @Resource
    TransactionRecordMapper transactionRecordMapper;

    @Resource
    NftMessageService nftMessageService;

    @Scheduled(cron = "0 0/15 * * * ?")
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

                NftMessageQryCmd nftMessageQryCmd = new NftMessageQryCmd();
                nftMessageQryCmd.setTokenId(bscScanTransactionLog.getTokenId());
                nftMessageQryCmd.setAddress(bscScanTransactionLog.getAddress());
                NftMessageDTO nftMessage = nftMessageService.getNftMessage(nftMessageQryCmd).getData();

                String timeStamp = bscScanTransactionLog.getTimeStamp();

                Date date = new Date(Long.parseLong(timeStamp.substring(2), 16) * 1000);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = simpleDateFormat.format(date);

                NftMessageTransferCmd nftMessageTransferCmd = new NftMessageTransferCmd();

                //是否是购买
                if (bscScanTransactionLog.getFrom().equals("0x0000000000000000000000000000000000000000")) {
                    QueryWrapper<TransactionRecord> buyQueryWrapper = new QueryWrapper<>();
                    buyQueryWrapper.eq("wallet_address", bscScanTransactionLog.getTo());
                    buyQueryWrapper.eq("transaction_hash", bscScanTransactionLog.getTransactionHash());
                    buyQueryWrapper.eq("log_index", bscScanTransactionLog.getLogIndex());

                    TransactionRecord buy = transactionRecordMapper.selectOne(buyQueryWrapper);
                    if (Objects.nonNull(buy)) {
                        continue;
                    }

                    QueryWrapper<BscScanAccountTransaction> bscScanAccountTransactionQueryWrapper = new QueryWrapper<>();
                    bscScanAccountTransactionQueryWrapper.eq("hash", bscScanTransactionLog.getTransactionHash());

                    BscScanAccountTransaction bscScanAccountTransaction = bscScanAccountTransactionMapper.selectOne(bscScanAccountTransactionQueryWrapper);
                    if (Objects.isNull(bscScanAccountTransaction)) {
                        continue;
                    }

                    if (bscScanAccountTransaction.getMethodId().equals("0x5a0ce362")) {

                        buy = new TransactionRecord();
                        buy.setTransactionHash(bscScanTransactionLog.getTransactionHash());
                        buy.setAction(ActionTypeEnum.BUY.getCode());
                        buy.setActionName(ActionTypeEnum.BUY.getName());
                        buy.setBlockNumber(bscScanTransactionLog.getDecodedBlockNumber());
                        buy.setType(nftMessage.getType());
                        buy.setTokenId(bscScanTransactionLog.getTokenId());
                        buy.setLogIndex(bscScanTransactionLog.getLogIndex());
                        buy.setWalletAddress(bscScanTransactionLog.getTo());
                        buy.setTime(time);

                        transactionRecordMapper.insert(buy);

                        nftMessageTransferCmd.setBugTime(time);
                        nftMessageTransferCmd.setBuyAddress(bscScanTransactionLog.getTo());

                    }

                }


                //转出
                QueryWrapper<TransactionRecord> transferOutQueryWrapper = new QueryWrapper<>();
                transferOutQueryWrapper.eq("wallet_address", bscScanTransactionLog.getFrom());
                transferOutQueryWrapper.eq("transaction_hash", bscScanTransactionLog.getTransactionHash());
                transferOutQueryWrapper.eq("log_index", bscScanTransactionLog.getLogIndex());

                TransactionRecord transferOut = transactionRecordMapper.selectOne(transferOutQueryWrapper);
                if (Objects.isNull(transferOut)) {

                    transferOut = new TransactionRecord();
                    transferOut.setTransactionHash(bscScanTransactionLog.getTransactionHash());
                    transferOut.setAction(ActionTypeEnum.TRANSFER_OUT.getCode());
                    transferOut.setActionName(ActionTypeEnum.TRANSFER_OUT.getName());
                    transferOut.setBlockNumber(bscScanTransactionLog.getDecodedBlockNumber());
                    transferOut.setType(nftMessage.getType());
                    transferOut.setTokenId(bscScanTransactionLog.getTokenId());
                    transferOut.setLogIndex(bscScanTransactionLog.getLogIndex());
                    transferOut.setWalletAddress(bscScanTransactionLog.getFrom());
                    transferOut.setTime(time);

                    transactionRecordMapper.insert(transferOut);
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
                    transferIn.setAction(ActionTypeEnum.TRANSFER_IN.getCode());
                    transferIn.setActionName(ActionTypeEnum.TRANSFER_IN.getName());
                    transferIn.setBlockNumber(bscScanTransactionLog.getDecodedBlockNumber());
                    transferIn.setType(nftMessage.getType());
                    transferIn.setTokenId(bscScanTransactionLog.getTokenId());
                    transferIn.setLogIndex(bscScanTransactionLog.getLogIndex());
                    transferIn.setWalletAddress(bscScanTransactionLog.getTo());
                    transferIn.setTime(time);

                    transactionRecordMapper.insert(transferIn);

                }

                nftMessageTransferCmd.setTokenId(bscScanTransactionLog.getTokenId());
                nftMessageTransferCmd.setNewAddress(bscScanTransactionLog.getTo());

                if (Objects.nonNull(nftMessageTransferCmd.getBuyAddress())) {
                    nftMessageTransferCmd.setOldAddress(nftMessageTransferCmd.getBuyAddress());
                } else {
                    nftMessageTransferCmd.setOldAddress(bscScanTransactionLog.getFrom());
                }
                nftMessageService.transferNftMessage(nftMessageTransferCmd);

            }
            PAGE++;
        }

        log.info("transactionRecord 结束执行");

    }


    @Scheduled(cron = "0 0/5 * * * ?")
    public void supplementType() {

        QueryWrapper<TransactionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("type");

        List<TransactionRecord> transactionRecords = transactionRecordMapper.selectList(queryWrapper);
        for (TransactionRecord transactionRecord : transactionRecords) {

            QueryWrapper<BscScanTransactionLog> bscScanTransactionLogQueryWrapper = new QueryWrapper<>();
            bscScanTransactionLogQueryWrapper.eq("transaction_hash", transactionRecord.getTransactionHash());
            bscScanTransactionLogQueryWrapper.eq("log_index", transactionRecord.getLogIndex());

            BscScanTransactionLog bscScanTransactionLog = bscScanTransactionLogMapper.selectOne(bscScanTransactionLogQueryWrapper);
            if (Objects.isNull(bscScanTransactionLog)) {
                continue;
            }

            NftMessageQryCmd nftMessageQryCmd = new NftMessageQryCmd();
            nftMessageQryCmd.setTokenId(transactionRecord.getTokenId());
            nftMessageQryCmd.setAddress(bscScanTransactionLog.getAddress());
            NftMessageDTO nftMessage = nftMessageService.getNftMessage(nftMessageQryCmd).getData();

            if (Objects.isNull(nftMessage.getType())) {
                continue;
            }

            transactionRecord.setType(nftMessage.getType());
            transactionRecordMapper.updateById(transactionRecord);
        }
    }


}

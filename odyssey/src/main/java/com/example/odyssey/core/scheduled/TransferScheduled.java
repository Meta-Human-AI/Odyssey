package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.cmd.NftMessageTransferCmd;
import com.example.odyssey.core.service.NftMessageService;
import com.example.odyssey.model.entity.TransactionRecord;
import com.example.odyssey.model.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransferScheduled {

    @Resource
    TransactionRecordMapper transactionRecordMapper;

    @Resource
    NftMessageService nftMessageService;

    /**
     * 每天凌晨1点执行 读取日志的时候 记录 旧的地址 这里就不在定时统计了 可能会出现问题 先留者 看看测试结果
     */
    @Scheduled(cron = "0 59 23 * * ?")
    public void transfer() {
        log.info("transfer 开始执行");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startDateTime = LocalDateTime.of(yesterday, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(yesterday, LocalTime.MAX);

        // 1. 查询昨天的交易记录
        //todo 所有的交易必须都在这里
        QueryWrapper<TransactionRecord> transactionRecordQueryWrapper = new QueryWrapper<>();
        transactionRecordQueryWrapper.eq("action", "transferIn");
        transactionRecordQueryWrapper.between("time", startDateTime, endDateTime);
        transactionRecordQueryWrapper.orderByAsc("time");

        List<TransactionRecord> transactionRecords = transactionRecordMapper.selectList(transactionRecordQueryWrapper);
        if (transactionRecords.isEmpty()) {
            log.info("transfer 结束执行，昨天没有交易记录");
            return;
        }

        Map<Long,String> transferMap = new HashMap<>();
        for (TransactionRecord transactionRecord : transactionRecords) {
            transferMap.put(transactionRecord.getTokenId(), transactionRecord.getWalletAddress());
        }

        transferMap.forEach((tokenId, address) -> {

            NftMessageTransferCmd nftMessageTransferCmd = new NftMessageTransferCmd();
            nftMessageTransferCmd.setTokenId(tokenId);
            nftMessageTransferCmd.setOldAddress(address);
            nftMessageService.transferNftMessage(nftMessageTransferCmd);

        });

        log.info("transfer 结束执行");
    }

}

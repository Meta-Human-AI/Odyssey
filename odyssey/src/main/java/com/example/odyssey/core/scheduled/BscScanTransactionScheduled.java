package com.example.odyssey.core.scheduled;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.dto.BscScanAccountTransactionDTO;
import com.example.odyssey.bean.dto.BscScanAccountTransactionResponseDTO;
import com.example.odyssey.bean.dto.BscScanTransactionLogDTO;
import com.example.odyssey.bean.dto.BscScanTransactionLogResponseDTO;
import com.example.odyssey.model.entity.ContractAddress;
import com.example.odyssey.model.entity.BscScanAccountTransaction;
import com.example.odyssey.model.entity.BscScanTransactionLog;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.ContractAddressMapper;
import com.example.odyssey.model.mapper.BscScanAccountTransactionMapper;
import com.example.odyssey.model.mapper.BscScanTransactionLogMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import com.example.odyssey.util.InputDataDecoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class BscScanTransactionScheduled {

    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private ContractAddressMapper contractAddressMapper;
    @Resource
    private BscScanAccountTransactionMapper bscScanAccountTransactionMapper;
    @Resource
    private BscScanTransactionLogMapper bscScanTransactionLogMapper;
    @Value("${bsc.scan.transaction.url}")
    private String url;

    @Scheduled(cron = "0 0/10 * * * ?")
    @SneakyThrows
    public void transactionAccountRecord() {

        log.info("transactionAccountRecord 开始执行");

        QueryWrapper<SystemConfig> systemConfigQueryWrapper = new QueryWrapper<>();
        systemConfigQueryWrapper.eq("`key`", "apikey");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemConfigQueryWrapper);
        if (Objects.isNull(systemConfig)) {
            return;
        }

        List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseDTOList = getBscScanAccountTransactionResponseList(systemConfig);

        //todo 考虑网络因素，和数据量因素，暂时不统一存储，每次请求一次就存储一次
        //todo 可能新项目启动 不会有那么多数据，先留者这个方法
        //saveBscScanAccountTransaction(bscScanAccountTransactionResponseDTOList);

        log.info("transactionAccountRecord 结束执行");

    }

    @Scheduled(cron = "0 0/15 * * * ?")
    @SneakyThrows
    public void transactionLogRecord() {

        log.info("transactionLogRecord 开始执行");

        QueryWrapper<SystemConfig> systemConfigQueryWrapper = new QueryWrapper<>();
        systemConfigQueryWrapper.eq("`key`", "apikey");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemConfigQueryWrapper);
        if (Objects.isNull(systemConfig)) {
            return;
        }

        // 获取合约的交易日志
        List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList = getBscScanTransactionLogResponseList(systemConfig);
        //todo 因为每次只返回1000条数据，需要多次请求，会导致请求超时，所以暂时不统一存储，目前每请求一次就存储一次
        //todo 可能新项目启动 不会有那么多数据，先留者这个方法
//        saveBscScanTransactionLog(bscScanTransactionLogResponseList);

        log.info("transactionLogRecord 结束执行");
    }

    /**
     * 获取合约或nft地址交易记录
     *
     * @return
     */
    @SneakyThrows
    public List<BscScanAccountTransactionResponseDTO> getBscScanAccountTransactionResponseList(SystemConfig systemConfig) {

        List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList = new ArrayList<>();

        Integer PAGE = 1;

        while (true) {

            List<ContractAddress> contractAddressList = contractAddressMapper.selectPage(Page.of(PAGE, 500), new QueryWrapper<>()).getRecords();

            if (CollectionUtils.isEmpty(contractAddressList)) {
                break;
            }

            for (ContractAddress contractAddress : contractAddressList) {

                getBscScanAccountTransactionResponse(contractAddress, systemConfig, bscScanAccountTransactionResponseList);

            }
            PAGE++;
        }

        return bscScanAccountTransactionResponseList;
    }

    @SneakyThrows
    public void getBscScanAccountTransactionResponse(ContractAddress contractAddress, SystemConfig systemConfig, List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList) {

        Long blockNumber = 0L;

        QueryWrapper<BscScanAccountTransaction> transactionQueryWrapper = new QueryWrapper<>();
        transactionQueryWrapper.eq("`to`", contractAddress.getAddress());
        transactionQueryWrapper.orderByDesc("block_number");
        transactionQueryWrapper.last("limit 1");
        BscScanAccountTransaction bscScanAccountTransaction = bscScanAccountTransactionMapper.selectOne(transactionQueryWrapper);

        if (Objects.nonNull(bscScanAccountTransaction)) {
            blockNumber = bscScanAccountTransaction.getBlockNumber();
        }

        while (true) {

            log.info("BscScanAccountTransaction startblock:{}", blockNumber);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("address", contractAddress.getAddress());
            paramMap.put("apikey", systemConfig.getValue());
            paramMap.put("startblock", blockNumber);
            paramMap.put("module", "account");
            paramMap.put("action", "txlist");


            String response = HttpUtil.get(url, paramMap);

            BscScanAccountTransactionResponseDTO bscScanAccountTransactionResponseDTO = JSONUtil.toBean(response, BscScanAccountTransactionResponseDTO.class);

            if (bscScanAccountTransactionResponseDTO.getStatus().equals("0")) {
                log.error("BscScanTransactionLogResponseDTO error:{}", bscScanAccountTransactionResponseDTO.getMessage());
                break;
            }

            if (CollectionUtils.isEmpty(bscScanAccountTransactionResponseDTO.getResult())) {
                break;
            }

            log.info("result:{}", bscScanAccountTransactionResponseDTO.getResult().size());
            saveBscScanAccountTransaction(Collections.singletonList(bscScanAccountTransactionResponseDTO));

//            bscScanAccountTransactionResponseList.add(bscScanAccountTransactionResponseDTO);



            BscScanAccountTransactionDTO bscScanAccountTransactionDTO = bscScanAccountTransactionResponseDTO.getResult().get(bscScanAccountTransactionResponseDTO.getResult().size() - 1);

            blockNumber = Long.valueOf(bscScanAccountTransactionDTO.getBlockNumber());

            //一秒请求一次
            TimeUnit.SECONDS.sleep(1);

            if (bscScanAccountTransactionResponseDTO.getResult().size() < 10000) {
                break;
            }

        }

    }

    /**
     * 存储交易记录
     *
     * @param bscScanAccountTransactionResponseList
     */
    @SneakyThrows
    public void saveBscScanAccountTransaction(List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList) {

        if (CollectionUtils.isEmpty(bscScanAccountTransactionResponseList)) {
            return;
        }

        for (BscScanAccountTransactionResponseDTO bscScanAccountTransactionResponseDTO : bscScanAccountTransactionResponseList) {

            List<BscScanAccountTransaction> bscScanAccountTransactionList = new ArrayList<>();

            List<BscScanAccountTransactionDTO> bscScanAccountTransactionDTOList = bscScanAccountTransactionResponseDTO.getResult();
            if (CollectionUtils.isEmpty(bscScanAccountTransactionDTOList)) {
                continue;
            }
            //批量查询已经存在的交易记录
            List<String> hashList = bscScanAccountTransactionDTOList.stream().map(BscScanAccountTransactionDTO::getHash).collect(Collectors.toList());

            QueryWrapper<BscScanAccountTransaction> transactionQueryWrapper = new QueryWrapper<>();
            transactionQueryWrapper.in("`hash`", hashList);

            Map<String, BscScanAccountTransaction> existHashCollect = bscScanAccountTransactionMapper.selectList(transactionQueryWrapper)
                    .stream()
                    .collect(Collectors.toMap(BscScanAccountTransaction::getHash, Function.identity()));

            for (BscScanAccountTransactionDTO bscScanAccountTransactionDTO : bscScanAccountTransactionDTOList) {

                BscScanAccountTransaction transaction = existHashCollect.get(bscScanAccountTransactionDTO.getHash());
                if (Objects.nonNull(transaction)) {
                    continue;
                }

                BscScanAccountTransaction bscScanAccountTransaction = new BscScanAccountTransaction();
                BeanUtils.copyProperties(bscScanAccountTransactionDTO, bscScanAccountTransaction);
                bscScanAccountTransaction.setBlockNumber(Long.valueOf(bscScanAccountTransactionDTO.getBlockNumber()));
                InputDataDecoderUtil.BscScanAccountTransaction(bscScanAccountTransaction);
                bscScanAccountTransaction.setReceiptStatus(bscScanAccountTransactionDTO.getTxreceipt_status());
                bscScanAccountTransactionList.add(bscScanAccountTransaction);

                if (bscScanAccountTransactionList.size() >= 1000) {
                    log.info("bscScanAccountTransactionList size:{}", bscScanAccountTransactionList.size());
                    bscScanAccountTransactionMapper.insertBatchSomeColumn(bscScanAccountTransactionList);
                    bscScanAccountTransactionList.clear();
                }
            }

            if (CollectionUtils.isEmpty(bscScanAccountTransactionList)) {
                continue;
            }
            bscScanAccountTransactionMapper.insertBatchSomeColumn(bscScanAccountTransactionList);

        }
    }

    /**
     * 获取合约的交易日志 为了获取nft的tokenid
     *
     * @param systemConfig
     * @return
     * @throws InterruptedException
     */
    @SneakyThrows
    public List<BscScanTransactionLogResponseDTO> getBscScanTransactionLogResponseList(SystemConfig systemConfig) {

        List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList = new ArrayList<>();

        Integer PAGE = 1;

        while (true) {

            QueryWrapper<ContractAddress> queryWrapper = new QueryWrapper<>();

            List<ContractAddress> contractAddressList = contractAddressMapper.selectPage(Page.of(PAGE, 500), queryWrapper).getRecords();

            if (CollectionUtils.isEmpty(contractAddressList)) {
                break;
            }

            for (ContractAddress contractAddress : contractAddressList) {

                getBscScanTransactionLogResponse(systemConfig, contractAddress, bscScanTransactionLogResponseList);
            }

            PAGE++;
        }

        return bscScanTransactionLogResponseList;
    }

    @SneakyThrows
    public void getBscScanTransactionLogResponse(SystemConfig systemConfig, ContractAddress contractAddress, List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList) {

        Long fromBlock = 0L;

        QueryWrapper<BscScanTransactionLog> transactionLogQueryWrapper = new QueryWrapper<>();
        transactionLogQueryWrapper.eq("`address`", contractAddress.getAddress());
        transactionLogQueryWrapper.orderByDesc("decoded_block_number");
        transactionLogQueryWrapper.last("limit 1");
        BscScanTransactionLog bscScanTransactionLog = bscScanTransactionLogMapper.selectOne(transactionLogQueryWrapper);

        if (Objects.nonNull(bscScanTransactionLog)) {
            fromBlock = bscScanTransactionLog.getDecodedBlockNumber();
        }

        while (true) {

            log.info("BscScanTransactionLog fromBlock:{}", fromBlock);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("address", contractAddress.getAddress());
            paramMap.put("apikey", systemConfig.getValue());
            paramMap.put("fromBlock", fromBlock);
            paramMap.put("toBlock", "latest");
            paramMap.put("module", "logs");
            paramMap.put("action", "getLogs");
            paramMap.put("topic0", "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

            String response = HttpUtil.get(url, paramMap);

            BscScanTransactionLogResponseDTO bscScanTransactionLogResponseDTO = JSONUtil.toBean(response, BscScanTransactionLogResponseDTO.class);

            if (bscScanTransactionLogResponseDTO.getStatus().equals("0")) {
                log.error("BscScanTransactionLogResponseDTO error:{}", bscScanTransactionLogResponseDTO.getMessage());
                break;
            }

            if (CollectionUtils.isEmpty(bscScanTransactionLogResponseDTO.getResult())) {
                break;
            }

            log.info("result:{}", bscScanTransactionLogResponseDTO.getResult().size());

            saveBscScanTransactionLog(Collections.singletonList(bscScanTransactionLogResponseDTO));

//            bscScanTransactionLogResponseList.add(bscScanTransactionLogResponseDTO);



            BscScanTransactionLogDTO bscScanTransactionLogDTO = bscScanTransactionLogResponseDTO.getResult().get(bscScanTransactionLogResponseDTO.getResult().size() - 1);

            fromBlock = Long.valueOf(bscScanTransactionLogDTO.getBlockNumber().substring(2), 16);

            //一秒请求一次
            TimeUnit.SECONDS.sleep(1);

            if (bscScanTransactionLogResponseDTO.getResult().size() < 1000) {
                break;
            }

        }


    }

    /**
     * 存储合约相关的操作日志
     *
     * @param bscScanTransactionLogResponseList
     */
    @SneakyThrows
    public void saveBscScanTransactionLog(List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList) {

        if (CollectionUtils.isEmpty(bscScanTransactionLogResponseList)) {
            return;
        }

        for (BscScanTransactionLogResponseDTO bscScanTransactionLogResponseDTO : bscScanTransactionLogResponseList) {

            List<BscScanTransactionLogDTO> bscScanTransactionLogResult = bscScanTransactionLogResponseDTO.getResult();
            if (CollectionUtils.isEmpty(bscScanTransactionLogResult)) {
                continue;
            }

            List<BscScanTransactionLog> bscScanTransactionLogList = new ArrayList<>();

            List<String> transactionHashList = bscScanTransactionLogResult.stream().map(BscScanTransactionLogDTO::getTransactionHash).collect(Collectors.toList());


            QueryWrapper<BscScanTransactionLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("transaction_hash", transactionHashList);

            Map<String, BscScanTransactionLog> existTransactionHashCollect = bscScanTransactionLogMapper.selectList(queryWrapper)
                    .stream()
                    .collect(Collectors.toMap(x -> x.getTransactionHash() + ":" + x.getLogIndex(), Function.identity()));

            for (BscScanTransactionLogDTO bscScanTransactionLogDTO : bscScanTransactionLogResult) {


                BscScanTransactionLog transactionLog = existTransactionHashCollect.get(bscScanTransactionLogDTO.getTransactionHash() + ":" + bscScanTransactionLogDTO.getLogIndex());
                if (Objects.nonNull(transactionLog)) {
                    continue;
                }

                BscScanTransactionLog bscScanTransactionLog = new BscScanTransactionLog();
                BeanUtils.copyProperties(bscScanTransactionLogDTO, bscScanTransactionLog);
                bscScanTransactionLog.setTopics(JSONUtil.toJsonStr(bscScanTransactionLogDTO.getTopics()));
                bscScanTransactionLog.setDecodedBlockNumber(Long.parseLong(bscScanTransactionLogDTO.getBlockNumber().substring(2), 16));
                InputDataDecoderUtil.BscScanLogTransaction(bscScanTransactionLog,bscScanTransactionLogDTO.getTopics());

                bscScanTransactionLogList.add(bscScanTransactionLog);

                bscScanTransactionLogMapper.insertBatchSomeColumn(bscScanTransactionLogList);
                bscScanTransactionLogList.clear();
            }

            if (CollectionUtils.isEmpty(bscScanTransactionLogList)) {
                continue;
            }
            bscScanTransactionLogMapper.insertBatchSomeColumn(bscScanTransactionLogList);
        }

    }


}

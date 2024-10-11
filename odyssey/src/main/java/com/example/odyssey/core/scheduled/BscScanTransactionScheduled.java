package com.example.odyssey.core.scheduled;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.dto.BscScanAccountTransactionDTO;
import com.example.odyssey.bean.dto.BscScanAccountTransactionResponseDTO;
import com.example.odyssey.bean.dto.BscScanTransactionLogDTO;
import com.example.odyssey.bean.dto.BscScanTransactionLogResponseDTO;
import com.example.odyssey.common.AddressTypeEnum;
import com.example.odyssey.model.entity.Address;
import com.example.odyssey.model.entity.BscScanAccountTransaction;
import com.example.odyssey.model.entity.BscScanTransactionLog;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.AddressMapper;
import com.example.odyssey.model.mapper.BscScanAccountTransactionMapper;
import com.example.odyssey.model.mapper.BscScanTransactionLogMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import com.example.odyssey.util.InputDataDecoderUtil;
import lombok.RequiredArgsConstructor;
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

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class BscScanTransactionScheduled {

    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private BscScanAccountTransactionMapper bscScanAccountTransactionMapper;
    @Resource
    private BscScanTransactionLogMapper bscScanTransactionLogMapper;
    @Value("${bsc.scan.transaction.url}")
    private String url;

    //    @Scheduled(cron = "0 0/30 * * * ?")
    public void transactionAccountRecord() {

        log.info("transactionAccountRecord 开始执行");

        QueryWrapper<SystemConfig> systemConfigQueryWrapper = new QueryWrapper<>();
        systemConfigQueryWrapper.eq("`key`", "apikey");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemConfigQueryWrapper);
        if (Objects.isNull(systemConfig)) {
            return;
        }

        try {
            // 获取合约或nft地址交易记录
            List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseDTOList = getBscScanAccountTransactionResponseList(systemConfig);
            saveBscScanAccountTransaction(bscScanAccountTransactionResponseDTOList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("transactionAccountRecord 结束执行");

    }

    //    @Scheduled(cron = "0 0/60 * * * ?")
    public void transactionLogRecord() {

        log.info("transactionLogRecord 开始执行");

        QueryWrapper<SystemConfig> systemConfigQueryWrapper = new QueryWrapper<>();
        systemConfigQueryWrapper.eq("`key`", "apikey");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemConfigQueryWrapper);
        if (Objects.isNull(systemConfig)) {
            return;
        }

        try {
            // 获取合约的交易日志
            List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList = getBscScanTransactionLogResponseList(systemConfig);
            saveBscScanTransactionLog(bscScanTransactionLogResponseList);
        } catch (Exception e) {
            e.printStackTrace();

        }

        log.info("transactionLogRecord 结束执行");
    }

    /**
     * 获取合约或nft地址交易记录
     *
     * @return
     */
    private List<BscScanAccountTransactionResponseDTO> getBscScanAccountTransactionResponseList(SystemConfig systemConfig) throws InterruptedException {

        List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList = new ArrayList<>();

        Integer PAGE = 1;

        while (true) {

            List<Address> addressList = addressMapper.selectPage(Page.of(PAGE, 500), new QueryWrapper<>()).getRecords();

            if (CollectionUtils.isEmpty(addressList)) {
                break;
            }

            for (Address address : addressList) {

                getBscScanAccountTransactionResponse(address, systemConfig, bscScanAccountTransactionResponseList);

            }
            PAGE++;
        }

        return bscScanAccountTransactionResponseList;
    }


    private void getBscScanAccountTransactionResponse(Address address, SystemConfig systemConfig, List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList) throws InterruptedException {

        Long blockNumber = 0L;

        QueryWrapper<BscScanAccountTransaction> transactionQueryWrapper = new QueryWrapper<>();
        transactionQueryWrapper.eq("`to`", address.getAddress());
        transactionQueryWrapper.orderByDesc("block_number");
        transactionQueryWrapper.last("limit 1");
        BscScanAccountTransaction bscScanAccountTransaction = bscScanAccountTransactionMapper.selectOne(transactionQueryWrapper);

        if (Objects.nonNull(bscScanAccountTransaction)) {
            blockNumber = bscScanAccountTransaction.getBlockNumber();
        }

        while (true) {

            log.info("BscScanAccountTransaction startblock:{}", blockNumber);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("address", address.getAddress());
            paramMap.put("apikey", systemConfig.getValue());
            paramMap.put("startblock", blockNumber);
            paramMap.put("module", "account");
            paramMap.put("action", "txlist");


            String response = HttpUtil.get(url, paramMap);

            BscScanAccountTransactionResponseDTO bscScanAccountTransactionResponseDTO = JSONUtil.toBean(response, BscScanAccountTransactionResponseDTO.class);

            if (CollectionUtils.isEmpty(bscScanAccountTransactionResponseDTO.getResult())) {
                break;
            }

            log.info("result:{}", bscScanAccountTransactionResponseDTO.getResult().size());
            bscScanAccountTransactionResponseList.add(bscScanAccountTransactionResponseDTO);

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
    private void saveBscScanAccountTransaction(List<BscScanAccountTransactionResponseDTO> bscScanAccountTransactionResponseList) {

        if (CollectionUtils.isEmpty(bscScanAccountTransactionResponseList)) {
            return;
        }

        for (BscScanAccountTransactionResponseDTO bscScanAccountTransactionResponseDTO : bscScanAccountTransactionResponseList) {

            List<BscScanAccountTransaction> bscScanAccountTransactionList = new ArrayList<>();

            List<BscScanAccountTransactionDTO> BscScanAccountTransactionDTOList = bscScanAccountTransactionResponseDTO.getResult();
            if (CollectionUtils.isEmpty(BscScanAccountTransactionDTOList)) {
                continue;
            }
            for (BscScanAccountTransactionDTO bscScanAccountTransactionDTO : BscScanAccountTransactionDTOList) {

                QueryWrapper<BscScanAccountTransaction> transactionQueryWrapper = new QueryWrapper<>();
                transactionQueryWrapper.eq("`hash`", bscScanAccountTransactionDTO.getHash());

                Long count = bscScanAccountTransactionMapper.selectCount(transactionQueryWrapper);
                if (count > 0) {
                    continue;
                }

                BscScanAccountTransaction bscScanAccountTransaction = new BscScanAccountTransaction();
                BeanUtils.copyProperties(bscScanAccountTransactionDTO, bscScanAccountTransaction);
                bscScanAccountTransaction.setBlockNumber(Long.valueOf(bscScanAccountTransactionDTO.getBlockNumber()));
                InputDataDecoderUtil.BscScanAccountTransaction(bscScanAccountTransaction);

                bscScanAccountTransactionList.add(bscScanAccountTransaction);

                if (bscScanAccountTransactionList.size() >= 1000) {
                    bscScanAccountTransactionMapper.insertBatchSomeColumn(bscScanAccountTransactionList);
                    bscScanAccountTransactionList.clear();
                }
            }

        }
    }

    /**
     * 获取合约的交易日志 为了获取nft的tokenid
     *
     * @param systemConfig
     * @return
     * @throws InterruptedException
     */
    private List<BscScanTransactionLogResponseDTO> getBscScanTransactionLogResponseList(SystemConfig systemConfig) throws InterruptedException {

        List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList = new ArrayList<>();

        Integer PAGE = 1;

        while (true) {

            QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type", AddressTypeEnum.CONTRACT.getCode());

            List<Address> addressList = addressMapper.selectPage(Page.of(PAGE, 500), queryWrapper).getRecords();

            if (CollectionUtils.isEmpty(addressList)) {
                break;
            }

            for (Address address : addressList) {

                getBscScanTransactionLogResponse(systemConfig, address, bscScanTransactionLogResponseList);
            }

            PAGE++;
        }

        return bscScanTransactionLogResponseList;
    }

    private void getBscScanTransactionLogResponse(SystemConfig systemConfig, Address address, List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList) throws InterruptedException {

        Long fromBlock = 0L;

        QueryWrapper<BscScanTransactionLog> transactionLogQueryWrapper = new QueryWrapper<>();
        transactionLogQueryWrapper.eq("`address`", address.getAddress());
        transactionLogQueryWrapper.orderByDesc("decoded_block_number");
        transactionLogQueryWrapper.last("limit 1");
        BscScanTransactionLog bscScanTransactionLog = bscScanTransactionLogMapper.selectOne(transactionLogQueryWrapper);

        if (Objects.nonNull(bscScanTransactionLog)) {
            fromBlock = bscScanTransactionLog.getDecodedBlockNumber();
        }

        while (true) {

            log.info("BscScanTransactionLog fromBlock:{}", fromBlock);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("address", address.getAddress());
            paramMap.put("apikey", systemConfig.getValue());
            paramMap.put("fromBlock", fromBlock);
            paramMap.put("module", "logs");
            paramMap.put("action", "getLogs");
            paramMap.put("topic0", "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

            String response = HttpUtil.get(url, paramMap);

            BscScanTransactionLogResponseDTO bscScanTransactionLogResponseDTO = JSONUtil.toBean(response, BscScanTransactionLogResponseDTO.class);

            if (CollectionUtils.isEmpty(bscScanTransactionLogResponseDTO.getResult())) {
                break;
            }

            bscScanTransactionLogResponseList.add(bscScanTransactionLogResponseDTO);

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
    private void saveBscScanTransactionLog(List<BscScanTransactionLogResponseDTO> bscScanTransactionLogResponseList) {

        if (CollectionUtils.isEmpty(bscScanTransactionLogResponseList)) {
            return;
        }

        for (BscScanTransactionLogResponseDTO bscScanTransactionLogResponseDTO : bscScanTransactionLogResponseList) {

            List<BscScanTransactionLogDTO> bscScanTransactionLogResult = bscScanTransactionLogResponseDTO.getResult();
            if (CollectionUtils.isEmpty(bscScanTransactionLogResult)) {
                continue;
            }

            for (BscScanTransactionLogDTO bscScanTransactionLogDTO : bscScanTransactionLogResult) {


                QueryWrapper<BscScanTransactionLog> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("transaction_hash", bscScanTransactionLogDTO.getTransactionHash());

                Long count = bscScanTransactionLogMapper.selectCount(queryWrapper);
                if (count > 0) {
                    continue;
                }

                BscScanTransactionLog bscScanTransactionLog = new BscScanTransactionLog();
                BeanUtils.copyProperties(bscScanTransactionLogDTO, bscScanTransactionLog);
                bscScanTransactionLog.setDecodedTopics(InputDataDecoderUtil.BscScanLogTransaction(bscScanTransactionLogDTO));
                bscScanTransactionLog.setTopics(JSONUtil.toJsonStr(bscScanTransactionLogDTO.getTopics()));
                bscScanTransactionLog.setDecodedBlockNumber(Long.valueOf(bscScanTransactionLogDTO.getBlockNumber().substring(2), 16));
                bscScanTransactionLogMapper.insert(bscScanTransactionLog);

            }

        }

    }


}

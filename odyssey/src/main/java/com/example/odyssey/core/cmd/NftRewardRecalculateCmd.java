package com.example.odyssey.core.cmd;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.dto.NftHoldInfoDTO;
import com.example.odyssey.bean.dto.RewardCalculateResultDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.core.scheduled.RewardDistributionScheduled;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NftRewardRecalculateCmd {
    @Resource
    TransactionRecordMapper transactionRecordMapper;
    @Resource
    OdsConfigMapper odsConfigMapper;
    @Resource
    RecommendMapper recommendMapper;
    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;
    @Resource
    SystemConfigMapper systemConfigMapper;
    @Resource
    RegionRecommendLogMapper regionRecommendLogMapper;
    @Resource
    RewardDistributionScheduled rewardDistributionScheduled;
    @Resource
    RebateConfigMapper rebateConfigMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 全量重新计算ODS奖励
     */
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAllOdsRewards() {
        log.info("开始全量重新计算ODS奖励");
        try {
            // 获取时间范围
            String[] dateRange = getTransactionDateRange();
            if (dateRange == null) return;

            // 调用指定时间范围的重新计算
            recalculateOdsRewardsByDateRange(dateRange[0], dateRange[1]);

        } catch (Exception e) {
            log.error("全量重新计算ODS奖励失败", e);
            throw new RuntimeException("全量重新计算ODS奖励失败", e);
        }
    }

    /**
     * 指定时间范围重新计算ODS奖励
     */
    @Transactional(rollbackFor = Exception.class)
    public void recalculateOdsRewardsByDateRange(String startDate, String endDate) {
        log.info("开始重新计算 {} 至 {} 的ODS奖励", startDate, endDate);

        try {
            // 1. 清理旧记录
            cleanOldRecords(startDate, endDate);

            // 2. 获取配置和交易记录
            Map<String, OdsConfig> configMap = getOdsConfigs();
            validateOdsConfigs(configMap);

            List<TransactionRecord> records = getTransactionRecords(endDate);
            if (records.isEmpty()) {
                log.warn("没有找到交易记录");
                return;
            }

            // 3. 按日期计算并保存奖励
            calculateAndSaveRewardsByDate(startDate, endDate, records, configMap);

            log.info("ODS奖励重新计算完成");
        } catch (Exception e) {
            log.error("重新计算ODS奖励失败", e);
            throw new RuntimeException("重新计算ODS奖励失败", e);
        }
    }

    /**
     * 获取交易记录的时间范围
     */
    private String[] getTransactionDateRange() {
        // 获取第一条记录
        List<TransactionRecord> firstRecord = transactionRecordMapper.selectList(
                new QueryWrapper<TransactionRecord>().orderByAsc("time").last("limit 1")
        );

        if (firstRecord.isEmpty()) {
            log.warn("没有找到交易记录");
            return null;
        }

        String startDate = firstRecord.get(0).getTime().split(" ")[0];

        // 获取最后一条记录
        List<TransactionRecord> lastRecord = transactionRecordMapper.selectList(
                new QueryWrapper<TransactionRecord>().orderByDesc("time").last("limit 1")
        );

        String endDate = lastRecord.get(0).getTime().split(" ")[0];

        return new String[]{startDate, endDate};
    }

    /**
     * 获取NFT等级配置
     */
    private Map<String, OdsConfig> getOdsConfigs() {
        List<OdsConfig> configs = odsConfigMapper.selectList(new QueryWrapper<>());
        return configs.stream().collect(
                Collectors.toMap(OdsConfig::getType, config -> config)
        );
    }

    /**
     * 计算单日奖励
     */
    private List<RewardCalculateResultDTO> calculateDailyRewards(LocalDate calculateDate, List<TransactionRecord> allRecords, Map<String, OdsConfig> configMap) {

        log.info("计算 {} 的奖励", calculateDate);
        List<RewardCalculateResultDTO> results = new ArrayList<>();

        // 1. 确定截止时间（T-2日23:59:59）
        LocalDateTime cutoffTime = calculateDate.minusDays(2).atTime(23, 59, 59);

        // 2. 构建NFT持有状态
        Map<Long, NftHoldInfoDTO> nftHoldMap = buildNftHoldStatus(allRecords, cutoffTime);

        // 3. 按NFT类型分组
        Map<String, List<NftHoldInfoDTO>> typeGroupMap = nftHoldMap.values().stream()
                .filter(info -> isValidHold(info, calculateDate))
                .collect(Collectors.groupingBy(NftHoldInfoDTO::getType));

        // 4. 计算每种类型的奖励
        typeGroupMap.forEach((type, nfts) -> {
            OdsConfig config = configMap.get(type);
            if (config == null) {
                log.warn("未找到{}类型的配置", type);
                return;
            }

            // 计算基础奖励
            BigDecimal baseReward = BigDecimal.valueOf(config.getNumber())
                    .divide(BigDecimal.valueOf(nfts.size()), 8, RoundingMode.HALF_UP);

            // 计算每个NFT的奖励
            nfts.forEach(nft -> results.addAll(calculateNftReward(nft, baseReward, calculateDate)));
        });

        return results;
    }

    /**
     * 构建NFT持有状态
     */
    private Map<Long, NftHoldInfoDTO> buildNftHoldStatus(List<TransactionRecord> records, LocalDateTime cutoffTime) {
        Map<Long, NftHoldInfoDTO> holdMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (TransactionRecord record : records) {
            // 使用formatter解析日期
            if (LocalDateTime.parse(record.getTime(), formatter).isAfter(cutoffTime)) {
                break;
            }

            Long tokenId = record.getTokenId();

            switch (record.getAction()) {
                case "buy":

                case "airdrop":
                    updateHoldStatus(holdMap, record);
                    break;
                case "transferOut":
                    holdMap.remove(tokenId);
                    break;
                case "transferIn":
                    updateHoldStatus(holdMap, record);
                    break;
            }
        }

        return holdMap;
    }

    /**
     * 更新持有状态
     */
    private void updateHoldStatus(Map<Long, NftHoldInfoDTO> holdMap, TransactionRecord record) {
        Recommend recommend = recommendMapper.selectOne(
                new QueryWrapper<Recommend>()
                        .eq("wallet_address", record.getWalletAddress())
        );

        holdMap.put(record.getTokenId(), NftHoldInfoDTO.builder()
                .tokenId(record.getTokenId())
                .type(record.getType())
                .walletAddress(record.getWalletAddress())
                .transferTime(record.getTime())
                .recommend(recommend)
                .build());
    }

    /**
     * 验证NFT持有状态
     */
    private boolean isValidHold(NftHoldInfoDTO info, LocalDate calculateDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime transferTime = LocalDateTime.parse(info.getTransferTime(), formatter);

        LocalDate holdDate = calculateDate.minusDays(1);  // 实际持有日期
        return transferTime.toLocalDate().isBefore(holdDate);
    }

    /**
     * 计算单个NFT的奖励
     */
    private List<RewardCalculateResultDTO> calculateNftReward(NftHoldInfoDTO nft, BigDecimal baseReward, LocalDate calculateDate) {
        List<RewardCalculateResultDTO> results = new ArrayList<>();

        // 使用正确的DateTimeFormatter来解析日期时间
        LocalDateTime transferTime;
        try {
            transferTime = LocalDateTime.parse(nft.getTransferTime(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("日期解析错误: {}", nft.getTransferTime(), e);
            return results;
        }

        // 验证持有时间是否满足要求
        if (transferTime.toLocalDate().isAfter(calculateDate.minusDays(1))) {
            return results;
        }

        String date = calculateDate.toString();

        // 1. 获取系统钱包地址
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("`key`", "official_wallet_address");
        SystemConfig systemConfig = systemConfigMapper.selectOne(queryWrapper);

        // 2. 获取推荐关系
        Recommend recommend = nft.getRecommend();

        // 3. 无推荐关系，12%给系统钱包，88%给持有人
        if (recommend == null) {
            BigDecimal serviceFee = baseReward.multiply(new BigDecimal("0.12"));
            results.add(RewardCalculateResultDTO.builder()
                    .date(date)
                    .tokenId(nft.getTokenId())
                    .type(nft.getType())
                    .walletAddress(systemConfig.getValue())
                    .recommendAddress(nft.getWalletAddress())
                    .amount(serviceFee)
                    .rewardType("SERVICE_FEE")
                    .build());

            results.add(RewardCalculateResultDTO.builder()
                    .date(date)
                    .tokenId(nft.getTokenId())
                    .type(nft.getType())
                    .walletAddress(nft.getWalletAddress())
                    .recommendAddress(nft.getWalletAddress())
                    .amount(baseReward.subtract(serviceFee))
                    .rewardType("HOLD")
                    .build());

            return results;
        }

        // 4. 检查推荐关系时间
        Long timestamp = transferTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (recommend.getRecommendTime() > timestamp) {
            // 推荐关系在NFT获得之后建立，按无推荐关系处理
            BigDecimal serviceFee = baseReward.multiply(new BigDecimal("0.12"));
            results.add(RewardCalculateResultDTO.builder()
                    .date(date)
                    .tokenId(nft.getTokenId())
                    .type(nft.getType())
                    .walletAddress(systemConfig.getValue())
                    .recommendAddress(nft.getWalletAddress())
                    .amount(serviceFee)
                    .rewardType("SERVICE_FEE")
                    .build());

            results.add(RewardCalculateResultDTO.builder()
                    .date(date)
                    .tokenId(nft.getTokenId())
                    .type(nft.getType())
                    .walletAddress(nft.getWalletAddress())
                    .recommendAddress(nft.getWalletAddress())
                    .amount(baseReward.subtract(serviceFee))
                    .rewardType("HOLD")
                    .build());

            return results;
        }

        // 5. 获取返佣配置
        QueryWrapper<RebateConfig> rebateConfigQuery = new QueryWrapper<>();
        rebateConfigQuery.eq("address", recommend.getLeaderWalletAddress())
                .eq("recommend_type", recommend.getRecommendType())
                .eq("rebate_type", RebateEnum.ODS.getCode());
        RebateConfig rebateConfig = rebateConfigMapper.selectOne(rebateConfigQuery);

        // 6. 计算返佣
        if (recommend.getFirstRecommendWalletAddress() != null) {
            if (recommend.getSecondRecommendWalletAddress() == null) {
                // 只有一级推荐
                BigDecimal firstRebate = baseReward.multiply(new BigDecimal(rebateConfig.getFirstRebateRate()));
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(recommend.getFirstRecommendWalletAddress())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(firstRebate)
                        .rewardType("FIRST_RECOMMEND")
                        .build());

                // 持有人获得剩余
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(nft.getWalletAddress())
                        .firstRecommend(recommend.getFirstRecommendWalletAddress())
                        .leaderAddress(recommend.getLeaderWalletAddress())
                        .recommendType(RecommendEnum.NORMAL.getCode())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(baseReward.subtract(firstRebate))
                        .rewardType("HOLD")
                        .build());
            } else {
                // 有二级推荐
                BigDecimal firstRebate = baseReward.multiply(new BigDecimal(rebateConfig.getThreeRebateRate()));
                BigDecimal secondRebate = baseReward.multiply(new BigDecimal(rebateConfig.getSecondRebateRate()));

                // 一级推荐人奖励
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(recommend.getFirstRecommendWalletAddress())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(firstRebate)
                        .rewardType("FIRST_RECOMMEND")
                        .build());

                // 二级推荐人奖励
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(recommend.getSecondRecommendWalletAddress())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(secondRebate)
                        .rewardType("SECOND_RECOMMEND")
                        .build());

                // 持有人获得剩余
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(nft.getWalletAddress())
                        .firstRecommend(recommend.getFirstRecommendWalletAddress())
                        .secondRecommend(recommend.getSecondRecommendWalletAddress())
                        .leaderAddress(recommend.getLeaderWalletAddress())
                        .recommendType(RecommendEnum.NORMAL.getCode())
                        .amount(baseReward.subtract(firstRebate).subtract(secondRebate))
                        .recommendAddress(nft.getWalletAddress())
                        .rewardType("HOLD")
                        .build());
            }
        } else {
            // 队长或无推荐关系
            if (recommend.getRecommendType().equals(RecommendEnum.LEADER.getCode())) {
                // 队长获得全部奖励
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(nft.getWalletAddress())
                        .leaderAddress(nft.getWalletAddress())
                        .recommendType(RecommendEnum.LEADER.getCode())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(baseReward)
                        .rewardType("HOLD")
                        .build());
            } else {
                // 按无推荐关系处理
                BigDecimal serviceFee = baseReward.multiply(new BigDecimal("0.12"));
                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(systemConfig.getValue())
                        .recommendAddress(nft.getWalletAddress())
                        .amount(serviceFee)
                        .rewardType("SERVICE_FEE")
                        .build());

                results.add(RewardCalculateResultDTO.builder()
                        .date(date)
                        .tokenId(nft.getTokenId())
                        .type(nft.getType())
                        .walletAddress(nft.getWalletAddress())
                        .amount(baseReward.subtract(serviceFee))
                        .recommendAddress(nft.getWalletAddress())
                        .rewardType("HOLD")
                        .build());
            }
        }

        return results;
    }


    /**
     * 统计计算结果
     */
    public void printCalculationSummary(List<RewardCalculateResultDTO> results) {
        if (results.isEmpty()) {
            log.info("没有计算结果");
            return;
        }

        // 按日期统计
        Map<String, BigDecimal> dailyTotal = results.stream()
                .collect(Collectors.groupingBy(
                        RewardCalculateResultDTO::getDate,
                        Collectors.mapping(
                                RewardCalculateResultDTO::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 按NFT类型统计
        Map<String, BigDecimal> typeTotal = results.stream()
                .collect(Collectors.groupingBy(
                        RewardCalculateResultDTO::getType,
                        Collectors.mapping(
                                RewardCalculateResultDTO::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 按奖励类型统计
        Map<String, BigDecimal> rewardTypeTotal = results.stream()
                .collect(Collectors.groupingBy(
                        RewardCalculateResultDTO::getRewardType,
                        Collectors.mapping(
                                RewardCalculateResultDTO::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 输出统计结果
        log.info("=== 计算结果统计 ===");
        log.info("总记录数: {}", results.size());

        log.info("每日奖励总额:");
        dailyTotal.forEach((date, amount) ->
                log.info("  {} : {}", date, amount));

        log.info("NFT类型奖励总额:");
        typeTotal.forEach((type, amount) ->
                log.info("  {} : {}", type, amount));

        log.info("奖励类型统计:");
        rewardTypeTotal.forEach((type, amount) ->
                log.info("  {} : {}", type, amount));
    }

    /**
     * 验证NFT配置数据
     */
    private void validateOdsConfigs(Map<String, OdsConfig> configMap) {
        if (configMap.isEmpty()) {
            throw new RuntimeException("未找到NFT等级配置");
        }

        // 验证每个等级的配置
        configMap.forEach((type, config) -> {
            if (config.getNumber() == null || config.getNumber() <= 0) {
                throw new RuntimeException("NFT等级 " + type + " 的奖励配置无效");
            }
        });
    }


    /**
     * 保存奖励分发记录
     */
    private void saveRewardDistributionRecords(List<RewardCalculateResultDTO> results) {
        log.info("开始保存奖励分发记录，共{}条", results.size());

        for (RewardCalculateResultDTO result : results) {
            // 1. 检查是否已存在记录
            QueryWrapper<RewardDistributionRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wallet_address", result.getWalletAddress())
                    .eq("token_id", result.getTokenId())
                    .eq("reward_type", RebateEnum.ODS.getCode())
                    .eq("create_time", result.getDate() + " 00:00:00")
                    .in("reward_status",
                            RewardDistributionStatusEnum.ISSUED.getCode(),
                            RewardDistributionStatusEnum.PROGRESS.getCode());

            if (rewardDistributionRecordMapper.selectOne(queryWrapper) != null) {
                log.debug("记录已存在，跳过: tokenId={}, address={}", result.getTokenId(), result.getWalletAddress());
                continue;
            }

            // 2. 构建记录
            RewardDistributionRecord record = RewardDistributionRecord.builder()
                    .walletAddress(result.getWalletAddress())
                    .tokenId(result.getTokenId())
                    .rewardNumber(result.getAmount().toString())
                    .rewardType(RebateEnum.ODS.getCode())
                    .createTime(result.getDate() + " 00:00:00")
                    .rewardStatus(RewardDistributionStatusEnum.UNISSUED.getCode())
                    .relationAddress(result.getRecommendAddress()) // 持有人地址
                    .build();

            // 3. 保存区域推荐记录（如果需要）
            RegionRecommendLog regionRecommendLog = rewardDistributionScheduled.saveRegionRecommendLog(record);

            // 4. 保存奖励记录
            rewardDistributionRecordMapper.insert(record);

            // 5. 关联区域推荐记录
            if (regionRecommendLog != null) {
                regionRecommendLog.setRewardDistributionRecordId(record.getId());
                regionRecommendLogMapper.insert(regionRecommendLog);
            }
        }

        log.info("奖励分发记录保存完成");
    }

    /**
     * 验证并清理指定日期范围的旧记录
     */
    private void cleanOldRecords(String startDate, String endDate) {
        log.info("清理 {} 至 {} 的旧记录", startDate, endDate);

        QueryWrapper<RewardDistributionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reward_type", RebateEnum.ODS.getCode())
                .ge("create_time", startDate + " 00:00:00")
                .le("create_time", endDate + " 00:00:00")
                .eq("reward_status", RewardDistributionStatusEnum.UNISSUED.getCode());
        int count = rewardDistributionRecordMapper.delete(queryWrapper);
        log.info("清理了 {} 条旧记录", count);
    }

    /**
     * 获取交易记录
     */
    private List<TransactionRecord> getTransactionRecords(String endDate) {
        return transactionRecordMapper.selectList(
                new QueryWrapper<TransactionRecord>().le("time", endDate + " 23:59:59").orderByAsc("time")
        );
    }

    /**
     * 按日期计算并保存奖励
     */
    private void calculateAndSaveRewardsByDate(String startDate, String endDate, List<TransactionRecord> records, Map<String, OdsConfig> configMap) {

        LocalDate currentDate = LocalDate.parse(startDate);
        LocalDate lastDate = LocalDate.parse(endDate);

        while (!currentDate.isAfter(lastDate)) {
            log.info("计算 {} 的奖励", currentDate);

            // 1. 计算当日奖励
            List<RewardCalculateResultDTO> dailyResults = calculateDailyRewards(currentDate, records, configMap);

            if (!dailyResults.isEmpty()) {
                // 2. 保存当日奖励记录
                saveRewardDistributionRecords(dailyResults);

                // 3. 打印统计信息
                printCalculationSummary(dailyResults);
            } else {
                log.info("{} 没有计算结果", currentDate);
            }

            currentDate = currentDate.plusDays(1);
        }
    }

}

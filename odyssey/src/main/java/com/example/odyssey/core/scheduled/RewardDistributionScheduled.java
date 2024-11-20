package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 计算奖励 和 返佣
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class RewardDistributionScheduled {

    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    OdsConfigMapper odsConfigMapper;
    @Resource
    RebateConfigMapper rebateConfigMapper;
    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;
    @Resource
    RecommendMapper recommendMapper;
    @Resource
    RegionRecommendMapper regionRecommendMapper;
    @Resource
    RegionRecommendLogMapper regionRecommendLogMapper;
    @Resource
    SystemConfigMapper systemConfigMapper;

    /**
     * 每天凌晨执行 发放昨天的ods奖励
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void odsRewardDistribution() {
        log.info("========== ODS奖励发放任务开始 ==========");
        
        QueryWrapper<OdsConfig> odsConfigQueryWrapper = new QueryWrapper();
        List<OdsConfig> odsConfigList = odsConfigMapper.selectList(odsConfigQueryWrapper);
        if (odsConfigList.isEmpty()) {
            log.info("没有找到ODS配置，任务结束");
            return;
        }
        log.info("找到{}个等级的ODS配置", odsConfigList.size());

        // 获取计算日期（前天）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        String yesterdayMaxTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
        log.info("计算日期: {}", yesterdayMaxTime);

        for (OdsConfig odsConfig : odsConfigList) {
            log.info("------ 开始处理{}等级的奖励发放 ------", odsConfig.getType());
            
            QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper();
            nftMessageQueryWrapper.eq("type", odsConfig.getType())
                                .le("transfer_time", yesterdayMaxTime);
            List<NftMessage> nftMessages = nftMessageMapper.selectList(nftMessageQueryWrapper);

            if (nftMessages.isEmpty()) {
                log.info("{}等级没有符合条件的NFT", odsConfig.getType());
                continue;
            }
            log.info("找到{}个符合条件的NFT", nftMessages.size());

            BigDecimal number = BigDecimal.valueOf(odsConfig.getNumber())
                    .divide(BigDecimal.valueOf(nftMessages.size()), 8, RoundingMode.HALF_UP);
            log.info("计算得到每个NFT的基础奖励数量: {}", number);

            int processedCount = 0;
            Map<String, BigDecimal> totalRewardByType = new HashMap<>(); // 记录不同类型的总奖励

            for (NftMessage nftMessage : nftMessages) {
                String address = nftMessage.getNewAddress();
                if (Objects.nonNull(nftMessage.getBuyAddress()) && !nftMessage.getNewAddress().equals(nftMessage.getBuyAddress())) {
                    address = nftMessage.getBuyAddress();
                }

                Map<String, String> rebateMap = getRebateMap(address, number, RebateEnum.ODS.getCode(), nftMessage);
                log.debug("TokenId: {}, 返佣分配: {}", nftMessage.getTokenId(), rebateMap);

                // 统计各类型奖励
                rebateMap.forEach((k, v) -> {
                    totalRewardByType.merge(k, new BigDecimal(v), BigDecimal::add);
                });

                if (Objects.nonNull(nftMessage.getBuyAddress()) && !nftMessage.getNewAddress().equals(nftMessage.getBuyAddress())) {
                    String reward = rebateMap.get(address);
                    rebateMap.remove(address);
                    rebateMap.put(nftMessage.getNewAddress(), reward);
                }

                saveRewardDistributionRecord(rebateMap, nftMessage, RebateEnum.ODS.getCode());
                processedCount++;

                if (processedCount % 100 == 0) {
                    log.info("已处理: {}/{}", processedCount, nftMessages.size());
                }
            }

            log.info("------ {}等级处理完成 ------", odsConfig.getType());
            log.info("统计信息:");
            log.info("- 总NFT数量: {}", nftMessages.size());
            log.info("- 成功处理数量: {}", processedCount);
            log.info("- 各地址获得的总奖励:");
            totalRewardByType.forEach((address, reward) -> {
                log.info("  - {}: {}", address, reward);
            });
            
            // 验证总奖励
            BigDecimal totalReward = totalRewardByType.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("- 实际发放总额: {}", totalReward);
            log.info("- 配置总额: {}", odsConfig.getNumber());
            
            if (totalReward.compareTo(BigDecimal.valueOf(odsConfig.getNumber())) > 0) {
                log.error("警告：实际发放总额超过配置总额！");
            }
        }
        
        log.info("========== ODS奖励发放任务结束 ==========");
    }

    /**
     * 计算每个人 应该获取多少奖励
     *
     * @param address
     * @param number
     * @param rebateType
     * @return
     */
    public Map<String, String> getRebateMap(String address, BigDecimal number, String rebateType,NftMessage nftMessage) {

        QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper();
        recommendQueryWrapper.eq("wallet_address", address);
        //查看是否在推荐体系
        Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

        Map<String, String> rebateMap = new HashMap<>();


        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("`key`","official_wallet_address");

        SystemConfig systemConfig = systemConfigMapper.selectOne(queryWrapper);

        if (Objects.isNull(recommend)){
            if (rebateType.equals(RebateEnum.ODS.getCode())) {

                //todo 还需要转到一个官方钱包 10%
                BigDecimal service = number.multiply(new BigDecimal("0.12"));

                rebateMap.put(systemConfig.getValue(),service.toString());

                rebateMap.put(address, number.subtract(service).toString());
            }

            return rebateMap;
        }

        //有推荐人，计算返佣
        if (Objects.nonNull(recommend.getRecommendWalletAddress())) {

            if (Objects.nonNull(recommend.getRecommendTime())){
                //如果 是购买的 空投的 转入的时间在 建立推荐关系之前 不给上层返佣
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = null;

                if (Objects.nonNull(nftMessage.getBuyTime())){
                    //购买的
                     dateTime = LocalDateTime.parse(nftMessage.getBuyTime(), formatter);
                }else if (Objects.nonNull(nftMessage.getAirdropTime())){
                    //空投的
                    dateTime = LocalDateTime.parse(nftMessage.getAirdropTime(), formatter);
                }else {
                    //转入的
                    dateTime = LocalDateTime.parse(nftMessage.getTransferTime(), formatter);
                }
                Long timestamp = dateTime.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                if (rebateType.equals(RebateEnum.USDT.getCode())) {

                    if (recommend.getRecommendTime() > timestamp){
                        return rebateMap;
                    }

                }else {

                    if (recommend.getRecommendTime() > timestamp){

                        // 还需要转到一个官方钱包 10%
                        BigDecimal service = number.multiply(new BigDecimal("0.12"));

                        rebateMap.put(systemConfig.getValue(),service.toString());

                        rebateMap.put(address, number.subtract(service).toString());

                        return rebateMap;
                    }
                }

            }else {

                return rebateMap;
            }

            QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper();
            rebateConfigQueryWrapper.eq("address", recommend.getLeaderWalletAddress());
            rebateConfigQueryWrapper.eq("recommend_type", recommend.getRecommendType());
            rebateConfigQueryWrapper.eq("rebate_type", rebateType);
            RebateConfig rebateConfig = rebateConfigMapper.selectOne(rebateConfigQueryWrapper);

            if (Objects.isNull(rebateConfig)) {
                return rebateMap;
            }

            if (Objects.nonNull(recommend.getFirstRecommendWalletAddress())) {
                //代表当前用户处在二级
                if (Objects.isNull(recommend.getSecondRecommendWalletAddress())) {

                    BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getFirstRebateRate()));

                    rebateMap.put(recommend.getFirstRecommendWalletAddress(), first.toString());

                    if (rebateType.equals(RebateEnum.ODS.getCode())) {
                        rebateMap.put(recommend.getWalletAddress(), number.subtract(first).toString());
                    }

                } else {
                    BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getThreeRebateRate()));
                    rebateMap.put(recommend.getFirstRecommendWalletAddress(), first.toString());

                    BigDecimal second = number.multiply(new BigDecimal(rebateConfig.getSecondRebateRate()));
                    rebateMap.put(recommend.getSecondRecommendWalletAddress(), second.toString());

                    if (rebateType.equals(RebateEnum.ODS.getCode())) {
                        rebateMap.put(recommend.getWalletAddress(), number.subtract(first).subtract(second).toString());
                    }
                }
            }
        } else {
            //没有推荐人 或者代表当前用户处在一级 不需要进行·返佣
            if (rebateType.equals(RebateEnum.ODS.getCode())) {

                if (recommend.getRecommendType().equals(RecommendEnum.LEADER.getCode())){

                    rebateMap.put(address, number.toString());

                }else {

                    BigDecimal service = number.multiply(new BigDecimal("0.12"));

                    rebateMap.put(systemConfig.getValue(),service.toString());

                    rebateMap.put(address, number.subtract(service).toString());
                }

            }
        }

        return rebateMap;
    }

    public void saveRewardDistributionRecord(Map<String, String> rebateMap, NftMessage nftMessage, String rebateType) {


        LocalDate newDay = LocalDate.now();
        LocalDateTime startDateTime = LocalDateTime.of(newDay, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(newDay, LocalTime.MAX);

        //todo 保存返佣记录
        rebateMap.forEach((k, v) -> {

            //判断今天是否已经发放
            QueryWrapper<RewardDistributionRecord> rewardDistributionRecordQueryWrapper = new QueryWrapper();
            rewardDistributionRecordQueryWrapper.eq("wallet_address", k);
            rewardDistributionRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
            rewardDistributionRecordQueryWrapper.eq("reward_type", rebateType);
            if (rebateType.equals(RebateEnum.ODS.getCode())) {
                rewardDistributionRecordQueryWrapper.between("create_time", startDateTime, endDateTime);
            }

            RewardDistributionRecord rewardDistributionRecord = rewardDistributionRecordMapper.selectOne(rewardDistributionRecordQueryWrapper);
            if (Objects.nonNull(rewardDistributionRecord)) {
                return;
            }

            rewardDistributionRecord = new RewardDistributionRecord();
            rewardDistributionRecord.setWalletAddress(k);
            rewardDistributionRecord.setTokenId(nftMessage.getTokenId());
            rewardDistributionRecord.setRewardNumber(v);
            rewardDistributionRecord.setRewardType(rebateType);
            rewardDistributionRecord.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            rewardDistributionRecord.setRewardStatus(RewardDistributionStatusEnum.UNISSUED.getCode());

            rewardDistributionRecord.setRelationAddress(nftMessage.getNewAddress());

            RegionRecommendLog regionRecommendLog = saveRegionRecommendLog(rewardDistributionRecord);

            rewardDistributionRecordMapper.insert(rewardDistributionRecord);

            if (Objects.nonNull(regionRecommendLog)){

                regionRecommendLog.setRewardDistributionRecordId(rewardDistributionRecord.getId());
                regionRecommendLogMapper.insert(regionRecommendLog);
            }


        });


    }

    public RegionRecommendLog saveRegionRecommendLog(RewardDistributionRecord rewardDistributionRecord) {

        QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper();
        recommendQueryWrapper.eq("wallet_address", rewardDistributionRecord.getWalletAddress());
        Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

        if (Objects.isNull(recommend)) {
            return null;
        }

        if (!recommend.getWalletAddress().equals(recommend.getLeaderWalletAddress())) {
            return null;
        }

        QueryWrapper<RegionRecommend> regionRecommendQueryWrapper = new QueryWrapper();
        regionRecommendQueryWrapper.eq("leader_address", recommend.getWalletAddress());

        RegionRecommend regionRecommend = regionRecommendMapper.selectOne(regionRecommendQueryWrapper);
        if (Objects.isNull(regionRecommend)) {
            return null;
        }

        QueryWrapper<RegionRecommendLog> regionRecommendLogQueryWrapper = new QueryWrapper();
        regionRecommendLogQueryWrapper.eq("reward_distribution_record_id", rewardDistributionRecord.getId());

        RegionRecommendLog regionRecommendLog = regionRecommendLogMapper.selectOne(regionRecommendLogQueryWrapper);
        if (Objects.nonNull(regionRecommendLog)) {
            return null;
        }

        regionRecommendLog = new RegionRecommendLog();
        regionRecommendLog.setRegionAddress(regionRecommend.getRegionAddress());
        regionRecommendLog.setLeaderAddress(recommend.getWalletAddress());
        regionRecommendLog.setRewardNumber(new BigDecimal(rewardDistributionRecord.getRewardNumber()).multiply(new BigDecimal(regionRecommend.getRebateRate())).toString());
        regionRecommendLog.setType(rewardDistributionRecord.getRewardType());
        regionRecommendLog.setTokenId(rewardDistributionRecord.getTokenId());

        BigDecimal reward = new BigDecimal(rewardDistributionRecord.getRewardNumber()).subtract(new BigDecimal(regionRecommendLog.getRewardNumber()));

        rewardDistributionRecord.setRewardNumber(reward.toString());

        return regionRecommendLog;
    }


    @Scheduled(cron = "0 0/2 * * * ?")
    public void usdtRewardDistribution() {

        log.info("usdtRewardDistribution 开始执行");

        QueryWrapper<RewardDistributionRecord> rewardDistributionRecordQueryWrapper = new QueryWrapper<>();
        rewardDistributionRecordQueryWrapper.eq("reward_type", RebateEnum.USDT.getCode());
        rewardDistributionRecordQueryWrapper.orderByDesc("token_id");
        rewardDistributionRecordQueryWrapper.last("Limit 1");
        RewardDistributionRecord rewardDistributionRecord = rewardDistributionRecordMapper.selectOne(rewardDistributionRecordQueryWrapper);

        QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper<>();
        nftMessageQueryWrapper.isNotNull("buy_address");

        if (Objects.nonNull(rewardDistributionRecord)) {
            nftMessageQueryWrapper.ge("token_id", rewardDistributionRecord.getTokenId());
        }

        List<NftMessage> nftMessageList = nftMessageMapper.selectList(nftMessageQueryWrapper);

        for (NftMessage nftMessage : nftMessageList) {

            Map<String, String> rebateMap = getRebateMap(nftMessage.getBuyAddress(), BigDecimal.valueOf(3600L), RebateEnum.USDT.getCode(),nftMessage);

            //todo 保存返佣记录
            saveRewardDistributionRecord(rebateMap, nftMessage, RebateEnum.USDT.getCode());
        }

        log.info("usdtRewardDistribution 结束执行");
    }

}

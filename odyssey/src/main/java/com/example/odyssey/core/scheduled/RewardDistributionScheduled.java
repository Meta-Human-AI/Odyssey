package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.common.RebateEnum;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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


    /**
     * 每天凌晨2点执行 发放昨天的ods奖励
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void odsRewardDistribution() {

        log.info("rewardDistribution 开始执行");
        QueryWrapper<OdsConfig> odsConfigQueryWrapper = new QueryWrapper();

        List<OdsConfig> odsConfigList = odsConfigMapper.selectList(odsConfigQueryWrapper);
        if (odsConfigList.isEmpty()) {
            log.info("rewardDistribution 结束执行，没有配置");
            return;
        }

        for (OdsConfig odsConfig : odsConfigList) {
            //todo 计算每个等级 每人获取的ods数量 再根据返佣比例计算返佣数量

            //计算每个等级总数
            QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper();
            nftMessageQueryWrapper.eq("type", odsConfig.getType());

            List<NftMessage> nftMessages = nftMessageMapper.selectList(nftMessageQueryWrapper);

            if (nftMessages.isEmpty()) {
                log.info("rewardDistribution  {}:没有用户", odsConfig.getType());
                continue;
            }

            //计算每个人获取的ods数量
            BigDecimal number = BigDecimal.valueOf(odsConfig.getNumber()).divide(BigDecimal.valueOf(nftMessages.size()), 8, BigDecimal.ROUND_UP);

            for (NftMessage nftMessage : nftMessages) {

                String address = nftMessage.getOldAddress();

                if (!nftMessage.getOldAddress().equals(nftMessage.getBuyAddress())) {
                    address = nftMessage.getBuyAddress();
                }

                Map<String, String> rebateMap = getRebateMap(address, number, RebateEnum.ODS.getCode());

                //todo 保存返佣记录
                saveRewardDistributionRecord(rebateMap,nftMessage,RebateEnum.ODS.getCode());
            }

        }

        log.info("rewardDistribution 结束执行");
    }

    /**
     * 计算每个人 应该获取多少奖励
     * @param address
     * @param number
     * @param rebateType
     * @return
     */
    public Map<String, String> getRebateMap(String address, BigDecimal number, String rebateType) {

        QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper();
        recommendQueryWrapper.eq("wallet_address", address);
        //查看是否有推荐人
        Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

        Map<String, String> rebateMap = new HashMap<>();
        //有推荐人，计算返佣
        if (Objects.nonNull(recommend)) {

            QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper();
            rebateConfigQueryWrapper.eq("address", recommend.getLeaderWalletAddress());
            rebateConfigQueryWrapper.eq("recommend_type", recommend.getRecommendType());
            rebateConfigQueryWrapper.eq("rebate_type", rebateType);
            RebateConfig rebateConfig = rebateConfigMapper.selectOne(rebateConfigQueryWrapper);

            if (Objects.nonNull(recommend.getFirstRecommendWalletAddress())) {
                //代表当前用户处在二级
                if (Objects.isNull(recommend.getSecondRecommendWalletAddress())) {

                    BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getSecondRebateRate()));

                    rebateMap.put(recommend.getFirstRecommendWalletAddress(), first.toString());

                    if (rebateType.equals(RebateEnum.ODS.getCode())){
                        rebateMap.put(recommend.getWalletAddress(), number.subtract(first).toString());
                    }

                } else {
                    BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getFirstRebateRate()));
                    rebateMap.put(recommend.getFirstRecommendWalletAddress(), first.toString());

                    BigDecimal second = number.multiply(new BigDecimal(rebateConfig.getSecondRebateRate()));
                    rebateMap.put(recommend.getSecondRecommendWalletAddress(), second.toString());

                    if (rebateType.equals(RebateEnum.ODS.getCode())){
                        rebateMap.put(recommend.getWalletAddress(), number.subtract(first).subtract(second).toString());
                    }
                }
            } else {
                //代表当前用户处在一级 不需要进行·返佣
                if (rebateType.equals(RebateEnum.ODS.getCode())){
                    rebateMap.put(recommend.getWalletAddress(), number.toString());
                }
            }
        } else {
            //没有推荐人
            if (rebateType.equals(RebateEnum.ODS.getCode())){
                rebateMap.put(recommend.getWalletAddress(), number.toString());
            }
        }

        return rebateMap;
    }

    public void saveRewardDistributionRecord(Map<String, String> rebateMap, NftMessage nftMessage, String rebateType) {

        List<RewardDistributionRecord> rewardDistributionRecordList = new ArrayList<>();

        LocalDate newDay = LocalDate.now();
        LocalDateTime startDateTime = LocalDateTime.of(newDay, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(newDay, LocalTime.MAX);

        //todo 保存返佣记录
        rebateMap.forEach((k, v) -> {

            //判断今天是否已经发放
            QueryWrapper<RewardDistributionRecord> rewardDistributionRecordQueryWrapper = new QueryWrapper();
            rewardDistributionRecordQueryWrapper.eq("wallet_address", k);
            rewardDistributionRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
            rewardDistributionRecordQueryWrapper.between("create_time", startDateTime, endDateTime);

            RewardDistributionRecord rewardDistributionRecord = new RewardDistributionRecord();
            rewardDistributionRecord.setWalletAddress(k);
            rewardDistributionRecord.setTokenId(nftMessage.getTokenId());
            rewardDistributionRecord.setRewardNumber(v);
            rewardDistributionRecord.setRewardType(RebateEnum.ODS.getCode());
            rewardDistributionRecord.setCreateTime(LocalDateTime.now().toString());
            rewardDistributionRecord.setRewardStatus(RewardDistributionStatusEnum.UNISSUED.getCode());

            rewardDistributionRecord.setRelationAddress(nftMessage.getOldAddress());

            rewardDistributionRecordList.add(rewardDistributionRecord);

        });

        if (CollectionUtils.isEmpty(rewardDistributionRecordList)){
            return;
        }

        rewardDistributionRecordMapper.insertBatchSomeColumn(rewardDistributionRecordList);

    }

}

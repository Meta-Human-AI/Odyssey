package com.example.odyssey.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsCompensateCmd;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.common.RewardDistributionStatusEnum;
import com.example.odyssey.core.scheduled.RewardDistributionScheduled;
import com.example.odyssey.core.service.RewardCompensateService;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional
public class RewardCompensateServiceImpl implements RewardCompensateService {

    @Resource
    private RewardDistributionRecordMapper rewardDistributionRecordMapper;
    @Resource
    private RewardDistributionScheduled rewardDistributionScheduled;
    @Resource
    private NftDailyHoldRecordMapper nftDailyHoldRecordMapper;
    @Resource
    private NftMessageMapper nftMessageMapper;
    @Resource
    SystemConfigMapper systemConfigMapper;
    @Resource
    private RebateConfigMapper rebateConfigMapper;

    @Override
    public SingleResponse compensateOdsReward(OdsCompensateCmd odsCompensateCmd) {

        QueryWrapper<NftDailyHoldRecord> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasLength(odsCompensateCmd.getTime())) {
            queryWrapper.eq("date", odsCompensateCmd.getTime());
        }
        if (StringUtils.hasLength(odsCompensateCmd.getStartTime()) && StringUtils.hasLength(odsCompensateCmd.getEndTime())) {
            queryWrapper.between("date", odsCompensateCmd.getStartTime(), odsCompensateCmd.getEndTime());
        }

        List<NftDailyHoldRecord> nftDailyHoldRecordList = nftDailyHoldRecordMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(nftDailyHoldRecordList)) {
            return SingleResponse.buildFailure("未查询到持仓记录");
        }

        for (NftDailyHoldRecord nftDailyHoldRecord : nftDailyHoldRecordList) {


            LocalDate localDate = LocalDate.parse(nftDailyHoldRecord.getDate());

            LocalDate dayDate = localDate.minusDays(2);

            LocalTime localTime = LocalTime.of(23, 59, 59);

            LocalDateTime localDateTime = LocalDateTime.of(dayDate, localTime);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime transferTime = LocalDateTime.parse(nftDailyHoldRecord.getTransferTime(), dateTimeFormatter);

            if (transferTime.isAfter(localDateTime)) {
                continue;
            }

            Map<String, String> rebateMap = getRebateMap(nftDailyHoldRecord);

            saveRewardDistributionRecord(rebateMap, nftDailyHoldRecord);
        }

        return SingleResponse.buildSuccess();

    }


    public static void main(String[] args) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDate localDate = LocalDate.parse("2021-07-01");

        LocalDate time = localDate.minusDays(2);

        LocalTime localTime = LocalTime.of(23, 59, 59);

        LocalDateTime localDateTime = LocalDateTime.of(time, localTime);

        System.out.println(localDateTime.format(dateTimeFormatter));

        System.out.println(time + " 23:59:59");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -2); // 减去一天
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        String yesterdayMaxTime = simpleDateFormat.format(calendar.getTime());
        System.out.println(yesterdayMaxTime);


        LocalDateTime one = LocalDateTime.parse("2021-07-01 23:59:59", dateTimeFormatter);

        LocalDateTime two = LocalDateTime.parse("2021-07-02 23:59:59", dateTimeFormatter);

        System.out.println(one.isAfter(two));
    }

    public Map<String, String> getRebateMap(NftDailyHoldRecord nftDailyHoldRecord) {


        Map<String, String> rebateMap = new HashMap<>();

        QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper<>();
        nftMessageQueryWrapper.eq("token_id", nftDailyHoldRecord.getTokenId());

        NftMessage nftMessage = nftMessageMapper.selectOne(nftMessageQueryWrapper);
        if (Objects.isNull(nftMessage)) {
            return rebateMap;
        }

        if (Objects.isNull(nftDailyHoldRecord.getLeaderWalletAddress())) {
            return rebateMap;
        }

        QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper();
        rebateConfigQueryWrapper.eq("address", nftDailyHoldRecord.getLeaderWalletAddress());
        rebateConfigQueryWrapper.eq("recommend_type", nftDailyHoldRecord.getRecommendType());
        rebateConfigQueryWrapper.eq("rebate_type", RebateEnum.ODS.getCode());
        RebateConfig rebateConfig = rebateConfigMapper.selectOne(rebateConfigQueryWrapper);

        if (Objects.isNull(rebateConfig)) {
            return rebateMap;
        }

        BigDecimal number = new BigDecimal(nftDailyHoldRecord.getNumber());

        if (Objects.nonNull(nftDailyHoldRecord.getFirstRecommendWalletAddress())) {
            //代表当前用户处在二级
            if (Objects.isNull(nftDailyHoldRecord.getSecondRecommendWalletAddress())) {

                BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getFirstRebateRate()));

                rebateMap.put(nftDailyHoldRecord.getFirstRecommendWalletAddress(), first.toString());

                rebateMap.put(nftDailyHoldRecord.getWalletAddress(), number.subtract(first).toString());

            } else {
                BigDecimal first = number.multiply(new BigDecimal(rebateConfig.getThreeRebateRate()));
                rebateMap.put(nftDailyHoldRecord.getFirstRecommendWalletAddress(), first.toString());

                BigDecimal second = number.multiply(new BigDecimal(rebateConfig.getSecondRebateRate()));
                rebateMap.put(nftDailyHoldRecord.getSecondRecommendWalletAddress(), second.toString());

                rebateMap.put(nftDailyHoldRecord.getWalletAddress(), number.subtract(first).subtract(second).toString());
            }
        } else {
            if (nftDailyHoldRecord.getRecommendType().equals(RecommendEnum.LEADER.getCode())) {

                rebateMap.put(nftDailyHoldRecord.getWalletAddress(), number.toString());

            } else {

                QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("`key`","official_wallet_address");

                SystemConfig systemConfig = systemConfigMapper.selectOne(queryWrapper);

                BigDecimal service = number.multiply(new BigDecimal("0.12"));

                rebateMap.put(systemConfig.getValue(), service.toString());

                rebateMap.put(nftDailyHoldRecord.getWalletAddress(), number.subtract(service).toString());
            }

        }
        return rebateMap;
    }

    public void saveRewardDistributionRecord(Map<String, String> rebateMap, NftDailyHoldRecord nftDailyHoldRecord) {

        List<RewardDistributionRecord> rewardDistributionRecordList = new ArrayList<>();

        //todo 保存返佣记录
        rebateMap.forEach((k, v) -> {

            //判断今天是否已经发放
            QueryWrapper<RewardDistributionRecord> rewardDistributionRecordQueryWrapper = new QueryWrapper();
            rewardDistributionRecordQueryWrapper.eq("wallet_address", k);
            rewardDistributionRecordQueryWrapper.eq("token_id", nftDailyHoldRecord.getTokenId());
            rewardDistributionRecordQueryWrapper.eq("reward_type", RebateEnum.ODS.getCode());
            rewardDistributionRecordQueryWrapper.eq("create_time", nftDailyHoldRecord.getDate() + "00:00:00");

            RewardDistributionRecord rewardDistributionRecord = rewardDistributionRecordMapper.selectOne(rewardDistributionRecordQueryWrapper);
            if (Objects.nonNull(rewardDistributionRecord)) {
                return;
            }

            rewardDistributionRecord = new RewardDistributionRecord();
            rewardDistributionRecord.setWalletAddress(k);
            rewardDistributionRecord.setTokenId(nftDailyHoldRecord.getTokenId());
            rewardDistributionRecord.setRewardNumber(v);
            rewardDistributionRecord.setRewardType(RebateEnum.ODS.getCode());
            rewardDistributionRecord.setCreateTime(nftDailyHoldRecord.getDate() + "00:00:00");
            rewardDistributionRecord.setRewardStatus(RewardDistributionStatusEnum.UNISSUED.getCode());
            rewardDistributionRecord.setRelationAddress(nftDailyHoldRecord.getWalletAddress());

            rewardDistributionRecordList.add(rewardDistributionRecord);

        });

        if (CollectionUtils.isEmpty(rewardDistributionRecordList)) {
            return;
        }

        rewardDistributionRecordMapper.insertBatchSomeColumn(rewardDistributionRecordList);

        for (RewardDistributionRecord rewardDistributionRecord : rewardDistributionRecordList) {
            rewardDistributionScheduled.saveRegionRecommendLog(rewardDistributionRecord);
        }

    }

}

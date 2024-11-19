package com.example.odyssey.core.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.RecommendCoreDTO;
import com.example.odyssey.bean.dto.RecommendDTO;
import com.example.odyssey.bean.dto.RecommendListDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.core.cmd.RecommendCmd;
import com.example.odyssey.core.cmd.RecommendCoreLogCmd;
import com.example.odyssey.core.service.RebateConfigService;
import com.example.odyssey.core.service.RecommendService;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class RecommendServiceImpl implements RecommendService {

    @Resource
    RedissonClient redissonClient;
    @Resource
    RebateConfigMapper rebateConfigMapper;
    @Resource
    RecommendCoreLogMapper recommendCoreLogMapper;
    @Resource
    RecommendMapper recommendMapper;

    @Resource
    SystemConfigMapper systemConfigMapper;

    @Resource
    RebateConfigService rebateConfigService;

    @Resource
    RewardDistributionRecordMapper rewardDistributionRecordMapper;

    @Resource
    RecommendCmd recommendCmd;

    @Resource
    RecommendCoreLogCmd recommendCoreLogCmd;

    @Resource
    NftMessageMapper nftMessageMapper;

    @Resource
    RegionRecommendMapper regionRecommendMapper;

    @Override
    public SingleResponse<RecommendCoreDTO> getRecommendCore(RecommendCoreCreateCmd recommendCoreCreateCmd) {

        RecommendCoreDTO recommendCoreDTO = new RecommendCoreDTO();

        QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
        recommendQueryWrapper.eq("wallet_address", recommendCoreCreateCmd.getWalletAddress());
        Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

        if (Objects.nonNull(recommend)) {
            recommendCoreDTO.setRecommendWalletAddress(recommend.getRecommendWalletAddress());
        } else {

            RecommendCreateCmd recommendCreateCmd = new RecommendCreateCmd();
            recommendCreateCmd.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());
            recommendCreateCmd.setRecommendType(RecommendEnum.NORMAL.getCode());
            recommendCreateCmd.setLeaderWalletAddress(recommendCoreCreateCmd.getWalletAddress());
            recommendCreateCmd.setRecommendTime(0L);
            recommend = recommendCmd.createRecommend(recommendCreateCmd);

            recommendCoreDTO.setRecommendWalletAddress("");

            RebateConfigCreateDefaultCmd rebateConfigCreateDefaultCmd = new RebateConfigCreateDefaultCmd();
            rebateConfigCreateDefaultCmd.setAddress(recommendCoreCreateCmd.getWalletAddress());

            rebateConfigService.defaultAdd(rebateConfigCreateDefaultCmd);
        }

        RecommendCoreLog recommendCoreLog = recommendCoreLogCmd.createRecommendCoreLog(recommendCoreCreateCmd);
        recommendCoreDTO.setRecommendCore(recommendCoreLog.getRecommendCore());

        return SingleResponse.of(recommendCoreDTO);

//        RLock redLock = redissonClient.getLock("odyssey:recommend:" + recommendCoreCreateCmd.getWalletAddress());
//
//        try {
//            if (redLock.tryLock(5, TimeUnit.SECONDS)) {
//
//                RecommendCoreDTO recommendCoreDTO = new RecommendCoreDTO();
//
//                QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
//                recommendQueryWrapper.eq("wallet_address", recommendCoreCreateCmd.getWalletAddress());
//                Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);
//
//                if (Objects.nonNull(recommend)) {
//                    recommendCoreDTO.setRecommendWalletAddress(recommend.getRecommendWalletAddress());
//                } else {
//
//                    recommend = new Recommend();
//                    recommend.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());
//                    recommend.setCreateTime(System.currentTimeMillis());
//                    recommend.setRecommendType(RecommendEnum.NORMAL.getCode());
//                    recommend.setLeaderWalletAddress(recommendCoreCreateCmd.getWalletAddress());
//                    recommendMapper.insert(recommend);
//
//                    RebateConfigCreateDefaultCmd rebateConfigCreateDefaultCmd = new RebateConfigCreateDefaultCmd();
//                    rebateConfigCreateDefaultCmd.setAddress(recommendCoreCreateCmd.getWalletAddress());
//
//                    rebateConfigService.defaultAdd(rebateConfigCreateDefaultCmd);
//
//                    recommendCoreDTO.setRecommendWalletAddress("");
//                }
//
//                QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
//                recommendCoreLogQueryWrapper.eq("wallet_address", recommendCoreCreateCmd.getWalletAddress());
//                RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);
//                if (Objects.nonNull(recommendCoreLog)) {
//                    recommendCoreDTO.setRecommendCore(recommendCoreLog.getRecommendCore());
//                    return SingleResponse.of(recommendCoreDTO);
//                }
//
//                recommendCoreLog = new RecommendCoreLog();
//                recommendCoreLog.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());
//
//                String randomAlphabetic;
//
//                while (true) {
//
//                    randomAlphabetic = RandomStringUtils.random(7, true, true).toUpperCase();
//
//                    QueryWrapper<RecommendCoreLog> queryWrapper = new QueryWrapper<>();
//                    queryWrapper.eq("recommend_core", randomAlphabetic);
//
//                    Long count = recommendCoreLogMapper.selectCount(queryWrapper);
//                    if (count == 0) {
//                        break;
//                    }
//                }
//
//                recommendCoreLog.setRecommendCore(randomAlphabetic);
//                recommendCoreLog.setCreateTime(System.currentTimeMillis());
////                recommendCoreLog.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
//                recommendCoreLogMapper.insert(recommendCoreLog);
//
//                QueryWrapper<SystemConfig> systemQueryWrapper = new QueryWrapper<>();
//
//                systemQueryWrapper.eq("`key`", "url");
//
//                SystemConfig systemConfig = systemConfigMapper.selectOne(systemQueryWrapper);
//
//                if (Objects.isNull(systemConfig)) {
//                    return SingleResponse.buildFailure("URL configuration not found");
//                }
//
//                recommendCoreDTO.setRecommendUrl(systemConfig.getValue() + "/odyssey/v1/recommend/add?core=" + randomAlphabetic);
//
//                recommendCoreDTO.setRecommendCore(randomAlphabetic);
//
//                return SingleResponse.of(recommendCoreDTO);
//
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return SingleResponse.buildFailure("获取推荐码失败");
//
//        } finally {
//            if (redLock.isHeldByCurrentThread()) {
//                redLock.unlock();
//            }
//        }
//        return SingleResponse.buildFailure("获取推荐码失败");
    }

    @Override
    public SingleResponse recommend(RecommendCreateCmd recommendCreateCmd) {

        QueryWrapper<RegionRecommend> regionRecommendQueryWrapper = new QueryWrapper();
        regionRecommendQueryWrapper.eq("region_address", recommendCreateCmd.getWalletAddress());

        Long regionRecommendCount = regionRecommendMapper.selectCount(regionRecommendQueryWrapper);
        if (regionRecommendCount > 0) {
            return SingleResponse.buildFailure("该地址不能被推荐");
        }


        Recommend oldRecommend = null;

        if (Objects.nonNull(recommendCreateCmd.getRecommendWalletAddress())) {

            QueryWrapper<RegionRecommend> regionRecommendWalletAddressQueryWrapper = new QueryWrapper();
            regionRecommendWalletAddressQueryWrapper.eq("region_address", recommendCreateCmd.getWalletAddress());

            Long regionRecommendWalletAddressCount = regionRecommendMapper.selectCount(regionRecommendQueryWrapper);
            if (regionRecommendWalletAddressCount > 0) {
                return SingleResponse.buildFailure("该地址不能推荐别人");
            }

            //推荐人
            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("wallet_address", recommendCreateCmd.getRecommendWalletAddress());
            oldRecommend = recommendMapper.selectOne(recommendQueryWrapper);

        } else {

            //推荐码
            QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
            recommendCoreLogQueryWrapper.eq("recommend_core", recommendCreateCmd.getRecommendCore());
            RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);

            if (Objects.isNull(recommendCoreLog)) {
                return SingleResponse.buildFailure("推荐码不存在");
            }

            QueryWrapper<RegionRecommend> regionrecommendCoreQueryWrapper = new QueryWrapper();
            regionrecommendCoreQueryWrapper.eq("region_address", recommendCoreLog.getWalletAddress());

            Long regionrecommendCoreCount = regionRecommendMapper.selectCount(regionRecommendQueryWrapper);
            if (regionrecommendCoreCount > 0) {
                return SingleResponse.buildFailure("该地址不能推荐别人");
            }
            //推荐人
            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("wallet_address", recommendCoreLog.getWalletAddress());
            oldRecommend = recommendMapper.selectOne(recommendQueryWrapper);


            QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper<>();
            nftMessageQueryWrapper.eq("buy_address", recommendCreateCmd.getWalletAddress());

            Long count = nftMessageMapper.selectCount(nftMessageQueryWrapper);
            if (count == 0) {
                return SingleResponse.buildFailure("请先购买NFT");
            }
        }


        if (Objects.isNull(oldRecommend)) {
            return SingleResponse.buildFailure("推荐人不存在");
        }

        if (Objects.nonNull(oldRecommend.getRecommendWalletAddress()) && oldRecommend.getRecommendWalletAddress().equals(recommendCreateCmd.getWalletAddress())) {
            return SingleResponse.buildFailure("不能循环推荐");
        }


        //被推荐人
        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wallet_address", recommendCreateCmd.getWalletAddress());
        Recommend recommend = recommendMapper.selectOne(queryWrapper);

        if (Objects.nonNull(recommend)) {

            if (recommend.getWalletAddress().equals(oldRecommend.getWalletAddress())) {
                return SingleResponse.buildFailure("不能自己推荐自己");
            }

            if (Objects.nonNull(recommend.getRecommendWalletAddress())) {
                return SingleResponse.buildFailure("已推荐过");
            }

            if (Objects.nonNull(recommend.getRecommendType()) && recommend.getRecommendType().equals(RecommendEnum.LEADER.getCode())) {
                return SingleResponse.buildFailure("不能推荐队长");
            }

            //游客
            RecommendUpdateCmd recommendUpdateCmd = new RecommendUpdateCmd();
            recommendUpdateCmd.setId(recommend.getId());
            recommendUpdateCmd.setWalletAddress(recommendCreateCmd.getWalletAddress());
            recommendUpdateCmd.setRecommendWalletAddress(oldRecommend.getWalletAddress());
            recommendUpdateCmd.setRecommendCore(recommendCreateCmd.getRecommendCore());
            recommendUpdateCmd.setLeaderWalletAddress(oldRecommend.getLeaderWalletAddress());

            if (Objects.isNull(recommendCreateCmd.getRecommendTime())){
                recommendUpdateCmd.setRecommendTime(System.currentTimeMillis());
            }else {
                recommendUpdateCmd.setRecommendTime(recommendCreateCmd.getRecommendTime());
            }

            if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress()) || Objects.isNull(oldRecommend.getSecondRecommendWalletAddress())) {
                if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress())) {
                    recommendUpdateCmd.setFirstRecommendWalletAddress(oldRecommend.getWalletAddress());
                } else {
                    recommendUpdateCmd.setFirstRecommendWalletAddress(oldRecommend.getFirstRecommendWalletAddress());
                    recommendUpdateCmd.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                }
                recommendUpdateCmd.setRecommendType(Objects.isNull(oldRecommend.getRecommendType()) ? RecommendEnum.LEADER.getCode() : oldRecommend.getRecommendType());
            } else {
                recommendUpdateCmd.setRecommendType(RecommendEnum.NORMAL.getCode());
                recommendUpdateCmd.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                recommendUpdateCmd.setFirstRecommendWalletAddress(oldRecommend.getSecondRecommendWalletAddress());
            }

            recommendCmd.updateRecommend(recommendUpdateCmd);

            QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper<>();
            rebateConfigQueryWrapper.eq("address", recommend.getWalletAddress());

            rebateConfigMapper.delete(rebateConfigQueryWrapper);
        } else {

            recommendCreateCmd.setRecommendWalletAddress(oldRecommend.getWalletAddress());
            recommendCreateCmd.setRecommendCore(recommendCreateCmd.getRecommendCore());
            recommendCreateCmd.setLeaderWalletAddress(oldRecommend.getLeaderWalletAddress());
            if (Objects.isNull(recommendCreateCmd.getRecommendTime())){
                recommendCreateCmd.setRecommendTime(System.currentTimeMillis());
            }

            if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress()) || Objects.isNull(oldRecommend.getSecondRecommendWalletAddress())) {
                if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress())) {
                    recommendCreateCmd.setFirstRecommendWalletAddress(oldRecommend.getWalletAddress());
                } else {
                    recommendCreateCmd.setFirstRecommendWalletAddress(oldRecommend.getFirstRecommendWalletAddress());
                    recommendCreateCmd.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                }
                recommendCreateCmd.setRecommendType(Objects.isNull(oldRecommend.getRecommendType()) ? RecommendEnum.LEADER.getCode() : oldRecommend.getRecommendType());
            } else {
                recommendCreateCmd.setRecommendType(RecommendEnum.NORMAL.getCode());
                recommendCreateCmd.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                recommendCreateCmd.setFirstRecommendWalletAddress(oldRecommend.getSecondRecommendWalletAddress());
            }

            recommendCmd.createRecommend(recommendCreateCmd);

        }

        return SingleResponse.buildSuccess();


//        RLock redLock = redissonClient.getLock("odyssey:recommend:" + recommendCreateCmd.getWalletAddress());
//
//
//        try {
//            //推荐人
////            Object recommendWalletAddress = redisTemplate.opsForValue().get(recommendCreateCmd.getRecommendCore());
////            if (Objects.isNull(recommendWalletAddress)) {
////                return SingleResponse.buildFailure("推荐码不存在或已过期");
////            }
//            if (redLock.tryLock(5, TimeUnit.SECONDS)) {
//
//                QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
//                recommendCoreLogQueryWrapper.eq("recommend_core", recommendCreateCmd.getRecommendCore());
//                RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);
//
//                if (Objects.isNull(recommendCoreLog)) {
//                    return SingleResponse.buildFailure("推荐码不存在");
//                }
//
//                QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("wallet_address", recommendCreateCmd.getWalletAddress());
//                Recommend recommend = recommendMapper.selectOne(queryWrapper);
//
//                if (Objects.nonNull(recommend) && Objects.nonNull(recommend.getRecommendWalletAddress())) {
//                    return SingleResponse.buildFailure("已推荐过");
//                } else {
//                    //查询推荐人的推荐关系
//                    QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
//                    recommendQueryWrapper.eq("wallet_address", recommendCoreLog.getWalletAddress());
//                    Recommend oldRecommend = recommendMapper.selectOne(recommendQueryWrapper);
//
//                    if (Objects.isNull(oldRecommend)) {
//                        return SingleResponse.buildFailure("推荐人不存在");
//                    }
//
//                    if (Objects.nonNull(oldRecommend.getRecommendWalletAddress()) && oldRecommend.getRecommendWalletAddress().equals(recommendCreateCmd.getWalletAddress())) {
//                        return SingleResponse.buildFailure("不能循环推荐");
//                    }
//
//
//                    if (Objects.isNull(recommend)) {
//                        recommend = new Recommend();
//                    }
//
//                    recommend.setWalletAddress(recommendCreateCmd.getWalletAddress());
//                    recommend.setRecommendWalletAddress(recommendCoreLog.getWalletAddress());
//                    recommend.setRecommendCode(recommendCreateCmd.getRecommendCore());
//                    recommend.setLeaderWalletAddress(oldRecommend.getLeaderWalletAddress());
//
//                    if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress()) || Objects.isNull(oldRecommend.getSecondRecommendWalletAddress())) {
//                        if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress())) {
//                            recommend.setFirstRecommendWalletAddress(oldRecommend.getWalletAddress());
//                        } else {
//                            recommend.setFirstRecommendWalletAddress(oldRecommend.getFirstRecommendWalletAddress());
//                            recommend.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
//                        }
//                        recommend.setRecommendType(Objects.isNull(oldRecommend.getRecommendType()) ? RecommendEnum.LEADER.getCode() : oldRecommend.getRecommendType());
//                    } else {
//                        recommend.setRecommendType(RecommendEnum.NORMAL.getCode());
//                        recommend.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
//                        recommend.setFirstRecommendWalletAddress(oldRecommend.getSecondRecommendWalletAddress());
//                    }
//
//                    recommend.setCreateTime(System.currentTimeMillis());
//
//                    if (Objects.isNull(recommend.getId())) {
//                        recommendMapper.insert(recommend);
//                    } else {
//                        recommendMapper.updateById(recommend);
//
//                        QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper<>();
//                        rebateConfigQueryWrapper.eq("address", recommend.getWalletAddress());
//
//                        rebateConfigMapper.delete(rebateConfigQueryWrapper);
//                    }
//                }
//
//
//                return SingleResponse.buildSuccess();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return SingleResponse.buildFailure("推荐失败");
//        } finally {
//            if (redLock.isHeldByCurrentThread()) {
//                redLock.unlock();
//            }
//        }
//        return SingleResponse.buildFailure("推荐失败");
    }

    @Override
    public SingleResponse recommendLeader(RecommendLeaderCreateCmd recommendLeaderCreateCmd) {

        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wallet_address", recommendLeaderCreateCmd.getWalletAddress());
        Recommend recommend = recommendMapper.selectOne(queryWrapper);
        if (Objects.nonNull(recommend) && recommend.getRecommendType().equals(RecommendEnum.LEADER.getCode())) {
            return SingleResponse.buildFailure("已添加");
        }

        if (Objects.isNull(recommend)) {
            recommend = new Recommend();
            recommend.setLeaderWalletAddress(recommendLeaderCreateCmd.getWalletAddress());
            recommend.setWalletAddress(recommendLeaderCreateCmd.getWalletAddress());
            recommend.setRecommendType(RecommendEnum.LEADER.getCode());
            recommend.setCreateTime(System.currentTimeMillis());
            recommendMapper.insert(recommend);
        } else {
            recommend.setRecommendType(RecommendEnum.LEADER.getCode());
            recommendMapper.updateById(recommend);

            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("first_recommend_wallet_address", recommend.getWalletAddress());

            List<Recommend> recommends = recommendMapper.selectList(recommendQueryWrapper);
            for (Recommend childRecommend : recommends) {
                childRecommend.setRecommendType(RecommendEnum.LEADER.getCode());
                recommendMapper.updateById(childRecommend);
            }

            QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper<>();
            rebateConfigQueryWrapper.eq("address", recommend.getWalletAddress());

            rebateConfigMapper.delete(rebateConfigQueryWrapper);

        }

        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<RecommendListDTO> getRecommendList(RecommendListQryCmd recommendListQryCmd) {

        List<Recommend> recommends = new ArrayList<>();

        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();

        if (Objects.nonNull(recommendListQryCmd.getRecommendType())) {
            queryWrapper.eq("recommend_type", recommendListQryCmd.getRecommendType());
        }

        if (Objects.isNull(recommendListQryCmd.getWalletAddress())) {
            queryWrapper.isNull("first_recommend_wallet_address");

            recommends = recommendMapper.selectList(queryWrapper);

        } else {

            queryWrapper.eq("wallet_address", recommendListQryCmd.getWalletAddress());

            Recommend recommend = recommendMapper.selectOne(queryWrapper);

            if (Objects.isNull(recommend)) {
                return MultiResponse.of(Collections.emptyList());
            }
            recommends.add(recommend);
        }

        List<RecommendListDTO> recommendList = new ArrayList<>();
        for (Recommend recommend : recommends) {

            RecommendListDTO recommendListDTO = new RecommendListDTO();
            recommendListDTO.setWalletAddress(recommend.getWalletAddress());

            if (recommend.getRecommendType().equals(RecommendEnum.LEADER.getCode())){
                recommendListDTO.setLeader(Boolean.TRUE);
            }else {
                recommendListDTO.setLeader(Boolean.FALSE);
            }
            getRecommendList(recommendListDTO, recommend.getWalletAddress());
            recommendList.add(recommendListDTO);
        }

        return MultiResponse.of(recommendList);
    }

    @Override
    public SingleResponse<RecommendDTO> getRecommend(RecommendQryCmd recommendQryCmd) {

        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wallet_address", recommendQryCmd.getWalletAddress());
        Recommend recommend = recommendMapper.selectOne(queryWrapper);

        if (Objects.isNull(recommend)) {
            return SingleResponse.buildSuccess();
        }

        RecommendDTO recommendDTO = new RecommendDTO();
        BeanUtils.copyProperties(recommend, recommendDTO);

        return SingleResponse.of(recommendDTO);
    }


    private void getRecommendList(RecommendListDTO recommendListDTO, String walletAddress) {

        QueryWrapper<RewardDistributionRecord> odsRewardDistributionRecordQueryWrapper = new QueryWrapper<>();
        odsRewardDistributionRecordQueryWrapper.eq("wallet_address", walletAddress);
        odsRewardDistributionRecordQueryWrapper.eq("relation_address", recommendListDTO.getWalletAddress());
        odsRewardDistributionRecordQueryWrapper.eq("reward_type", RebateEnum.ODS.getCode());
        List<RewardDistributionRecord> odsRewardDistributionRecordList = rewardDistributionRecordMapper.selectList(odsRewardDistributionRecordQueryWrapper);
        if (!CollectionUtils.isEmpty(odsRewardDistributionRecordList)) {

            BigDecimal odsTotalReward = odsRewardDistributionRecordList.stream()
                    .map(RewardDistributionRecord::getRewardNumber)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.DOWN)
                    .add(new BigDecimal(recommendListDTO.getOdsRewardNumber()));

            recommendListDTO.setOdsRewardNumber(odsTotalReward.toString());
        }

        QueryWrapper<RewardDistributionRecord> usdtRewardDistributionRecordQueryWrapper = new QueryWrapper<>();
        usdtRewardDistributionRecordQueryWrapper.eq("wallet_address", walletAddress);
        usdtRewardDistributionRecordQueryWrapper.eq("relation_address", recommendListDTO.getWalletAddress());
        usdtRewardDistributionRecordQueryWrapper.eq("reward_type", RebateEnum.USDT.getCode());
        List<RewardDistributionRecord> usdtRewardDistributionRecordList = rewardDistributionRecordMapper.selectList(usdtRewardDistributionRecordQueryWrapper);
        if (!CollectionUtils.isEmpty(usdtRewardDistributionRecordList)) {

            BigDecimal usdtTotalReward = usdtRewardDistributionRecordList.stream()
                    .map(RewardDistributionRecord::getRewardNumber)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.DOWN)
                    .add(new BigDecimal(recommendListDTO.getUsdtRewardNumber()));

            recommendListDTO.setUsdtRewardNumber(usdtTotalReward.toString());
        }

        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recommend_wallet_address", recommendListDTO.getWalletAddress());

        List<Recommend> recommends = recommendMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(recommends)) {
            return;
        }
        List<RecommendListDTO> childRecommendList = new ArrayList<>();

        for (Recommend recommend : recommends) {

            recommendListDTO.setCount(recommendListDTO.getCount() + 1);

            RecommendListDTO childRecommend = new RecommendListDTO();
            childRecommend.setWalletAddress(recommend.getWalletAddress());
            childRecommendList.add(childRecommend);
            getRecommendList(childRecommend, walletAddress);
        }

        //recommendListDTO.setRewardNumber(childRecommendList.stream().map(RecommendListDTO::getRewardNumber).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add).toString());
        recommendListDTO.setCount(childRecommendList.stream().map(RecommendListDTO::getCount).reduce(Integer::sum).orElse(0) + childRecommendList.size());
        recommendListDTO.setRecommendList(childRecommendList);

    }


    public static void main(String[] args) {
        System.out.println(RandomStringUtils.random(7, true, true).toUpperCase());
    }
}

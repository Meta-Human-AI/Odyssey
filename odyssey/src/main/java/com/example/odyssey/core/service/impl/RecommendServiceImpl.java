package com.example.odyssey.core.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RecommendCoreCreateCmd;
import com.example.odyssey.bean.cmd.RecommendCreateCmd;
import com.example.odyssey.bean.cmd.RecommendLeaderCreateCmd;
import com.example.odyssey.bean.cmd.RecommendListQryCmd;
import com.example.odyssey.bean.dto.RecommendCoreDTO;
import com.example.odyssey.bean.dto.RecommendListDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.core.service.RecommendService;
import com.example.odyssey.model.entity.RebateConfig;
import com.example.odyssey.model.entity.Recommend;
import com.example.odyssey.model.entity.RecommendCoreLog;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import com.example.odyssey.model.mapper.RecommendCoreLogMapper;
import com.example.odyssey.model.mapper.RecommendMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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

    @Override
    public SingleResponse<RecommendCoreDTO> getRecommendCore(RecommendCoreCreateCmd recommendCoreCreateCmd) {


        RLock redLock = redissonClient.getLock("odyssey:recommend:" + recommendCoreCreateCmd.getWalletAddress());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                RecommendCoreDTO recommendCoreDTO = new RecommendCoreDTO();

                QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
                recommendQueryWrapper.eq("wallet_address",recommendCoreCreateCmd.getWalletAddress());
                Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

                if (Objects.nonNull(recommend)){
                    recommendCoreDTO.setRecommendWalletAddress(recommend.getRecommendWalletAddress());
                }else {

                    recommend = new Recommend();
                    recommend.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());
                    recommend.setCreateTime(System.currentTimeMillis());
                    recommend.setRecommendType(RecommendEnum.NORMAL.getCode());
                    recommend.setLeaderWalletAddress(recommendCoreCreateCmd.getWalletAddress());
                    recommendMapper.insert(recommend);

                    QueryWrapper<SystemConfig> odsFirstRebateRateWrapper = new QueryWrapper<>();
                    odsFirstRebateRateWrapper.eq("`key`", "ods_first_rebate_rate");

                    SystemConfig odsFirstRebateRate = systemConfigMapper.selectOne(odsFirstRebateRateWrapper);

                    QueryWrapper<SystemConfig> odsSecondRebateRateWrapper = new QueryWrapper<>();
                    odsSecondRebateRateWrapper.eq("`key`", "ods_second_rebate_rate");

                    SystemConfig odsSecondRebateRate = systemConfigMapper.selectOne(odsSecondRebateRateWrapper);

                    QueryWrapper<SystemConfig> usdtFirstRebateRateWrapper = new QueryWrapper<>();
                    usdtFirstRebateRateWrapper.eq("`key`", "usdt_first_rebate_rate");

                    SystemConfig usdtFirstRebateRate = systemConfigMapper.selectOne(usdtFirstRebateRateWrapper);

                    QueryWrapper<SystemConfig> usdtSecondRebateRateWrapper = new QueryWrapper<>();
                    usdtSecondRebateRateWrapper.eq("`key`", "usdt_second_rebate_rate");

                    SystemConfig usdtSecondRebateRate = systemConfigMapper.selectOne(usdtSecondRebateRateWrapper);

                    RebateConfig odRebateConfig = new RebateConfig();
                    odRebateConfig.setAddress(recommendCoreCreateCmd.getWalletAddress());
                    odRebateConfig.setFirstRebateRate(Objects.isNull(odsFirstRebateRate) ? "0.02" : odsFirstRebateRate.getValue());
                    odRebateConfig.setSecondRebateRate(Objects.isNull(odsSecondRebateRate) ? "0.1" : odsSecondRebateRate.getValue());
                    odRebateConfig.setRecommendType(RecommendEnum.NORMAL.getCode());
                    odRebateConfig.setRebateType(RebateEnum.ODS.getCode());

                    RebateConfig usdtRebateConfig = new RebateConfig();
                    usdtRebateConfig.setAddress(recommendCoreCreateCmd.getWalletAddress());
                    usdtRebateConfig.setFirstRebateRate(Objects.isNull(usdtFirstRebateRate) ? "0.02" : usdtFirstRebateRate.getValue());
                    usdtRebateConfig.setSecondRebateRate(Objects.isNull(usdtSecondRebateRate) ? "0.1" : usdtSecondRebateRate.getValue());
                    usdtRebateConfig.setRecommendType(RecommendEnum.NORMAL.getCode());
                    usdtRebateConfig.setRebateType(RebateEnum.USDT.getCode());

                    rebateConfigMapper.insert(odRebateConfig);
                    rebateConfigMapper.insert(usdtRebateConfig);

                    recommendCoreDTO.setRecommendWalletAddress("");
                }

                QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
                recommendCoreLogQueryWrapper.eq("wallet_address", recommendCoreCreateCmd.getWalletAddress());
                RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);
                if (Objects.nonNull(recommendCoreLog)) {
                    recommendCoreDTO.setRecommendCore(recommendCoreLog.getRecommendCore());
                    return SingleResponse.of(recommendCoreDTO);
                }

                recommendCoreLog = new RecommendCoreLog();
                recommendCoreLog.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());

                String randomAlphabetic;

                while (true) {

                    randomAlphabetic = RandomStringUtils.random(7, true, true).toUpperCase();

                    QueryWrapper<RecommendCoreLog> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("recommend_core", randomAlphabetic);

                    Long count = recommendCoreLogMapper.selectCount(queryWrapper);
                    if (count == 0) {
                        break;
                    }
                }

                recommendCoreLog.setRecommendCore(randomAlphabetic);
                recommendCoreLog.setCreateTime(System.currentTimeMillis());
//                recommendCoreLog.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
                recommendCoreLogMapper.insert(recommendCoreLog);

                QueryWrapper<SystemConfig> systemQueryWrapper = new QueryWrapper<>();

                systemQueryWrapper.eq("`key`", "url");

                SystemConfig systemConfig = systemConfigMapper.selectOne(systemQueryWrapper);

                if (Objects.isNull(systemConfig)) {
                    return SingleResponse.buildFailure("URL configuration not found");
                }

                recommendCoreDTO.setRecommendUrl(systemConfig.getValue() + "/odyssey/v1/recommend/add?core=" + randomAlphabetic);

                recommendCoreDTO.setRecommendCore(randomAlphabetic);

                return SingleResponse.of(recommendCoreDTO);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return SingleResponse.buildFailure("获取推荐码失败");

        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("获取推荐码失败");
    }

    @Override
    public SingleResponse recommend(RecommendCreateCmd recommendCreateCmd) {

        RLock redLock = redissonClient.getLock("odyssey:recommend:" + recommendCreateCmd.getWalletAddress());


        try {
            //推荐人
//            Object recommendWalletAddress = redisTemplate.opsForValue().get(recommendCreateCmd.getRecommendCore());
//            if (Objects.isNull(recommendWalletAddress)) {
//                return SingleResponse.buildFailure("推荐码不存在或已过期");
//            }
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
                recommendCoreLogQueryWrapper.eq("recommend_core", recommendCreateCmd.getRecommendCore());
                RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);

                if (Objects.isNull(recommendCoreLog)) {
                    return SingleResponse.buildFailure("推荐码不存在");
                }

                QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("wallet_address", recommendCreateCmd.getWalletAddress());
                Recommend recommend = recommendMapper.selectOne(queryWrapper);

                if (Objects.nonNull(recommend) && Objects.nonNull(recommend.getRecommendWalletAddress())) {
                    return SingleResponse.buildFailure("已推荐过");
                } else {
                    //查询推荐人的推荐关系
                    QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
                    recommendQueryWrapper.eq("wallet_address", recommendCoreLog.getWalletAddress());
                    Recommend oldRecommend = recommendMapper.selectOne(recommendQueryWrapper);

                    if (Objects.isNull(oldRecommend)) {
                        return SingleResponse.buildFailure("推荐人不存在");
                    }

                    if (oldRecommend.getRecommendWalletAddress().equals(recommendCreateCmd.getWalletAddress())) {
                        return SingleResponse.buildFailure("不能循环推荐");
                    }

                    if (Objects.isNull(recommend)) {
                        recommend = new Recommend();
                    }

                    recommend.setWalletAddress(recommendCreateCmd.getWalletAddress());
                    recommend.setRecommendWalletAddress(recommendCoreLog.getWalletAddress());
                    recommend.setRecommendCode(recommendCreateCmd.getRecommendCore());
                    recommend.setLeaderWalletAddress(oldRecommend.getLeaderWalletAddress());

                    if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress()) || Objects.isNull(oldRecommend.getSecondRecommendWalletAddress())) {
                        if (Objects.isNull(oldRecommend.getFirstRecommendWalletAddress())) {
                            recommend.setFirstRecommendWalletAddress(oldRecommend.getWalletAddress());
                        } else {
                            recommend.setFirstRecommendWalletAddress(oldRecommend.getFirstRecommendWalletAddress());
                            recommend.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                        }
                        recommend.setRecommendType(Objects.isNull(oldRecommend.getRecommendType()) ? RecommendEnum.LEADER.getCode() : oldRecommend.getRecommendType());
                    } else {
                        recommend.setRecommendType(RecommendEnum.NORMAL.getCode());
                        recommend.setSecondRecommendWalletAddress(oldRecommend.getWalletAddress());
                        recommend.setFirstRecommendWalletAddress(oldRecommend.getSecondRecommendWalletAddress());
                    }

                    recommend.setCreateTime(System.currentTimeMillis());

                    if (Objects.isNull(recommend.getId())) {
                        recommendMapper.insert(recommend);
                    } else {
                        recommendMapper.updateById(recommend);

                        QueryWrapper<RebateConfig> rebateConfigQueryWrapper = new QueryWrapper<>();
                        rebateConfigQueryWrapper.eq("address", recommend.getWalletAddress());

                        rebateConfigMapper.delete(rebateConfigQueryWrapper);
                    }
                }


                return SingleResponse.buildSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SingleResponse.buildFailure("推荐失败");
        }finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("推荐失败");
    }

    @Override
    public SingleResponse recommendLeader(RecommendLeaderCreateCmd recommendLeaderCreateCmd) {

        QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wallet_address", recommendLeaderCreateCmd.getWalletAddress());
        Long count = recommendMapper.selectCount(queryWrapper);
        if (count > 0) {
            return SingleResponse.buildFailure("已添加");
        }

        Recommend recommend = new Recommend();
        recommend.setLeaderWalletAddress(recommendLeaderCreateCmd.getWalletAddress());
        recommend.setWalletAddress(recommendLeaderCreateCmd.getWalletAddress());
        recommend.setRecommendType(RecommendEnum.LEADER.getCode());
        recommend.setCreateTime(System.currentTimeMillis());

        recommendMapper.insert(recommend);
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

            getRecommendList(recommendListDTO);
            recommendList.add(recommendListDTO);
        }

        return MultiResponse.of(recommendList);
    }


    private void getRecommendList(RecommendListDTO recommendListDTO) {

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
            getRecommendList(childRecommend);
        }

        recommendListDTO.setCount(childRecommendList.stream().map(RecommendListDTO::getCount).reduce(Integer::sum).orElse(0) + childRecommendList.size());
        recommendListDTO.setRecommendList(childRecommendList);

    }


    public static void main(String[] args) {
        System.out.println(RandomStringUtils.random(7, true, true).toUpperCase());
    }
}

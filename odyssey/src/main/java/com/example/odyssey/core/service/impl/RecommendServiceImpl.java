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
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.core.service.RecommendService;
import com.example.odyssey.model.entity.Recommend;
import com.example.odyssey.model.entity.RecommendCoreLog;
import com.example.odyssey.model.entity.SystemConfig;
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
    RedisTemplate redisTemplate;
    @Resource
    RecommendCoreLogMapper recommendCoreLogMapper;
    @Resource
    RecommendMapper recommendMapper;

    @Resource
    SystemConfigMapper systemConfigMapper;

    @Override
    public SingleResponse<RecommendCoreDTO> getRecommendCore(RecommendCoreCreateCmd recommendCoreCreateCmd) {


        RLock redLock = redissonClient.getLock("odyssey:recommend:core:" + recommendCoreCreateCmd.getWalletAddress());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                RecommendCoreDTO recommendCoreDTO = new RecommendCoreDTO();

                Object recommendCore = redisTemplate.opsForValue().get(recommendCoreCreateCmd.getWalletAddress());
                if (Objects.nonNull(recommendCore)) {
                    recommendCoreDTO.setRecommendCore(recommendCore.toString());
                    return SingleResponse.of(recommendCoreDTO);
                }

                RecommendCoreLog recommendCoreLog = new RecommendCoreLog();
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

                systemQueryWrapper.eq("key", "ods_url");

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

        try {
            //推荐人
//            Object recommendWalletAddress = redisTemplate.opsForValue().get(recommendCreateCmd.getRecommendCore());
//            if (Objects.isNull(recommendWalletAddress)) {
//                return SingleResponse.buildFailure("推荐码不存在或已过期");
//            }

            QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
            recommendCoreLogQueryWrapper.eq("recommend_core", recommendCreateCmd.getRecommendCore());
            RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);

            if (Objects.nonNull(recommendCoreLog)){
                return SingleResponse.buildFailure("推荐码不存在");
            }

            QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wallet_address", recommendCreateCmd.getWalletAddress());
            Long count = recommendMapper.selectCount(queryWrapper);

            if (count > 0) {
                return SingleResponse.buildFailure("已推荐过");
            }

            //查询推荐人的推荐关系
            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("wallet_address", recommendCoreLog.getWalletAddress());
            Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

            if (Objects.isNull(recommend)) {
                return SingleResponse.buildFailure("推荐人不存在");
            }

            Recommend newRecommend = new Recommend();
            newRecommend.setWalletAddress(recommendCreateCmd.getWalletAddress());
            newRecommend.setRecommendWalletAddress( recommendCoreLog.getWalletAddress()    );
            newRecommend.setRecommendCode(recommendCreateCmd.getRecommendCore());
            newRecommend.setLeaderWalletAddress(recommend.getLeaderWalletAddress());


            if (Objects.isNull(recommend.getFirstRecommendWalletAddress()) || Objects.isNull(recommend.getSecondRecommendWalletAddress())) {
                if (Objects.isNull(recommend.getFirstRecommendWalletAddress())) {
                    newRecommend.setFirstRecommendWalletAddress(recommend.getWalletAddress());
                } else {
                    newRecommend.setFirstRecommendWalletAddress(recommend.getFirstRecommendWalletAddress());
                    newRecommend.setSecondRecommendWalletAddress(recommend.getWalletAddress());
                }
                newRecommend.setRecommendType(RecommendEnum.LEADER.getCode());
            } else {
                newRecommend.setRecommendType(RecommendEnum.NORMAL.getCode());
                newRecommend.setSecondRecommendWalletAddress(recommend.getWalletAddress());
                newRecommend.setFirstRecommendWalletAddress(recommend.getFirstRecommendWalletAddress());
            }
            newRecommend.setCreateTime(System.currentTimeMillis());
            recommendMapper.insert(newRecommend);

            return SingleResponse.buildSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return SingleResponse.buildFailure("推荐失败");
        }
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
        recommend.setCreateTime(System.currentTimeMillis());

        recommendMapper.insert(recommend);
        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<RecommendListDTO> getRecommendList(RecommendListQryCmd recommendListQryCmd) {

        List<Recommend> recommends = new ArrayList<>();
        if (Objects.isNull(recommendListQryCmd.getWalletAddress())) {
            QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNull("first_recommend_wallet_address");
            recommends = recommendMapper.selectList(queryWrapper);

        } else {
            QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wallet_address", recommendListQryCmd.getWalletAddress());
            Recommend recommend = recommendMapper.selectOne(queryWrapper);

            Assert.notNull(recommend, "钱包未注册");
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
        queryWrapper.eq("first_recommend_wallet_address", recommendListDTO.getWalletAddress());

        List<Recommend> recommends = recommendMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(recommends)) {
            return;
        }
        List<RecommendListDTO> childRecommendList = new ArrayList<>();

        for (Recommend recommend : recommends) {

            RecommendListDTO childRecommend = new RecommendListDTO();
            childRecommend.setWalletAddress(recommend.getWalletAddress());
            childRecommendList.add(recommendListDTO);

            getRecommendList(childRecommend);
        }
        recommendListDTO.setRecommendList(childRecommendList);

    }


    public static void main(String[] args) {
        System.out.println(RandomStringUtils.random(7, true, true).toUpperCase());
    }
}

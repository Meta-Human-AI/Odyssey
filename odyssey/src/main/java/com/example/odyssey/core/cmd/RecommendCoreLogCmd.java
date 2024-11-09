package com.example.odyssey.core.cmd;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.cmd.RecommendCoreCreateCmd;
import com.example.odyssey.model.entity.Recommend;
import com.example.odyssey.model.entity.RecommendCoreLog;
import com.example.odyssey.model.mapper.RecommendCoreLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RecommendCoreLogCmd {

    @Resource
    RedissonClient redissonClient;

    @Resource
    RecommendCoreLogMapper recommendCoreLogMapper;

    private static final String RECOMMEND_CORE_LOCK_KEY = "odyssey:recommend:core:";

    public synchronized RecommendCoreLog createRecommendCoreLog(RecommendCoreCreateCmd recommendCoreCreateCmd){

        // 1. 获取锁实例
        RLock lock = redissonClient.getLock(RECOMMEND_CORE_LOCK_KEY + recommendCoreCreateCmd.getWalletAddress());

        try {
            // 2. 尝试获取锁
            // waitTime: 等待获取锁的最大时间
            // 不设置 leaseTime，使用看门狗机制自动续期
            boolean isLocked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取RecommendCoreLog锁失败, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress());
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            log.info("成功获取RecommendCoreLog锁, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress());

            // 3. 执行业务逻辑
            QueryWrapper<RecommendCoreLog> recommendCoreLogQueryWrapper = new QueryWrapper<>();
            recommendCoreLogQueryWrapper.eq("wallet_address", recommendCoreCreateCmd.getWalletAddress());
            RecommendCoreLog recommendCoreLog = recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);

            if (Objects.nonNull(recommendCoreLog)){
                return recommendCoreLog;
            }

            recommendCoreLog = new RecommendCoreLog();

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
            recommendCoreLog.setWalletAddress(recommendCoreCreateCmd.getWalletAddress());
            recommendCoreLog.setCreateTime(System.currentTimeMillis());

            try {
                recommendCoreLogMapper.insert(recommendCoreLog);
                log.info("创建推荐码成功, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress());
                return recommendCoreLog;
            } catch (DuplicateKeyException e) {
                // 处理并发插入导致的唯一索引冲突
                log.warn("并发创建推荐码成功, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress());
                return recommendCoreLogMapper.selectOne(recommendCoreLogQueryWrapper);
            }

        } catch (InterruptedException e) {
            log.error("createRecommendCoreLog 获取锁被中断, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress(), e);
            Thread.currentThread().interrupt(); // 重置中断标志
            throw new RuntimeException("系统异常，请稍后重试");
        } finally {
            // 4. 释放锁
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("createRecommendCoreLog 释放锁成功, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress());
                }
            } catch (Exception e) {
                log.error("createRecommendCoreLog 释放锁异常, walletAddress: {}", recommendCoreCreateCmd.getWalletAddress(), e);
            }
        }

    }

}

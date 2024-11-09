package com.example.odyssey.core.cmd;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.cmd.RecommendCreateCmd;
import com.example.odyssey.bean.cmd.RecommendUpdateCmd;
import com.example.odyssey.core.service.RebateConfigService;
import com.example.odyssey.model.entity.Recommend;
import com.example.odyssey.model.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RecommendCmd {

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


    private static final String RECOMMEND_LOCK_KEY = "odyssey:recommend:wallet:";

    /**
     * 创建recommend
     *
     * @param recommendCreateCmd
     * @return
     */
    public synchronized Recommend createRecommend(RecommendCreateCmd recommendCreateCmd) {

        // 1. 获取锁实例
        RLock lock = redissonClient.getLock(RECOMMEND_LOCK_KEY + recommendCreateCmd.getWalletAddress());

        try {
            // 2. 尝试获取锁
            // waitTime: 等待获取锁的最大时间
            // 不设置 leaseTime，使用看门狗机制自动续期
            boolean isLocked = lock.tryLock(5, TimeUnit.SECONDS);

            System.out.println(recommendCreateCmd.getWalletAddress() + "获取锁 : " + isLocked);
            if (!isLocked) {
                log.warn("获取锁失败, walletAddress: {}", recommendCreateCmd.getWalletAddress());
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            log.info("成功获取锁, walletAddress: {}", recommendCreateCmd.getWalletAddress());

            // 3. 执行业务逻辑
            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("wallet_address", recommendCreateCmd.getWalletAddress());
            Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

            if (Objects.nonNull(recommend)) {
                return recommend;
            }

            recommend = new Recommend();
            recommend.setRecommendType(recommendCreateCmd.getRecommendType());
            recommend.setRecommendCode(recommendCreateCmd.getRecommendCore());
            recommend.setCreateTime(System.currentTimeMillis());
            recommend.setFirstRecommendWalletAddress(recommendCreateCmd.getFirstRecommendWalletAddress());
            recommend.setSecondRecommendWalletAddress(recommendCreateCmd.getSecondRecommendWalletAddress());
            recommend.setLeaderWalletAddress(recommendCreateCmd.getLeaderWalletAddress());
            recommend.setWalletAddress(recommendCreateCmd.getWalletAddress());
            recommend.setRecommendWalletAddress(recommendCreateCmd.getRecommendWalletAddress());

            try {
                recommendMapper.insert(recommend);
                log.info("创建游客推荐记录成功, walletAddress: {}", recommendCreateCmd.getWalletAddress());
                return recommend;
            } catch (DuplicateKeyException e) {
                // 处理并发插入导致的唯一索引冲突
                log.warn("并发创建游客推荐记录, walletAddress: {}", recommendCreateCmd.getWalletAddress());
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

        } catch (InterruptedException e) {
            log.error("获取锁被中断, walletAddress: {}", recommendCreateCmd.getWalletAddress(), e);
            Thread.currentThread().interrupt(); // 重置中断标志
            throw new RuntimeException("系统异常，请稍后重试");
        } finally {
            // 4. 释放锁
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("释放锁成功, walletAddress: {}", recommendCreateCmd.getWalletAddress());
                }
            } catch (Exception e) {
                log.error("释放锁异常, walletAddress: {}", recommendCreateCmd.getWalletAddress(), e);
            }
        }
    }

    public Recommend updateRecommend(RecommendUpdateCmd recommendUpdateCmd) {

        // 1. 获取锁实例
        RLock lock = redissonClient.getLock(RECOMMEND_LOCK_KEY + recommendUpdateCmd.getWalletAddress());

        try {
            // 2. 尝试获取锁
            // waitTime: 等待获取锁的最大时间
            // 不设置 leaseTime，使用看门狗机制自动续期
            boolean isLocked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("updateRecommend 获取锁失败, walletAddress: {}", recommendUpdateCmd.getWalletAddress());
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            log.info("updateRecommend 成功获取锁, walletAddress: {}", recommendUpdateCmd.getWalletAddress());

            // 3. 执行业务逻辑
            QueryWrapper<Recommend> recommendQueryWrapper = new QueryWrapper<>();
            recommendQueryWrapper.eq("wallet_address", recommendUpdateCmd.getWalletAddress());
            Recommend recommend = recommendMapper.selectOne(recommendQueryWrapper);

            if (Objects.isNull(recommend)){
                throw new RuntimeException("钱包地址不存在");
            }

            recommend.setRecommendType(recommendUpdateCmd.getRecommendType());
            recommend.setRecommendCode(recommendUpdateCmd.getRecommendCore());
            recommend.setFirstRecommendWalletAddress(recommendUpdateCmd.getFirstRecommendWalletAddress());
            recommend.setSecondRecommendWalletAddress(recommendUpdateCmd.getSecondRecommendWalletAddress());
            recommend.setLeaderWalletAddress(recommendUpdateCmd.getLeaderWalletAddress());
            recommend.setWalletAddress(recommendUpdateCmd.getWalletAddress());
            recommend.setRecommendWalletAddress(recommendUpdateCmd.getRecommendWalletAddress());

            try {
                recommendMapper.updateById(recommend);
                log.info("updateRecommend 更新游客推荐记录成功, walletAddress: {}", recommendUpdateCmd.getWalletAddress());
                return recommend;
            } catch (DuplicateKeyException e) {
                // 处理并发插入导致的唯一索引冲突
                log.warn("updateRecommend 并发更新游客推荐记录, walletAddress: {}", recommendUpdateCmd.getWalletAddress());
                throw new RuntimeException("更新钱包地址信息错误");
            }

        } catch (InterruptedException e) {
            log.error("updateRecommend 获取锁被中断, walletAddress: {}", recommendUpdateCmd.getWalletAddress(), e);
            Thread.currentThread().interrupt(); // 重置中断标志
            throw new RuntimeException("系统异常，请稍后重试");
        } finally {
            // 4. 释放锁
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("updateRecommend 释放锁成功, walletAddress: {}", recommendUpdateCmd.getWalletAddress());
                }
            } catch (Exception e) {
                log.error("updateRecommend 释放锁异常, walletAddress: {}", recommendUpdateCmd.getWalletAddress(), e);
            }
        }
    }
}

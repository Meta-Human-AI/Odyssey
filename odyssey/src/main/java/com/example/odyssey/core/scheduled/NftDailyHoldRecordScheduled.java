package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.model.entity.NftDailyHoldRecord;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.entity.OdsConfig;
import com.example.odyssey.model.entity.Recommend;
import com.example.odyssey.model.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NftDailyHoldRecordScheduled {

    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    OdsConfigMapper odsConfigMapper;
    @Resource
    RecommendMapper recommendMapper;
    @Resource
    NftDailyHoldRecordMapper nftDailyHoldRecordMapper;

    /**
     * 每天23.58分执行
     */
    @Scheduled(cron = "0 58 23 * * ?")
    public void transfer() {

        log.info("NftDailyHoldRecordScheduled 开始执行");

        QueryWrapper<OdsConfig> odsConfigQueryWrapper = new QueryWrapper();

        List<OdsConfig> odsConfigList = odsConfigMapper.selectList(odsConfigQueryWrapper);
        if (odsConfigList.isEmpty()) {
            log.info("NftDailyHoldRecordScheduled 结束执行");
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
            BigDecimal number = BigDecimal.valueOf(odsConfig.getNumber()).divide(BigDecimal.valueOf(nftMessages.size()), 8, RoundingMode.HALF_UP);

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            for (NftMessage nftMessage : nftMessages) {

                QueryWrapper<NftDailyHoldRecord> nftDailyHoldRecordQueryWrapper = new QueryWrapper<>();
                nftDailyHoldRecordQueryWrapper.eq("token_id", nftMessage.getTokenId());
                nftDailyHoldRecordQueryWrapper.eq("wallet_address", nftMessage.getNewAddress());
                nftDailyHoldRecordQueryWrapper.eq("date", today);

                Long count = nftDailyHoldRecordMapper.selectCount(nftDailyHoldRecordQueryWrapper);
                if(count > 0){
                    continue;
                }

                NftDailyHoldRecord nftDailyHoldRecord = new NftDailyHoldRecord();
                nftDailyHoldRecord.setTokenId(nftMessage.getTokenId());
                nftDailyHoldRecord.setWalletAddress(nftMessage.getNewAddress());
                nftDailyHoldRecord.setNumber(number.toString());
                nftDailyHoldRecord.setTransferTime(nftMessage.getTransferTime());
                nftDailyHoldRecord.setDate(today);

                QueryWrapper<Recommend> queryWrapper = new QueryWrapper<>();

                if (nftMessage.getNewAddress().equals(nftMessage.getBuyAddress())) {
                    queryWrapper.eq("wallet_address", nftMessage.getNewAddress());
                } else {
                    queryWrapper.eq("wallet_address", nftMessage.getBuyAddress());
                }
                Recommend recommend = recommendMapper.selectOne(queryWrapper);
                if (Objects.nonNull(recommend)) {
                    nftDailyHoldRecord.setFirstRecommendWalletAddress(recommend.getFirstRecommendWalletAddress());
                    nftDailyHoldRecord.setSecondRecommendWalletAddress(recommend.getSecondRecommendWalletAddress());
                    nftDailyHoldRecord.setRecommendType(recommend.getRecommendType());
                    nftDailyHoldRecord.setLeaderWalletAddress(recommend.getLeaderWalletAddress());
                }

                nftDailyHoldRecordMapper.insert(nftDailyHoldRecord);
            }

        }

        log.info("NftDailyHoldRecordScheduled 结束执行");
    }
}

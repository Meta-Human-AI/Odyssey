package com.example.odyssey.core.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.common.NftLevelEnum;
import com.example.odyssey.model.entity.NftDailyStatistics;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.mapper.NftDailyStatisticsMapper;
import com.example.odyssey.model.mapper.NftMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NftStatisticsScheduled {

    @Resource
    private NftMessageMapper nftMessageMapper;

    @Resource
    private NftDailyStatisticsMapper nftDailyStatisticsMapper;

    /**
     * 每3分钟统计一次
     */
    @Scheduled(cron = "0 */3 * * * *")
    public void statisticsNftCount() {
        try {
            log.info("开始统计NFT数量");
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String todayStart = today + " 00:00:00";
            String todayEnd = today + " 23:59:59";
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 1. 统计当前总量
            Map<String, Long> totalMap = new HashMap<>();
            for (NftLevelEnum level : NftLevelEnum.values()) {
                Long count = nftMessageMapper.selectCount(new QueryWrapper<NftMessage>().eq("type", level.getName()));
                totalMap.put(level.getName(), count);
            }

            // 2. 统计今日新增(根据transfer_time判断)
            Map<String, Long> newMap = new HashMap<>();
            for (NftLevelEnum level : NftLevelEnum.values()) {
                Long count = nftMessageMapper.selectCount(new QueryWrapper<NftMessage>().eq("type", level.getName()).between("transfer_time", todayStart, todayEnd));
                newMap.put(level.getName(), count);
            }

            // 3. 更新统计记录
            NftDailyStatistics statistics = nftDailyStatisticsMapper.selectOne(new QueryWrapper<NftDailyStatistics>().eq("date", today));

            if (statistics == null) {
                statistics = NftDailyStatistics.builder().date(today).build();
            }

            // 4. 设置统计数据
            statistics.setOlTotal(totalMap.get(NftLevelEnum.OL.getName()).intValue());
            statistics.setOsTotal(totalMap.get(NftLevelEnum.OS.getName()).intValue());
            statistics.setOaTotal(totalMap.get(NftLevelEnum.OA.getName()).intValue());
            statistics.setObTotal(totalMap.get(NftLevelEnum.OB.getName()).intValue());

            statistics.setOlNew(newMap.get(NftLevelEnum.OL.getName()).intValue());
            statistics.setOsNew(newMap.get(NftLevelEnum.OS.getName()).intValue());
            statistics.setOaNew(newMap.get(NftLevelEnum.OA.getName()).intValue());
            statistics.setObNew(newMap.get(NftLevelEnum.OB.getName()).intValue());

            statistics.setUpdateTime(now);

            // 5. 保存或更新
            if (statistics.getId() == null) {
                nftDailyStatisticsMapper.insert(statistics);
            } else {
                nftDailyStatisticsMapper.updateById(statistics);
            }

            log.info("NFT数量统计完成");

        } catch (Exception e) {
            log.error("NFT数量统计失败", e);
        }
    }
}

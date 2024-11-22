package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigCreateCmd;
import com.example.odyssey.bean.cmd.RebateConfigCreateDefaultCmd;
import com.example.odyssey.bean.cmd.RebateConfigListQryCmd;
import com.example.odyssey.bean.cmd.RebateConfigUpdateCmd;
import com.example.odyssey.bean.dto.RebateConfigDTO;
import com.example.odyssey.common.RebateEnum;
import com.example.odyssey.common.RecommendEnum;
import com.example.odyssey.core.service.RebateConfigService;
import com.example.odyssey.model.entity.RebateConfig;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RebateConfigServiceImpl implements RebateConfigService {

    @Resource
    RebateConfigMapper rebateConfigMapper;

    @Resource
    SystemConfigMapper systemConfigMapper;

    @Override
    public synchronized SingleResponse defaultAdd(RebateConfigCreateDefaultCmd rebateConfigCreateDefaultCmd) {

        QueryWrapper<SystemConfig> odsFirstRebateRateWrapper = new QueryWrapper<>();
        odsFirstRebateRateWrapper.eq("`key`", "ods_first_rebate_rate");

        SystemConfig odsFirstRebateRate = systemConfigMapper.selectOne(odsFirstRebateRateWrapper);

        QueryWrapper<SystemConfig> odsSecondRebateRateWrapper = new QueryWrapper<>();
        odsSecondRebateRateWrapper.eq("`key`", "ods_second_rebate_rate");

        SystemConfig odsSecondRebateRate = systemConfigMapper.selectOne(odsSecondRebateRateWrapper);


        QueryWrapper<SystemConfig> odsThreeRebateRateWrapper = new QueryWrapper<>();
        odsThreeRebateRateWrapper.eq("`key`", "ods_three_rebate_rate");

        SystemConfig odsThreeRebateRate = systemConfigMapper.selectOne(odsThreeRebateRateWrapper);

        QueryWrapper<SystemConfig> usdtFirstRebateRateWrapper = new QueryWrapper<>();
        usdtFirstRebateRateWrapper.eq("`key`", "usdt_first_rebate_rate");

        SystemConfig usdtFirstRebateRate = systemConfigMapper.selectOne(usdtFirstRebateRateWrapper);

        QueryWrapper<SystemConfig> usdtSecondRebateRateWrapper = new QueryWrapper<>();
        usdtSecondRebateRateWrapper.eq("`key`", "usdt_second_rebate_rate");

        SystemConfig usdtSecondRebateRate = systemConfigMapper.selectOne(usdtSecondRebateRateWrapper);

        QueryWrapper<SystemConfig> usdtThreeRebateRateWrapper = new QueryWrapper<>();
        usdtThreeRebateRateWrapper.eq("`key`", "usdt_three_rebate_rate");

        SystemConfig usdtThreeRebateRate = systemConfigMapper.selectOne(usdtThreeRebateRateWrapper);

        RebateConfigCreateCmd odRebateConfig = new RebateConfigCreateCmd();
        odRebateConfig.setAddress(rebateConfigCreateDefaultCmd.getAddress());
        odRebateConfig.setFirstRebateRate(Objects.isNull(odsFirstRebateRate) ? "0.1" : odsFirstRebateRate.getValue());
        odRebateConfig.setSecondRebateRate(Objects.isNull(odsSecondRebateRate) ? "0.1" : odsSecondRebateRate.getValue());
        odRebateConfig.setThreeRebateRate(Objects.isNull(odsThreeRebateRate) ? "0.02" : odsThreeRebateRate.getValue());
        odRebateConfig.setRecommendType(RecommendEnum.NORMAL.getCode());

        odRebateConfig.setRebateType(RebateEnum.ODS.getCode());

        RebateConfigCreateCmd usdtRebateConfig = new RebateConfigCreateCmd();
        usdtRebateConfig.setAddress(rebateConfigCreateDefaultCmd.getAddress());
        usdtRebateConfig.setFirstRebateRate(Objects.isNull(usdtFirstRebateRate) ? "0.1" : usdtFirstRebateRate.getValue());
        usdtRebateConfig.setSecondRebateRate(Objects.isNull(usdtSecondRebateRate) ? "0.1" : usdtSecondRebateRate.getValue());
        usdtRebateConfig.setThreeRebateRate(Objects.isNull(usdtThreeRebateRate) ? "0.02" : usdtThreeRebateRate.getValue());
        usdtRebateConfig.setRecommendType(RecommendEnum.NORMAL.getCode());
        usdtRebateConfig.setRebateType(RebateEnum.USDT.getCode());

        add(odRebateConfig);

        add(usdtRebateConfig);


        return SingleResponse.buildSuccess();
    }

    @Override
    public synchronized SingleResponse add(RebateConfigCreateCmd rebateConfigCreateCmd) {

        QueryWrapper<RebateConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("address", rebateConfigCreateCmd.getAddress());
        queryWrapper.eq("recommend_type", rebateConfigCreateCmd.getRecommendType());
        queryWrapper.eq("rebate_type", rebateConfigCreateCmd.getRebateType());

        RebateConfig rebateConfig = rebateConfigMapper.selectOne(queryWrapper);
        if (Objects.nonNull(rebateConfig)) {
            return SingleResponse.buildFailure("配置已存在");
        }

        rebateConfig = new RebateConfig();
        BeanUtils.copyProperties(rebateConfigCreateCmd, rebateConfig);

        try {
            rebateConfigMapper.insert(rebateConfig);
        } catch (DuplicateKeyException e) {
            // 处理并发插入导致的唯一索引冲突
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse update(RebateConfigUpdateCmd rebateConfigUpdateCmd) {

        RebateConfig rebateConfig = rebateConfigMapper.selectById(rebateConfigUpdateCmd.getId());
        if (rebateConfig == null) {
            return SingleResponse.buildFailure("配置不存在");
        }

        rebateConfig.setFirstRebateRate(rebateConfigUpdateCmd.getFirstRebateRate());
        rebateConfig.setSecondRebateRate(rebateConfigUpdateCmd.getSecondRebateRate());
        rebateConfig.setThreeRebateRate(rebateConfigUpdateCmd.getThreeRebateRate());
        rebateConfigMapper.updateById(rebateConfig);

        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<RebateConfigDTO> listRebateConfig(RebateConfigListQryCmd rebateConfigListQryCmd) {


        List<RebateConfigDTO> rebateConfigDTOList = new ArrayList<>();

        QueryWrapper<RebateConfig> queryWrapper = new QueryWrapper<>();

        if (Objects.nonNull(rebateConfigListQryCmd.getAddress())) {
            queryWrapper.eq("address", rebateConfigListQryCmd.getAddress());
        }

        if (Objects.nonNull(rebateConfigListQryCmd.getRecommendType())) {
            queryWrapper.eq("recommend_type", rebateConfigListQryCmd.getRecommendType());
        }

        if (Objects.nonNull(rebateConfigListQryCmd.getRebateType())) {
            queryWrapper.eq("rebate_type", rebateConfigListQryCmd.getRebateType());
        }


        Page<RebateConfig> rebateConfigPage = rebateConfigMapper.selectPage(Page.of(rebateConfigListQryCmd.getPageNum(), rebateConfigListQryCmd.getPageSize()), queryWrapper);


        for (RebateConfig rebateConfig : rebateConfigPage.getRecords()) {

            RebateConfigDTO rebateConfigDTO = new RebateConfigDTO();
            BeanUtils.copyProperties(rebateConfig, rebateConfigDTO);
            rebateConfigDTOList.add(rebateConfigDTO);
        }
        return MultiResponse.of(rebateConfigDTOList, (int) rebateConfigPage.getTotal());
    }

}

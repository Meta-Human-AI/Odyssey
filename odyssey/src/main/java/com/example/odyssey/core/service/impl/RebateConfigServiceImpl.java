package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigCreateCmd;
import com.example.odyssey.bean.cmd.RebateConfigListQryCmd;
import com.example.odyssey.bean.cmd.RebateConfigUpdateCmd;
import com.example.odyssey.bean.dto.RebateConfigDTO;
import com.example.odyssey.core.service.RebateConfigService;
import com.example.odyssey.model.entity.RebateConfig;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import org.springframework.beans.BeanUtils;
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

    @Override
    public SingleResponse add(RebateConfigCreateCmd rebateConfigCreateCmd) {

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

        rebateConfigMapper.insert(rebateConfig);

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

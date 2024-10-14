package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RebateConfigUpdateCmd;
import com.example.odyssey.bean.dto.RebateConfigDTO;
import com.example.odyssey.core.service.RebateConfigService;
import com.example.odyssey.model.entity.RebateConfig;
import com.example.odyssey.model.mapper.RebateConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RebateConfigServiceImpl implements RebateConfigService {

    @Resource
    RebateConfigMapper rebateConfigMapper;
    @Override
    public SingleResponse update(RebateConfigUpdateCmd rebateConfigUpdateCmd) {

        QueryWrapper<RebateConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recommend_type", rebateConfigUpdateCmd.getRecommendType());
        queryWrapper.eq("rebate_type", rebateConfigUpdateCmd.getRebateType());

        RebateConfig rebateConfig = rebateConfigMapper.selectOne(queryWrapper);
        if (rebateConfig == null) {
            return SingleResponse.buildFailure("配置不存在");
        }

        rebateConfig.setFirstRebateRate(rebateConfigUpdateCmd.getFirstRebateRate());
        rebateConfig.setSecondRebateRate(rebateConfigUpdateCmd.getSecondRebateRate());

        rebateConfigMapper.updateById(rebateConfig);

        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<RebateConfigDTO> list() {

        List<RebateConfigDTO> rebateConfigDTOList = new ArrayList<>();

        QueryWrapper<RebateConfig> queryWrapper = new QueryWrapper<>();

        List<RebateConfig> rebateConfigList = rebateConfigMapper.selectList(queryWrapper);

        for (RebateConfig rebateConfig : rebateConfigList) {
            RebateConfigDTO rebateConfigDTO = new RebateConfigDTO();
            rebateConfigDTO.setRecommendType(rebateConfig.getRecommendType());
            rebateConfigDTO.setRebateType(rebateConfig.getRebateType());
            rebateConfigDTO.setFirstRebateRate(rebateConfig.getFirstRebateRate());
            rebateConfigDTO.setSecondRebateRate(rebateConfig.getSecondRebateRate());

            rebateConfigDTOList.add(rebateConfigDTO);
        }
        return MultiResponse.of(rebateConfigDTOList);
    }
}

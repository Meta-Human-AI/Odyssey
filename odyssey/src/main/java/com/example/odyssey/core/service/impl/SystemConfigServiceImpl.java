package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.dto.SystemConfigDTO;
import com.example.odyssey.core.service.SystemConfigService;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    SystemConfigMapper systemConfigMapper;

    @Override
    public MultiResponse<SystemConfigDTO> listSystemConfig() {

        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();

        List<SystemConfig> systemConfigs = systemConfigMapper.selectList(queryWrapper);

        List<SystemConfigDTO> systemConfigDTOList = new ArrayList<>();
        for (SystemConfig systemConfig : systemConfigs) {

            SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
            systemConfigDTO.setId(systemConfig.getId());
            systemConfigDTO.setKey(systemConfig.getKey());
            systemConfigDTO.setValue(systemConfig.getValue());

            systemConfigDTOList.add(systemConfigDTO);
        }

        return MultiResponse.of(systemConfigDTOList);
    }
}

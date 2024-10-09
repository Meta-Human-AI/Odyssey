package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsConfigUpdateCmd;
import com.example.odyssey.bean.dto.OdsConfigDTO;
import com.example.odyssey.core.service.OdsConfigService;
import com.example.odyssey.model.entity.OdsConfig;
import com.example.odyssey.model.mapper.OdsConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OdsConfigServiceImpl implements OdsConfigService {

    @Resource
    OdsConfigMapper odsConfigMapper;
    @Override
    public SingleResponse update(OdsConfigUpdateCmd odsConfigUpdateCmd) {
        QueryWrapper<OdsConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", odsConfigUpdateCmd.getType());

        OdsConfig odsConfig = odsConfigMapper.selectOne(queryWrapper);
        if (odsConfig == null) {
            return SingleResponse.buildFailure("配置不存在");
        }

        odsConfig.setRate(odsConfigUpdateCmd.getRate());
        odsConfig.setNumber(odsConfigUpdateCmd.getNumber());
        odsConfigMapper.updateById(odsConfig);
        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<OdsConfigDTO> list() {

        List<OdsConfigDTO> odsConfigDTOList = new ArrayList<>();

        QueryWrapper<OdsConfig> queryWrapper = new QueryWrapper<>();

        List<OdsConfig> odsConfigList = odsConfigMapper.selectList(queryWrapper);

        for (OdsConfig odsConfig : odsConfigList) {
            OdsConfigDTO odsConfigDTO = new OdsConfigDTO();
            odsConfigDTO.setType(odsConfig.getType());
            odsConfigDTO.setRate(odsConfig.getRate());
            odsConfigDTO.setNumber(odsConfig.getNumber());
            odsConfigDTOList.add(odsConfigDTO);
        }
        return MultiResponse.of(odsConfigDTOList);
    }
}
